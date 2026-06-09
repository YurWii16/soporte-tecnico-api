package com.veterinary.support.controller;

import com.veterinary.support.dto.SolicitudSoporteRequestDTO;
import com.veterinary.support.dto.SolicitudSoporteResponseDTO;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/solicitudes-soporte")
@RequiredArgsConstructor
@Tag(name = "Solicitudes de Soporte", description = "Gestión de solicitudes de soporte técnico veterinario")
public class SolicitudSoporteController {

    private final SolicitudSoporteService service;

    @GetMapping
    @Operation(summary = "Obtener todas las solicitudes", description = "Retorna el listado completo de solicitudes de soporte")
    public ResponseEntity<List<SolicitudSoporteResponseDTO>> getAll() {
        return ResponseEntity.ok(service.obtenerTodas());
    }

    @PostMapping
    @Operation(summary = "Crear solicitud", description = "Crea una nueva solicitud de soporte técnico")
    @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente")
    public ResponseEntity<SolicitudSoporteResponseDTO> create(
            @Valid @RequestBody SolicitudSoporteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener solicitud por ID")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    public ResponseEntity<SolicitudSoporteResponseDTO> getById(
            @Parameter(description = "ID de la solicitud") @PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar solicitud completa")
    public ResponseEntity<SolicitudSoporteResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudSoporteRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar solicitud")
    @ApiResponse(responseCode = "204", description = "Solicitud eliminada")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de la solicitud",
               description = "Estados válidos: PENDIENTE, EN_PROCESO, RESUELTO, CERRADO")
    public ResponseEntity<SolicitudSoporteResponseDTO> updateEstado(
            @PathVariable Long id,
            @RequestParam EstadoSolicitud estado) {
        return ResponseEntity.ok(service.actualizarEstado(id, estado));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener solicitudes por estado")
    public ResponseEntity<List<SolicitudSoporteResponseDTO>> getByEstado(
            @PathVariable EstadoSolicitud estado) {
        return ResponseEntity.ok(service.obtenerPorEstado(estado));
    }

    @GetMapping("/clinica/{clinica}")
    @Operation(summary = "Obtener solicitudes por clínica")
    public ResponseEntity<List<SolicitudSoporteResponseDTO>> getByClinica(
            @PathVariable String clinica) {
        return ResponseEntity.ok(service.obtenerPorClinica(clinica));
    }

    @GetMapping("/activas")
    @Operation(summary = "Obtener solicitudes activas",
               description = "Retorna las solicitudes en estado PENDIENTE o EN_PROCESO")
    public ResponseEntity<List<SolicitudSoporteResponseDTO>> getActivas() {
        return ResponseEntity.ok(service.obtenerActivas());
    }

    @GetMapping("/estadisticas/por-estado")
    @Operation(summary = "Estadísticas por estado",
               description = "Retorna el conteo de solicitudes agrupado por estado")
    public ResponseEntity<Map<String, Long>> getEstadisticasPorEstado() {
        return ResponseEntity.ok(service.estadisticasPorEstado());
    }
}
