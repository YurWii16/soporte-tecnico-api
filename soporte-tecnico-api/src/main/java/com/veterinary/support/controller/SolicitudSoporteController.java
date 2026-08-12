package com.veterinary.support.controller;

import com.veterinary.support.dto.PageResponse;
import com.veterinary.support.dto.ResponseDTO;
import com.veterinary.support.dto.SolicitudSoporteRequestDTO;
import com.veterinary.support.dto.SolicitudSoporteResponseDTO;
import com.veterinary.support.model.SolicitudSoporte;
import com.veterinary.support.model.SolicitudSoporte.EstadoSolicitud;
import com.veterinary.support.service.SolicitudSoporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/solicitudes-soporte")
@RequiredArgsConstructor
@Tag(name = "Solicitudes de Soporte", description = "Gestión de solicitudes de soporte técnico veterinario")
public class SolicitudSoporteController {

    private final SolicitudSoporteService service;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Obtener todas las solicitudes de forma paginada (solo ADMIN)")
    public ResponseEntity<ResponseDTO<PageResponse<SolicitudSoporte>>> listarSolicitudes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SolicitudSoporte> dataPaginada = service.listarSolicitudesPaginadas(page, size);
        ResponseDTO<PageResponse<SolicitudSoporte>> response = new ResponseDTO<>(
                200, "Solicitudes recuperadas exitosamente", dataPaginada
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/mis-solicitudes")
    @Operation(summary = "Ver únicamente mis propias solicitudes (solo CLIENTE)")
    public ResponseEntity<List<SolicitudSoporteResponseDTO>> misSolicitudes() {
        return ResponseEntity.ok(service.misSolicitudes());
    }

    @PreAuthorize("hasRole('TECNICO')")
    @GetMapping("/mis-asignaciones")
    @Operation(summary = "Ver únicamente las solicitudes que me fueron asignadas (solo TECNICO)")
    public ResponseEntity<List<SolicitudSoporteResponseDTO>> misAsignaciones() {
        return ResponseEntity.ok(service.misAsignaciones());
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    @PostMapping
    @Operation(summary = "Crear solicitud (ADMIN o CLIENTE)")
    @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente")
    public ResponseEntity<SolicitudSoporteResponseDTO> create(
            @Valid @RequestBody SolicitudSoporteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener solicitud por ID (con control de acceso según el rol)")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    public ResponseEntity<SolicitudSoporteResponseDTO> getById(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar solicitud completa (ADMIN, o CLIENTE dueño si aún está PENDIENTE)")
    public ResponseEntity<SolicitudSoporteResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudSoporteRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar solicitud (solo ADMIN)")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de la solicitud (ADMIN, o TECNICO asignado)",
            description = "Estados válidos: PENDIENTE, EN_PROCESO, RESUELTO, CERRADO")
    public ResponseEntity<SolicitudSoporteResponseDTO> updateEstado(
            @PathVariable Long id,
            @RequestParam EstadoSolicitud estado) {
        return ResponseEntity.ok(service.actualizarEstado(id, estado));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/asignar-tecnico")
    @Operation(summary = "Asignar un técnico a una solicitud (solo ADMIN)")
    public ResponseEntity<SolicitudSoporteResponseDTO> asignarTecnico(
            @PathVariable Long id,
            @RequestParam String tecnico) {
        return ResponseEntity.ok(service.asignarTecnico(id, tecnico));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener solicitudes por estado (solo ADMIN)")
    public ResponseEntity<List<SolicitudSoporteResponseDTO>> getByEstado(
            @PathVariable EstadoSolicitud estado) {
        return ResponseEntity.ok(service.obtenerPorEstado(estado));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/clinica/{clinica}")
    @Operation(summary = "Obtener solicitudes por clínica (solo ADMIN)")
    public ResponseEntity<List<SolicitudSoporteResponseDTO>> getByClinica(
            @PathVariable String clinica) {
        return ResponseEntity.ok(service.obtenerPorClinica(clinica));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/activas")
    @Operation(summary = "Obtener solicitudes activas (solo ADMIN)")
    public ResponseEntity<List<SolicitudSoporteResponseDTO>> getActivas() {
        return ResponseEntity.ok(service.obtenerActivas());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estadisticas/por-estado")
    @Operation(summary = "Estadísticas por estado (solo ADMIN)")
    public ResponseEntity<Map<String, Long>> getEstadisticasPorEstado() {
        return ResponseEntity.ok(service.estadisticasPorEstado());
    }
}
