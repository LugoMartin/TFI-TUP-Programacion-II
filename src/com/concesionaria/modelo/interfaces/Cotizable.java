package com.concesionaria.modelo.interfaces;

/**
 * Contrato que define la capacidad de cotizar un vehiculo.
 * <p>
 * Separa la responsabilidad del calculo de precios del resto de la logica de negocio,
 * permitiendo que cada tipo de vehiculo aplique impuestos y recargos propios.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public interface Cotizable {

    /**
     * Calcula el precio final de venta del vehiculo.
     *
     * @return el precio final con impuestos y recargos aplicados; nunca {@code null}
     */
    Double calcularPrecioFinal();
}
