package com.veterinary.support.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_soporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @NotBlank
    @Column(nullable = false)
    private String clinica;

    @NotBlank
    @Column(nullable = false)
    private String solicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadSolicitud prioridad;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = EstadoSolicitud.PENDIENTE;
        if (prioridad == null) prioridad = PrioridadSolicitud.MEDIA;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum EstadoSolicitud {
        PENDIENTE, EN_PROCESO, RESUELTO, CERRADO
    }

    public enum PrioridadSolicitud {
        BAJA, MEDIA, ALTA, CRITICA
    }
}
