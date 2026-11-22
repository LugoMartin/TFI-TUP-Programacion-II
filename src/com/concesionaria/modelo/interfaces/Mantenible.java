package com.concesionaria.modelo.interfaces;

/**
 * Contrato que determina si un vehiculo requiere mantenimiento.
 * <p>
 * La decision se basa en la condicion del vehiculo y en su kilometraje, segun
 * el criterio que cada subclase especializa.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public interface Mantenible {

    /**
     * Indica si el vehiculo debe enviarse a service.
     *
     * @return {@code Boolean.TRUE} si requiere mantenimiento, {@code Boolean.FALSE} en caso contrario
     */
    Boolean requiereService();
}
