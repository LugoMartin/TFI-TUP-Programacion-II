package com.concesionaria.modelo.excepciones;

/**
 * Excepcion propia que se lanza cuando se intenta registrar un vehiculo cuyo
 * identificador (patente o chasis) ya se encuentra en el inventario.
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class VehiculoDuplicadoException extends Exception {

    /** Version de serializacion requerida por {@link Exception}. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion indicando el identificador repetido.
     *
     * @param identificador identificador que ya se encuentra registrado
     */
    public VehiculoDuplicadoException(String identificador) {
        super("Ya existe un vehiculo registrado con el identificador: " + identificador);
    }
}
