package com.veterinary.support.dto;

import com.veterinary.support.model.SolicitudSoporte.PrioridadSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitudSoporteRequestDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String titulo;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String descripcion;

    @NotBlank(message = "La clínica es obligatoria")
    private String clinica;

    @NotBlank(message = "El solicitante es obligatorio")
    private String solicitante;

    private PrioridadSolicitud prioridad;
}
