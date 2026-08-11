package com.veterinary.support.model;

import jakarta.persistence.*;
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

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private String clinica;

    @Column(nullable = false)
    private String solicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PrioridadSolicitud prioridad = PrioridadSolicitud.MEDIA;

    @Column(name = "creado_por_username")
    private String creadoPorUsername;

    @Column(name = "tecnico_asignado_username")
    private String tecnicoAsignadoUsername;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
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
