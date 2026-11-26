package com.concesionaria.modelo;

import com.concesionaria.modelo.enums.CondicionVehiculo;
import com.concesionaria.modelo.enums.TipoMotocicleta;

public class Motocicleta extends Vehiculo {

    private static final long serialVersionUID = 1L;

    private static final Integer KILOMETRAJE_SERVICE_MOTO = Integer.valueOf(5000);

    private TipoMotocicleta tipoMotocicleta;

    private Integer cilindrada;

    public Motocicleta() {
        super();

    }

    public Motocicleta(String identificador, String marca, String modelo, Integer anio, String color,
                       Integer kilometraje, Double precioBase, CondicionVehiculo condicion,
                       TipoMotocicleta tipoMotocicleta, Integer cilindrada) {
        super(identificador, marca, modelo, anio, color, kilometraje, precioBase, condicion);
        this.tipoMotocicleta = tipoMotocicleta;
        this.cilindrada = cilindrada;
    }

    public TipoMotocicleta getTipoMotocicleta() {
        return this.tipoMotocicleta;
    }

    public void setTipoMotocicleta(TipoMotocicleta tipoMotocicleta) {
        this.tipoMotocicleta = tipoMotocicleta;
    }

    public Integer getCilindrada() {
        return this.cilindrada;
    }

    public void setCilindrada(Integer cilindrada) {
        this.cilindrada = cilindrada;
    }

    @Override
    public String getCategoria() {
        return "Motocicleta";
    }

    @Override
    public String obtenerDetalleEspecifico() {
        String tipo = (this.tipoMotocicleta == null) ? "Sin tipo" : this.tipoMotocicleta.name();
        String cilindradaTexto = (this.cilindrada == null) ? "0" : this.cilindrada.toString();
        return tipo + " " + cilindradaTexto + " cc";
    }

    @Override
    public Double calcularPrecioFinal() {
        Double precio = super.calcularPrecioFinal();
        Integer cc = (this.cilindrada == null) ? Integer.valueOf(0) : this.cilindrada;

        Double recargo = (cc.intValue() > 500) ? Double.valueOf(0.07) : Double.valueOf(0.0);
        return Double.valueOf(precio.doubleValue() + (precio.doubleValue() * recargo.doubleValue()));
    }

    @Override
    public Boolean requiereService() {
        if (CondicionVehiculo.NUEVO.equals(this.getCondicion())) {
            return Boolean.FALSE;
        }
        Integer kilometros = (this.getKilometraje() == null) ? Integer.valueOf(0) : this.getKilometraje();
        return Boolean.valueOf(kilometros.intValue() >= KILOMETRAJE_SERVICE_MOTO.intValue());
    }
}
