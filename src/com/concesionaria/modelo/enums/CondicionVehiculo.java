package com.concesionaria.modelo.enums;

/**
 * Condicion comercial de un vehiculo dentro del stock de la concesionaria.
 * <p>
 * Se utiliza tanto para la validacion de reglas de negocio como para el calculo
 * de impuestos y recargos en {@code calcularPrecioFinal()}.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public enum CondicionVehiculo {

    /** Vehiculo con 0 kilometros, sin uso previo. */
    NUEVO,

    /** Vehiculo con kilometraje mayor a 0, proveniente de un uso anterior. */
    USADO
}
