package com.concesionaria.modelo;

import java.io.Serializable;
import java.util.Objects;

import com.concesionaria.modelo.enums.CondicionVehiculo;
import com.concesionaria.modelo.interfaces.Cotizable;
import com.concesionaria.modelo.interfaces.Mantenible;

/**
 * Superclase abstracta que modela los datos comunes a todo vehiculo de la concesionaria.
 * <p>
 * Cumple el estandar POJO / JavaBean: atributos privados, constructor sin argumentos,
 * constructor parametrizado, metodos de acceso y sobreescritura de {@code equals},
 * {@code hashCode} y {@code toString}. La identidad de un vehiculo se determina
 * exclusivamente por su identificador unico (patente o codigo de chasis).
 * </p>
 * <p>
 * Implementa {@link Cotizable} y {@link Mantenible} para fijar el comportamiento base
 * de calculo de precio y de mantenimiento, que cada subclase puede especializar
 * mediante polimorfismo.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public abstract class Vehiculo implements Serializable, Cotizable, Mantenible {

    /** Version de serializacion requerida por la interfaz {@link Serializable}. */
    private static final long serialVersionUID = 1L;

    /** Kilometraje a partir del cual un vehiculo usado requiere service (regla base). */
    protected static final Integer KILOMETRAJE_SERVICE = Integer.valueOf(10000);

    /** Identificador unico: patente o codigo de chasis. */
    private String identificador;

    /** Marca del vehiculo. */
    private String marca;

    /** Modelo comercial del vehiculo. */
    private String modelo;

    /** Anio de fabricacion del vehiculo. */
    private Integer anio;

    /** Color exterior del vehiculo. */
    private String color;

    /** Kilometraje registrado: 0 para un vehiculo nuevo. */
    private Integer kilometraje;

    /** Precio base antes de impuestos o recargos. */
    private Double precioBase;

    /** Condicion comercial: nuevo o usado. */
    private CondicionVehiculo condicion;

    /**
     * Constructor sin argumentos exigido por el estandar JavaBean.
     * <p>
     * Los atributos quedan con sus valores por defecto; la validacion de obligatoriedad
     * se realiza en la capa de servicio antes de persistir.
     * </p>
     */
    public Vehiculo() {
        // Constructor vacio: requerido por frameworks, DAO y herramientas de reflexion.
    }

    /**
     * Crea un vehiculo con todos sus datos comunes.
     *
     * @param identificador identificador unico (patente o chasis)
     * @param marca         marca del vehiculo
     * @param modelo        modelo comercial
     * @param anio          anio de fabricacion
     * @param color         color exterior
     * @param kilometraje   kilometros registrados
     * @param precioBase    precio base antes de recargos
     * @param condicion     condicion comercial (NUEVO o USADO)
     */
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

    /**
     * Devuelve el identificador unico del vehiculo.
     *
     * @return el identificador (patente o chasis), o {@code null} si no fue asignado
     */
    public String getIdentificador() {
        return this.identificador;
    }

    /**
     * Asigna el identificador unico del vehiculo.
     *
     * @param identificador nuevo identificador (patente o chasis)
     */
    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    /**
     * Devuelve la marca del vehiculo.
     *
     * @return la marca, o {@code null} si no fue asignada
     */
    public String getMarca() {
        return this.marca;
    }

    /**
     * Asigna la marca del vehiculo.
     *
     * @param marca nueva marca
     */
    public void setMarca(String marca) {
        this.marca = marca;
    }

    /**
     * Devuelve el modelo comercial del vehiculo.
     *
     * @return el modelo, o {@code null} si no fue asignado
     */
    public String getModelo() {
        return this.modelo;
    }

    /**
     * Asigna el modelo comercial del vehiculo.
     *
     * @param modelo nuevo modelo
     */
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    /**
     * Devuelve el anio de fabricacion.
     *
     * @return el anio como {@link Integer}, o {@code null} si no fue asignado
     */
    public Integer getAnio() {
        return this.anio;
    }

    /**
     * Asigna el anio de fabricacion.
     *
     * @param anio nuevo anio de fabricacion
     */
    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    /**
     * Devuelve el color exterior.
     *
     * @return el color, o {@code null} si no fue asignado
     */
    public String getColor() {
        return this.color;
    }

    /**
     * Asigna el color exterior.
     *
     * @param color nuevo color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Devuelve el kilometraje registrado.
     *
     * @return los kilometros como {@link Integer}, o {@code null} si no fue asignado
     */
    public Integer getKilometraje() {
        return this.kilometraje;
    }

    /**
     * Asigna el kilometraje registrado.
     *
     * @param kilometraje nuevos kilometros
     */
    public void setKilometraje(Integer kilometraje) {
        this.kilometraje = kilometraje;
    }

    /**
     * Devuelve el precio base del vehiculo.
     *
     * @return el precio base como {@link Double}, o {@code null} si no fue asignado
     */
    public Double getPrecioBase() {
        return this.precioBase;
    }

    /**
     * Asigna el precio base del vehiculo.
     *
     * @param precioBase nuevo precio base
     */
    public void setPrecioBase(Double precioBase) {
        this.precioBase = precioBase;
    }

    /**
     * Devuelve la condicion comercial del vehiculo.
     *
     * @return la condicion, o {@code null} si no fue asignada
     */
    public CondicionVehiculo getCondicion() {
        return this.condicion;
    }

    /**
     * Asigna la condicion comercial del vehiculo.
     *
     * @param condicion nueva condicion (NUEVO o USADO)
     */
    public void setCondicion(CondicionVehiculo condicion) {
        this.condicion = condicion;
    }

    /**
     * Devuelve la categoria comercial del vehiculo.
     *
     * @return categoria legible para el usuario (por ejemplo, "Automovil")
     */
    public abstract String getCategoria();

    /**
     * Devuelve el detalle propio de la subclase: carroceria, o tipo de moto y cilindrada.
     *
     * @return texto con el detalle especifico del tipo de vehiculo
     */
    public abstract String obtenerDetalleEspecifico();

    /**
     * Calcula el precio final aplicando el criterio comun por condicion.
     * <p>
     * Los vehiculos nuevos tributan el 10 % de IVA y los usados un arancel de
     * transferencia del 3 %. Las subclases pueden sumar recargos propios invocando
     * este metodo con {@code super}.
     * </p>
     *
     * @return el precio final calculado; nunca {@code null}
     */
    @Override
    public Double calcularPrecioFinal() {
        // Se usa un centinela para evitar NullPointerException si el precio no fue cargado.
        Double precio = (this.precioBase == null) ? Double.valueOf(0.0) : this.precioBase;
        Boolean esNuevo = CondicionVehiculo.NUEVO.equals(this.condicion);
        Double factor = esNuevo ? Double.valueOf(1.10) : Double.valueOf(1.03);
        return Double.valueOf(precio.doubleValue() * factor.doubleValue());
    }

    /**
     * Indica si el vehiculo necesita service segun su condicion y kilometraje.
     * <p>
     * Un vehiculo nuevo nunca requiere service; uno usado lo requiere al superar
     * {@link #KILOMETRAJE_SERVICE} kilometros.
     * </p>
     *
     * @return {@code Boolean.TRUE} cuando debe realizarse el service, {@code Boolean.FALSE} en caso contrario
     */
    @Override
    public Boolean requiereService() {
        // Un vehiculo nuevo se considera siempre libre de mantenimiento.
        if (CondicionVehiculo.NUEVO.equals(this.condicion)) {
            return Boolean.FALSE;
        }
        Integer kilometros = (this.kilometraje == null) ? Integer.valueOf(0) : this.kilometraje;
        return Boolean.valueOf(kilometros.intValue() >= KILOMETRAJE_SERVICE.intValue());
    }

    /**
     * Compara dos vehiculos por su identificador unico.
     *
     * @param obj objeto a comparar
     * @return {@code true} si ambos son vehiculos con el mismo identificador
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vehiculo)) {
            return false;
        }
        Vehiculo otro = (Vehiculo) obj;
        // La identidad no depende de la subclase: solo del identificador unico.
        return Objects.equals(this.identificador, otro.identificador);
    }

    /**
     * Devuelve el codigo hash basado en el identificador unico.
     *
     * @return codigo hash coherente con {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.identificador);
    }

    /**
     * Devuelve una representacion textual de todos los datos comunes y del detalle especifico.
     *
     * @return cadena con el estado completo del vehiculo
     */
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
