package com.concesionaria.modelo.excepciones;

/**
 * Excepcion propia que se lanza cuando no existe un vehiculo con el identificador
 * solicitado para una operacion de busqueda, modificacion o baja.
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class VehiculoNoEncontradoException extends Exception {

    /** Version de serializacion requerida por {@link Exception}. */
    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion indicando el identificador buscado.
     *
     * @param identificador identificador del vehiculo no localizado
     */
    public VehiculoNoEncontradoException(String identificador) {
        super("No se encontro ningun vehiculo con el identificador: " + identificador);
    }
}
