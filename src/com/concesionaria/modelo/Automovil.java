package com.concesionaria.modelo;

import com.concesionaria.modelo.enums.CondicionVehiculo;
import com.concesionaria.modelo.enums.TipoCarroceria;

/**
 * Representa un automovil dentro del stock de la concesionaria.
 * <p>
 * Extiende {@link Vehiculo} incorporando el tipo de carroceria y especializando
 * el calculo de precio para las carrocerias de mayor tamaño.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class Automovil extends Vehiculo {

    /** Version de serializacion requerida por {@link java.io.Serializable}. */
    private static final long serialVersionUID = 1L;

    /** Tipo de carroceria del automovil. */
    private TipoCarroceria tipoCarroceria;

    /**
     * Constructor sin argumentos exigido por el estandar JavaBean.
     */
    public Automovil() {
        super();
        // Los atributos propios quedan con sus valores por defecto.
    }

    /**
     * Crea un automovil con todos sus datos.
     *
     * @param identificador  identificador unico (patente o chasis)
     * @param marca          marca del automovil
     * @param modelo         modelo comercial
     * @param anio           anio de fabricacion
     * @param color          color exterior
     * @param kilometraje    kilometros registrados
     * @param precioBase     precio base antes de recargos
     * @param condicion      condicion comercial (NUEVO o USADO)
     * @param tipoCarroceria tipo de carroceria del automovil
     */
    public Automovil(String identificador, String marca, String modelo, Integer anio, String color,
                     Integer kilometraje, Double precioBase, CondicionVehiculo condicion,
                     TipoCarroceria tipoCarroceria) {
        super(identificador, marca, modelo, anio, color, kilometraje, precioBase, condicion);
        this.tipoCarroceria = tipoCarroceria;
    }

    /**
     * Devuelve el tipo de carroceria del automovil.
     *
     * @return la carroceria, o {@code null} si no fue asignada
     */
    public TipoCarroceria getTipoCarroceria() {
        return this.tipoCarroceria;
    }

    /**
     * Asigna el tipo de carroceria del automovil.
     *
     * @param tipoCarroceria nueva carroceria
     */
    public void setTipoCarroceria(TipoCarroceria tipoCarroceria) {
        this.tipoCarroceria = tipoCarroceria;
    }

    /**
     * Devuelve la categoria comercial del vehiculo.
     *
     * @return la cadena "Automovil"
     */
    @Override
    public String getCategoria() {
        return "Automovil";
    }

    /**
     * Devuelve el detalle especifico del automovil.
     *
     * @return el nombre de la carroceria, o "Sin carroceria" si no fue asignada
     */
    @Override
    public String obtenerDetalleEspecifico() {
        return (this.tipoCarroceria == null) ? "Sin carroceria" : this.tipoCarroceria.name();
    }

    /**
     * Calcula el precio final sumando un recargo del 5 % a las carrocerias SUV y PICKUP.
     *
     * @return precio final con impuestos base y recargo por carroceria
     */
    @Override
    public Double calcularPrecioFinal() {
        Double precio = super.calcularPrecioFinal();
        // Las carrocerias grandes requieren una preparacion adicional del 5 %.
        Boolean esGrande = TipoCarroceria.SUV.equals(this.tipoCarroceria)
                || TipoCarroceria.PICKUP.equals(this.tipoCarroceria);
        Double recargo = esGrande ? Double.valueOf(0.05) : Double.valueOf(0.0);
        return Double.valueOf(precio.doubleValue() + (precio.doubleValue() * recargo.doubleValue()));
    }
}
