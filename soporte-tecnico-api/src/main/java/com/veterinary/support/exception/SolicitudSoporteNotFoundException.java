package com.veterinary.support.exception;

public class SolicitudSoporteNotFoundException extends RuntimeException {

    public SolicitudSoporteNotFoundException(Long id) {
        super("No se encontró la solicitud de soporte con ID: " + id);
    }
}
