package com.concesionaria.modelo;

import com.concesionaria.modelo.enums.CondicionVehiculo;
import com.concesionaria.modelo.enums.TipoCarroceria;

/**
 * Representa una camioneta dentro del stock de la concesionaria.
 * <p>
 * Extiende {@link Vehiculo} incorporando el tipo de carroceria y aplicando un
 * recargo propio para las configuraciones PICKUP y UTILITARIO.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class Camioneta extends Vehiculo {

    /** Version de serializacion requerida por {@link java.io.Serializable}. */
    private static final long serialVersionUID = 1L;

    /** Tipo de carroceria de la camioneta. */
    private TipoCarroceria tipoCarroceria;

    /**
     * Constructor sin argumentos exigido por el estandar JavaBean.
     */
    public Camioneta() {
        super();
        // Los atributos propios quedan con sus valores por defecto.
    }

    /**
     * Crea una camioneta con todos sus datos.
     *
     * @param identificador  identificador unico (patente o chasis)
     * @param marca          marca de la camioneta
     * @param modelo         modelo comercial
     * @param anio           anio de fabricacion
     * @param color          color exterior
     * @param kilometraje    kilometros registrados
     * @param precioBase     precio base antes de recargos
     * @param condicion      condicion comercial (NUEVO o USADO)
     * @param tipoCarroceria tipo de carroceria de la camioneta
     */
    public Camioneta(String identificador, String marca, String modelo, Integer anio, String color,
                     Integer kilometraje, Double precioBase, CondicionVehiculo condicion,
                     TipoCarroceria tipoCarroceria) {
        super(identificador, marca, modelo, anio, color, kilometraje, precioBase, condicion);
        this.tipoCarroceria = tipoCarroceria;
    }

    /**
     * Devuelve el tipo de carroceria de la camioneta.
     *
     * @return la carroceria, o {@code null} si no fue asignada
     */
    public TipoCarroceria getTipoCarroceria() {
        return this.tipoCarroceria;
    }

    /**
     * Asigna el tipo de carroceria de la camioneta.
     *
     * @param tipoCarroceria nueva carroceria
     */
    public void setTipoCarroceria(TipoCarroceria tipoCarroceria) {
        this.tipoCarroceria = tipoCarroceria;
    }

    /**
     * Devuelve la categoria comercial del vehiculo.
     *
     * @return la cadena "Camioneta"
     */
    @Override
    public String getCategoria() {
        return "Camioneta";
    }

    /**
     * Devuelve el detalle especifico de la camioneta.
     *
     * @return el nombre de la carroceria, o "Sin carroceria" si no fue asignada
     */
    @Override
    public String obtenerDetalleEspecifico() {
        return (this.tipoCarroceria == null) ? "Sin carroceria" : this.tipoCarroceria.name();
    }

    /**
     * Calcula el precio final sumando un recargo del 8 % a las carrocerias de trabajo.
     *
     * @return precio final con impuestos base y recargo por carroceria
     */
    @Override
    public Double calcularPrecioFinal() {
        Double precio = super.calcularPrecioFinal();
        // Las versiones de carga (PICKUP y UTILITARIO) incluyen equipamiento extra.
        Boolean esDeCarga = TipoCarroceria.PICKUP.equals(this.tipoCarroceria)
                || TipoCarroceria.UTILITARIO.equals(this.tipoCarroceria);
        Double recargo = esDeCarga ? Double.valueOf(0.08) : Double.valueOf(0.0);
        return Double.valueOf(precio.doubleValue() + (precio.doubleValue() * recargo.doubleValue()));
    }
}
