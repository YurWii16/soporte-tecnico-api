package com.veterinary.support.repository;

import com.veterinary.support.model.SolicitudSoporte;
import com.veterinary.support.model.SolicitudSoporte.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudSoporteRepository extends JpaRepository<SolicitudSoporte, Long> {

    List<SolicitudSoporte> findByEstado(EstadoSolicitud estado);

    List<SolicitudSoporte> findByEstadoIn(List<EstadoSolicitud> estados);

    @Query("SELECT s.estado AS estado, COUNT(s) AS cantidad FROM SolicitudSoporte s GROUP BY s.estado")
    List<Object[]> countByEstado();
    List<SolicitudSoporte> findByCreadoPorUsername(String creadoPorUsername);
    List<SolicitudSoporte> findByTecnicoAsignadoUsername(String tecnicoAsignadoUsername);
}
