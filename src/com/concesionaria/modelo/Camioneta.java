package com.concesionaria.modelo;

import com.concesionaria.modelo.enums.CondicionVehiculo;
import com.concesionaria.modelo.enums.TipoCarroceria;

public class Camioneta extends Vehiculo {

    private static final long serialVersionUID = 1L;

    private TipoCarroceria tipoCarroceria;

    public Camioneta() {
        super();

    }

    public Camioneta(String identificador, String marca, String modelo, Integer anio, String color,
                     Integer kilometraje, Double precioBase, CondicionVehiculo condicion,
                     TipoCarroceria tipoCarroceria) {
        super(identificador, marca, modelo, anio, color, kilometraje, precioBase, condicion);
        this.tipoCarroceria = tipoCarroceria;
    }

    public TipoCarroceria getTipoCarroceria() {
        return this.tipoCarroceria;
    }

    public void setTipoCarroceria(TipoCarroceria tipoCarroceria) {
        this.tipoCarroceria = tipoCarroceria;
    }

    @Override
    public String getCategoria() {
        return "Camioneta";
    }

    @Override
    public String obtenerDetalleEspecifico() {
        return (this.tipoCarroceria == null) ? "Sin carroceria" : this.tipoCarroceria.name();
    }

    @Override
    public Double calcularPrecioFinal() {
        Double precio = super.calcularPrecioFinal();

        Boolean esDeCarga = TipoCarroceria.PICKUP.equals(this.tipoCarroceria)
                || TipoCarroceria.UTILITARIO.equals(this.tipoCarroceria);
        Double recargo = esDeCarga ? Double.valueOf(0.08) : Double.valueOf(0.0);
        return Double.valueOf(precio.doubleValue() + (precio.doubleValue() * recargo.doubleValue()));
    }
}
