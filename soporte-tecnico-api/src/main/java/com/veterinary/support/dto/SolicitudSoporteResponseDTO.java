package com.veterinary.support.dto;

import com.veterinary.support.model.SolicitudSoporte;
import com.veterinary.support.model.SolicitudSoporte.EstadoSolicitud;
import com.veterinary.support.model.SolicitudSoporte.PrioridadSolicitud;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolicitudSoporteResponseDTO {

    private Long id;
    private String titulo;
    private String descripcion;
    private String clinica;
    private String solicitante;
    private EstadoSolicitud estado;
    private PrioridadSolicitud prioridad;
    private String creadoPorUsername;
    private String tecnicoAsignadoUsername;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public static SolicitudSoporteResponseDTO fromEntity(SolicitudSoporte entity) {
        SolicitudSoporteResponseDTO dto = new SolicitudSoporteResponseDTO();
        dto.setId(entity.getId());
        dto.setTitulo(entity.getTitulo());
        dto.setDescripcion(entity.getDescripcion());
        dto.setClinica(entity.getClinica());
        dto.setSolicitante(entity.getSolicitante());
        dto.setEstado(entity.getEstado());
        dto.setPrioridad(entity.getPrioridad());
        dto.setCreadoPorUsername(entity.getCreadoPorUsername());
        dto.setTecnicoAsignadoUsername(entity.getTecnicoAsignadoUsername());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setFechaActualizacion(entity.getFechaActualizacion());
        return dto;
    }
}
