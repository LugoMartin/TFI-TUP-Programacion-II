package com.concesionaria.modelo;

import com.concesionaria.modelo.enums.CondicionVehiculo;
import com.concesionaria.modelo.enums.TipoMotocicleta;

/**
 * Representa una motocicleta dentro del stock de la concesionaria.
 * <p>
 * Extiende {@link Vehiculo} incorporando el tipo de motocicleta y la cilindrada,
 * y reduce el intervalo de service a 5000 kilometros por tratarse de un motor mas exigido.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class Motocicleta extends Vehiculo {

    /** Version de serializacion requerida por {@link java.io.Serializable}. */
    private static final long serialVersionUID = 1L;

    /** Intervalo de service propio de las motocicletas. */
    private static final Integer KILOMETRAJE_SERVICE_MOTO = Integer.valueOf(5000);

    /** Tipo de motocicleta. */
    private TipoMotocicleta tipoMotocicleta;

    /** Cilindrada del motor en centimetros cubicos. */
    private Integer cilindrada;

    /**
     * Constructor sin argumentos exigido por el estandar JavaBean.
     */
    public Motocicleta() {
        super();
        // Los atributos propios quedan con sus valores por defecto.
    }

    /**
     * Crea una motocicleta con todos sus datos.
     *
     * @param identificador    identificador unico (patente o chasis)
     * @param marca           marca de la motocicleta
     * @param modelo          modelo comercial
     * @param anio            anio de fabricacion
     * @param color           color exterior
     * @param kilometraje     kilometros registrados
     * @param precioBase      precio base antes de recargos
     * @param condicion       condicion comercial (NUEVO o USADO)
     * @param tipoMotocicleta tipo de motocicleta
     * @param cilindrada      cilindrada en centimetros cubicos
     */
    public Motocicleta(String identificador, String marca, String modelo, Integer anio, String color,
                       Integer kilometraje, Double precioBase, CondicionVehiculo condicion,
                       TipoMotocicleta tipoMotocicleta, Integer cilindrada) {
        super(identificador, marca, modelo, anio, color, kilometraje, precioBase, condicion);
        this.tipoMotocicleta = tipoMotocicleta;
        this.cilindrada = cilindrada;
    }

    /**
     * Devuelve el tipo de motocicleta.
     *
     * @return el tipo, o {@code null} si no fue asignado
     */
    public TipoMotocicleta getTipoMotocicleta() {
        return this.tipoMotocicleta;
    }

    /**
     * Asigna el tipo de motocicleta.
     *
     * @param tipoMotocicleta nuevo tipo de motocicleta
     */
    public void setTipoMotocicleta(TipoMotocicleta tipoMotocicleta) {
        this.tipoMotocicleta = tipoMotocicleta;
    }

    /**
     * Devuelve la cilindrada de la motocicleta.
     *
     * @return los centimetros cubicos como {@link Integer}, o {@code null} si no fue asignada
     */
    public Integer getCilindrada() {
        return this.cilindrada;
    }

    /**
     * Asigna la cilindrada de la motocicleta.
     *
     * @param cilindrada nueva cilindrada en centimetros cubicos
     */
    public void setCilindrada(Integer cilindrada) {
        this.cilindrada = cilindrada;
    }

    /**
     * Devuelve la categoria comercial del vehiculo.
     *
     * @return la cadena "Motocicleta"
     */
    @Override
    public String getCategoria() {
        return "Motocicleta";
    }

    /**
     * Devuelve el detalle especifico de la motocicleta.
     *
     * @return el tipo y la cilindrada expresada en "cc"
     */
    @Override
    public String obtenerDetalleEspecifico() {
        String tipo = (this.tipoMotocicleta == null) ? "Sin tipo" : this.tipoMotocicleta.name();
        String cilindradaTexto = (this.cilindrada == null) ? "0" : this.cilindrada.toString();
        return tipo + " " + cilindradaTexto + " cc";
    }

    /**
     * Calcula el precio final sumando un 7 % cuando la cilindrada supera los 500 cc.
     *
     * @return precio final con impuestos base y recargo por cilindrada
     */
    @Override
    public Double calcularPrecioFinal() {
        Double precio = super.calcularPrecioFinal();
        Integer cc = (this.cilindrada == null) ? Integer.valueOf(0) : this.cilindrada;
        // Las motos de gran cilindrada tienen un sobrecargo por segmento premium.
        Double recargo = (cc.intValue() > 500) ? Double.valueOf(0.07) : Double.valueOf(0.0);
        return Double.valueOf(precio.doubleValue() + (precio.doubleValue() * recargo.doubleValue()));
    }

    /**
     * Indica si la motocicleta requiere service usando el intervalo propio de 5000 km.
     *
     * @return {@code Boolean.TRUE} cuando debe realizarse el service, {@code Boolean.FALSE} en caso contrario
     */
    @Override
    public Boolean requiereService() {
        if (CondicionVehiculo.NUEVO.equals(this.getCondicion())) {
            return Boolean.FALSE;
        }
        Integer kilometros = (this.getKilometraje() == null) ? Integer.valueOf(0) : this.getKilometraje();
        return Boolean.valueOf(kilometros.intValue() >= KILOMETRAJE_SERVICE_MOTO.intValue());
    }
}
