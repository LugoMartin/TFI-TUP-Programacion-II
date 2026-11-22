package com.concesionaria.modelo.excepciones;

/**
 * Excepcion propia que representa un dato de entrada invalido: campos vacios,
 * valores numericos no parseables, anios fuera de rango o inconsistencias entre
 * la condicion y el kilometraje.
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class DatoInvalidoException extends Exception {

    /** Version de serializacion requerida por {@link Exception}. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con un mensaje descriptivo del error.
     *
     * @param mensaje explicacion del dato invalido detectado
     */
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
