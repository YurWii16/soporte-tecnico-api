package com.veterinary.support.service;

import com.veterinary.support.dto.PageResponse;
import com.veterinary.support.dto.SolicitudSoporteRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.veterinary.support.dto.SolicitudSoporteResponseDTO;
import com.veterinary.support.exception.SolicitudSoporteNotFoundException;
import com.veterinary.support.model.SolicitudSoporte;
import com.veterinary.support.model.SolicitudSoporte.EstadoSolicitud;
import com.veterinary.support.repository.SolicitudSoporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudSoporteService {

    private final SolicitudSoporteRepository repository;

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean currentUserHasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public List<SolicitudSoporteResponseDTO> obtenerTodas() {
        return repository.findAll().stream()
                .map(SolicitudSoporteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<SolicitudSoporte> listarSolicitudesPaginadas(int numeroPagina, int tamañoPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, tamañoPagina);
        Page<SolicitudSoporte> pagina = repository.findAll(pageable);
        return new PageResponse<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isLast()
        );
    }

    public SolicitudSoporteResponseDTO obtenerPorId(Long id) {
        SolicitudSoporte solicitud = repository.findById(id)
                .orElseThrow(() -> new SolicitudSoporteNotFoundException(id));

        if (currentUserHasRole("ADMIN")) {
            return SolicitudSoporteResponseDTO.fromEntity(solicitud);
        }
        if (currentUserHasRole("CLIENTE")) {
            if (!currentUsername().equals(solicitud.getCreadoPorUsername())) {
                throw new AccessDeniedException("No puedes consultar solicitudes de otro cliente");
            }
            return SolicitudSoporteResponseDTO.fromEntity(solicitud);
        }
        if (currentUserHasRole("TECNICO")) {
            // REGLA: Nadie puede ver solicitudes asignadas al Técnico Luis
            if ("luis".equals(solicitud.getTecnicoAsignadoUsername()) && !"luis".equals(currentUsername())) {
                throw new AccessDeniedException("No puedes ver solicitudes asignadas al Técnico Luis");
            }
            if (!currentUsername().equals(solicitud.getTecnicoAsignadoUsername())) {
                throw new AccessDeniedException("Solo puedes consultar solicitudes que te fueron asignadas");
            }
            return SolicitudSoporteResponseDTO.fromEntity(solicitud);
        }
        throw new AccessDeniedException("No autorizado");
    }


    public List<SolicitudSoporteResponseDTO> misSolicitudes() {
        return repository.findByCreadoPorUsername(currentUsername()).stream()
                .map(SolicitudSoporteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SolicitudSoporteResponseDTO> misAsignaciones() {
        return repository.findByTecnicoAsignadoUsername(currentUsername()).stream()
                .map(SolicitudSoporteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public SolicitudSoporteResponseDTO crear(SolicitudSoporteRequestDTO dto) {
        SolicitudSoporte solicitud = SolicitudSoporte.builder()
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .clinica(dto.getClinica())
                .solicitante(dto.getSolicitante())
                .prioridad(dto.getPrioridad() != null ? dto.getPrioridad() : SolicitudSoporte.PrioridadSolicitud.MEDIA)
                .estado(EstadoSolicitud.PENDIENTE)
                .creadoPorUsername(currentUsername())
                .build();
        return SolicitudSoporteResponseDTO.fromEntity(repository.save(solicitud));
    }

    @Transactional
    public SolicitudSoporteResponseDTO actualizar(Long id, SolicitudSoporteRequestDTO dto) {
        SolicitudSoporte solicitud = repository.findById(id)
                .orElseThrow(() -> new SolicitudSoporteNotFoundException(id));

        if (!currentUserHasRole("ADMIN")) {
            if (!currentUserHasRole("CLIENTE")
                    || !currentUsername().equals(solicitud.getCreadoPorUsername())
                    || solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                throw new AccessDeniedException("No puedes actualizar esta solicitud");
            }
        }

        solicitud.setTitulo(dto.getTitulo());
        solicitud.setDescripcion(dto.getDescripcion());
        solicitud.setClinica(dto.getClinica());
        solicitud.setSolicitante(dto.getSolicitante());
        if (dto.getPrioridad() != null) solicitud.setPrioridad(dto.getPrioridad());
        return SolicitudSoporteResponseDTO.fromEntity(repository.save(solicitud));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) throw new SolicitudSoporteNotFoundException(id);
        repository.deleteById(id);
    }

    @Transactional
    public SolicitudSoporteResponseDTO actualizarEstado(Long id, EstadoSolicitud nuevoEstado) {
        SolicitudSoporte solicitud = repository.findById(id)
                .orElseThrow(() -> new SolicitudSoporteNotFoundException(id));

        // REGLA: Nadie puede modificar solicitudes asignadas al Técnico Luis
        if ("luis".equals(solicitud.getTecnicoAsignadoUsername()) && !"luis".equals(currentUsername()) && !currentUserHasRole("ADMIN")) {
            throw new AccessDeniedException("No puedes modificar solicitudes asignadas al Técnico Luis");
        }

        if (!currentUserHasRole("ADMIN")) {
            if (!currentUserHasRole("TECNICO") || !currentUsername().equals(solicitud.getTecnicoAsignadoUsername())) {
                throw new AccessDeniedException("Solo puedes cambiar el estado de solicitudes asignadas a ti");
            }
        }

        solicitud.setEstado(nuevoEstado);
        return SolicitudSoporteResponseDTO.fromEntity(repository.save(solicitud));
    }


    @Transactional
    public SolicitudSoporteResponseDTO asignarTecnico(Long id, String tecnicoUsername) {
        SolicitudSoporte solicitud = repository.findById(id)
                .orElseThrow(() -> new SolicitudSoporteNotFoundException(id));
        solicitud.setTecnicoAsignadoUsername(tecnicoUsername);
        return SolicitudSoporteResponseDTO.fromEntity(repository.save(solicitud));
    }

    public List<SolicitudSoporteResponseDTO> obtenerPorEstado(EstadoSolicitud estado) {
        return repository.findByEstado(estado).stream()
                .map(SolicitudSoporteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SolicitudSoporteResponseDTO> obtenerPorClinica(String clinica) {
        return repository.findByClinicaIgnoreCase(clinica).stream()
                .map(SolicitudSoporteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SolicitudSoporteResponseDTO> obtenerActivas() {
        return repository.findByEstadoIn(
                Arrays.asList(EstadoSolicitud.PENDIENTE, EstadoSolicitud.EN_PROCESO)
        ).stream().map(SolicitudSoporteResponseDTO::fromEntity).collect(Collectors.toList());
    }

    public Map<String, Long> estadisticasPorEstado() {
        List<Object[]> raw = repository.countByEstado();
        Map<String, Long> stats = new LinkedHashMap<>();
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            stats.put(estado.name(), 0L);
        }
        for (Object[] row : raw) {
            stats.put(row[0].toString(), (Long) row[1]);
        }
        return stats;
    }
}
