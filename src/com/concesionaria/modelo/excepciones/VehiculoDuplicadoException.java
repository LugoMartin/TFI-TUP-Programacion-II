package com.concesionaria.modelo.excepciones;

public class VehiculoDuplicadoException extends Exception {

    private static final long serialVersionUID = 1L;

    public VehiculoDuplicadoException(String identificador) {
        super("Ya existe un vehiculo registrado con el identificador: " + identificador);
    }
}
