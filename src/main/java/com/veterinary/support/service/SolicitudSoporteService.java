package com.veterinary.support.service;

import com.veterinary.support.dto.SolicitudSoporteRequestDTO;
import com.veterinary.support.dto.SolicitudSoporteResponseDTO;
import com.veterinary.support.exception.SolicitudSoporteNotFoundException;
import com.veterinary.support.model.SolicitudSoporte;
import com.veterinary.support.model.SolicitudSoporte.EstadoSolicitud;
import com.veterinary.support.repository.SolicitudSoporteRepository;
import lombok.RequiredArgsConstructor;
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

    public List<SolicitudSoporteResponseDTO> obtenerTodas() {
        return repository.findAll().stream()
                .map(SolicitudSoporteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public SolicitudSoporteResponseDTO obtenerPorId(Long id) {
        SolicitudSoporte solicitud = repository.findById(id)
                .orElseThrow(() -> new SolicitudSoporteNotFoundException(id));
        return SolicitudSoporteResponseDTO.fromEntity(solicitud);
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
                .build();
        return SolicitudSoporteResponseDTO.fromEntity(repository.save(solicitud));
    }

    @Transactional
    public SolicitudSoporteResponseDTO actualizar(Long id, SolicitudSoporteRequestDTO dto) {
        SolicitudSoporte solicitud = repository.findById(id)
                .orElseThrow(() -> new SolicitudSoporteNotFoundException(id));
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
        solicitud.setEstado(nuevoEstado);
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
