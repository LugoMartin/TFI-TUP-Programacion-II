package com.concesionaria.modelo;

import java.io.Serializable;
import java.util.Objects;

import com.concesionaria.modelo.enums.CondicionVehiculo;
import com.concesionaria.modelo.interfaces.Cotizable;
import com.concesionaria.modelo.interfaces.Mantenible;

public abstract class Vehiculo implements Serializable, Cotizable, Mantenible {

    private static final long serialVersionUID = 1L;

    protected static final Integer KILOMETRAJE_SERVICE = Integer.valueOf(10000);

    private String identificador;

    private String marca;

    private String modelo;

    private Integer anio;

    private String color;

    private Integer kilometraje;

    private Double precioBase;

    private CondicionVehiculo condicion;

    public Vehiculo() {

    }

    public Vehiculo(String identificador, String marca, String modelo, Integer anio, String color,
                    Integer kilometraje, Double precioBase, CondicionVehiculo condicion) {
        this.identificador = identificador;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.kilometraje = kilometraje;
        this.precioBase = precioBase;
        this.condicion = condicion;
    }

    public String getIdentificador() {
        return this.identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getMarca() {
        return this.marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return this.modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnio() {
        return this.anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getKilometraje() {
        return this.kilometraje;
    }

    public void setKilometraje(Integer kilometraje) {
        this.kilometraje = kilometraje;
    }

    public Double getPrecioBase() {
        return this.precioBase;
    }

    public void setPrecioBase(Double precioBase) {
        this.precioBase = precioBase;
    }

    public CondicionVehiculo getCondicion() {
        return this.condicion;
    }

    public void setCondicion(CondicionVehiculo condicion) {
        this.condicion = condicion;
    }

    public abstract String getCategoria();

    public abstract String obtenerDetalleEspecifico();

    @Override
    public Double calcularPrecioFinal() {

        Double precio = (this.precioBase == null) ? Double.valueOf(0.0) : this.precioBase;
        Boolean esNuevo = CondicionVehiculo.NUEVO.equals(this.condicion);
        Double factor = esNuevo ? Double.valueOf(1.10) : Double.valueOf(1.03);
        return Double.valueOf(precio.doubleValue() * factor.doubleValue());
    }

    @Override
    public Boolean requiereService() {

        if (CondicionVehiculo.NUEVO.equals(this.condicion)) {
            return Boolean.FALSE;
        }
        Integer kilometros = (this.kilometraje == null) ? Integer.valueOf(0) : this.kilometraje;
        return Boolean.valueOf(kilometros.intValue() >= KILOMETRAJE_SERVICE.intValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vehiculo)) {
            return false;
        }
        Vehiculo otro = (Vehiculo) obj;

        return Objects.equals(this.identificador, otro.identificador);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.identificador);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Vehiculo [identificador=").append(this.identificador)
                .append(", marca=").append(this.marca)
                .append(", modelo=").append(this.modelo)
                .append(", anio=").append(this.anio)
                .append(", color=").append(this.color)
                .append(", kilometraje=").append(this.kilometraje)
                .append(", precioBase=").append(this.precioBase)
                .append(", condicion=").append(this.condicion)
                .append(", detalle=").append(this.obtenerDetalleEspecifico())
                .append("]")
                .toString();
    }
}
