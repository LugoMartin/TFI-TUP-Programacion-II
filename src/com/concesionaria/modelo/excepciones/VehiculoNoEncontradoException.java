package com.concesionaria.modelo.excepciones;

public class VehiculoNoEncontradoException extends Exception {

    private static final long serialVersionUID = 1L;

    public VehiculoNoEncontradoException(String identificador) {
        super("No se encontro ningun vehiculo con el identificador: " + identificador);
    }
}
