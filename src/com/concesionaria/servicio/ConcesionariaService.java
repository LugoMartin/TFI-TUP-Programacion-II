package com.concesionaria.servicio;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.concesionaria.modelo.Automovil;
import com.concesionaria.modelo.Camioneta;
import com.concesionaria.modelo.Motocicleta;
import com.concesionaria.modelo.Vehiculo;
import com.concesionaria.modelo.enums.CondicionVehiculo;
import com.concesionaria.modelo.excepciones.DatoInvalidoException;
import com.concesionaria.modelo.excepciones.VehiculoDuplicadoException;
import com.concesionaria.modelo.excepciones.VehiculoNoEncontradoException;
import com.concesionaria.persistencia.RepositorioGenerico;

/**
 * Servicio de negocio que administra el inventario de la concesionaria.
 * <p>
 * Mantiene una copia del stock en memoria mediante un {@link Map} indexado por
 * identificador, delega la persistencia en un {@link RepositorioGenerico} inyectado
 * y centraliza todas las reglas de validacion y los filtros solicitados por
 * los directivos. Esta clase no interactúa con la consola: solo devuelve resultados
 * y lanza excepciones propias para que la vista decida cómo informarlos.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class ConcesionariaService {

    /** Anio minimo de fabricacion admitido por la concesionaria. */
    private static final Integer ANIO_MINIMO = Integer.valueOf(1900);

    /** Inventario en memoria, indexado por identificador unico. */
    private final Map<String, Vehiculo> inventario;

    /** Repositorio inyectado: permite cambiar el medio de persistencia sin tocar el servicio. */
    private final RepositorioGenerico<Vehiculo, String> repositorio;

    /** Anio corriente usado como limite superior de validacion. */
    private final Integer anioActual;

    /**
     * Crea el servicio con el anio actual del sistema como limite de validacion.
     *
     * @param repositorio repositorio de persistencia a utilizar; no puede ser nulo
     * @throws IllegalArgumentException si el repositorio es nulo
     */
    public ConcesionariaService(RepositorioGenerico<Vehiculo, String> repositorio) {
        this(repositorio, Integer.valueOf(Year.now().getValue()));
    }

    /**
     * Crea el servicio con un anio de referencia explicito, util para pruebas.
     *
     * @param repositorio repositorio de persistencia a utilizar; no puede ser nulo
     * @param anioActual  anio usado como limite superior; si es nulo se toma el anio del sistema
     * @throws IllegalArgumentException si el repositorio es nulo
     */
    public ConcesionariaService(RepositorioGenerico<Vehiculo, String> repositorio, Integer anioActual) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio de persistencia es obligatorio.");
        }
        this.repositorio = repositorio;
        this.anioActual = (anioActual == null) ? Integer.valueOf(Year.now().getValue()) : anioActual;
        // LinkedHashMap conserva el orden de alta de los vehiculos.
        this.inventario = new LinkedHashMap<>();
    }

    /**
     * Carga el stock persistido en el inventario en memoria, validando cada registro.
     *
     * @throws IOException           si no puede leerse el soporte de datos
     * @throws DatoInvalidoException si un registro del archivo no supera las reglas de negocio
     */
    public void cargarStock() throws IOException, DatoInvalidoException {
        this.inventario.clear();
        for (Vehiculo vehiculo : this.repositorio.listarTodos()) {
            this.validarVehiculo(vehiculo);
            String identificador = vehiculo.getIdentificador().trim();
            vehiculo.setIdentificador(identificador);
            if (this.inventario.containsKey(identificador)) {
                throw new DatoInvalidoException("Identificador repetido en el stock: " + identificador);
            }
            this.inventario.put(identificador, vehiculo);
        }
    }

    /**
     * Da de alta un vehiculo despues de validarlo y persistirlo.
     *
     * @param vehiculo vehiculo a incorporar al inventario
     * @throws IOException                si no puede escribirse el soporte de datos
     * @throws DatoInvalidoException      si el vehiculo no cumple las reglas de negocio
     * @throws VehiculoDuplicadoException si el identificador ya esta registrado
     */
    public void agregarVehiculo(Vehiculo vehiculo)
            throws IOException, DatoInvalidoException, VehiculoDuplicadoException {
        this.validarVehiculo(vehiculo);
        String identificador = vehiculo.getIdentificador().trim();
        if (this.inventario.containsKey(identificador)) {
            throw new VehiculoDuplicadoException(identificador);
        }
        // Se normaliza el identificador antes de persistir y de actualizar el Map.
        vehiculo.setIdentificador(identificador);
        this.repositorio.guardar(vehiculo);
        this.inventario.put(identificador, vehiculo);
    }

    /**
     * Modifica un vehiculo existente conservando su identificador.
     *
     * @param vehiculo vehiculo con los datos ya actualizados
     * @throws IOException                  si no puede escribirse el soporte de datos
     * @throws DatoInvalidoException        si el vehiculo no cumple las reglas de negocio
     * @throws VehiculoNoEncontradoException si el identificador no esta registrado
     */
    public void modificarVehiculo(Vehiculo vehiculo)
            throws IOException, DatoInvalidoException, VehiculoNoEncontradoException {
        this.validarVehiculo(vehiculo);
        String identificador = vehiculo.getIdentificador().trim();
        if (!this.inventario.containsKey(identificador)) {
            throw new VehiculoNoEncontradoException(identificador);
        }
        vehiculo.setIdentificador(identificador);
        this.repositorio.actualizar(vehiculo);
        this.inventario.put(identificador, vehiculo);
    }

    /**
     * Elimina un vehiculo del inventario y del soporte de datos.
     *
     * @param identificador identificador del vehiculo a borrar
     * @throws IOException                  si no puede escribirse el soporte de datos
     * @throws DatoInvalidoException        si el identificador esta vacio
     * @throws VehiculoNoEncontradoException si el identificador no esta registrado
     */
    public void eliminarVehiculo(String identificador)
            throws IOException, DatoInvalidoException, VehiculoNoEncontradoException {
        if (this.estaVacio(identificador)) {
            throw new DatoInvalidoException("El identificador del vehiculo a eliminar es obligatorio.");
        }
        String normalizado = identificador.trim();
        if (!this.inventario.containsKey(normalizado)) {
            throw new VehiculoNoEncontradoException(normalizado);
        }
        this.repositorio.eliminar(normalizado);
        this.inventario.remove(normalizado);
    }

    /**
     * Busca un vehiculo en el inventario en memoria por su identificador.
     *
     * @param identificador identificador unico buscado
     * @return el vehiculo encontrado
     * @throws DatoInvalidoException        si el identificador esta vacio
     * @throws VehiculoNoEncontradoException si no existe un vehiculo con ese identificador
     */
    public Vehiculo buscarPorId(String identificador)
            throws DatoInvalidoException, VehiculoNoEncontradoException {
        if (this.estaVacio(identificador)) {
            throw new DatoInvalidoException("El identificador de busqueda es obligatorio.");
        }
        String normalizado = identificador.trim();
        Vehiculo vehiculo = this.inventario.get(normalizado);
        if (vehiculo == null) {
            throw new VehiculoNoEncontradoException(normalizado);
        }
        return vehiculo;
    }

    /**
     * Devuelve una copia ordenada del inventario para no exponer la coleccion interna.
     *
     * @return lista de vehiculos ordenada por identificador
     */
    public List<Vehiculo> listarTodos() {
        // Se devuelve una copia: el llamador no puede modificar el Map del servicio.
        return this.inventario.values().stream()
                .sorted((a, b) -> String.valueOf(a.getIdentificador())
                        .compareTo(String.valueOf(b.getIdentificador())))
                .collect(Collectors.toList());
    }

    /**
     * Filtra el inventario por marca, admitiendo coincidencias parciales sin distinguir mayusculas.
     *
     * @param marca texto a buscar en la marca; si esta vacio se devuelve todo el inventario
     * @return lista de vehiculos que coinciden con la marca indicada
     */
    public List<Vehiculo> filtrarPorMarca(String marca) {
        List<Vehiculo> resultado = this.listarTodos();
        if (this.estaVacio(marca)) {
            return resultado;
        }
        String buscada = marca.trim().toLowerCase(Locale.ROOT);
        return resultado.stream()
                .filter(v -> v.getMarca() != null
                        && v.getMarca().toLowerCase(Locale.ROOT).contains(buscada))
                .collect(Collectors.toList());
    }

    /**
     * Filtra el inventario por categoria comercial (Automovil, Camioneta o Motocicleta).
     *
     * @param categoria categoria a buscar; si esta vacio se devuelve todo el inventario
     * @return lista de vehiculos de la categoria indicada
     */
    public List<Vehiculo> filtrarPorCategoria(String categoria) {
        List<Vehiculo> resultado = this.listarTodos();
        if (this.estaVacio(categoria)) {
            return resultado;
        }
        String buscada = categoria.trim();
        return resultado.stream()
                .filter(v -> buscada.equalsIgnoreCase(String.valueOf(v.getCategoria())))
                .collect(Collectors.toList());
    }

    /**
     * Filtra el inventario por condicion comercial.
     *
     * @param condicion condicion deseada; si es nula se devuelve todo el inventario
     * @return lista de vehiculos con la condicion indicada
     */
    public List<Vehiculo> filtrarPorCondicion(CondicionVehiculo condicion) {
        List<Vehiculo> resultado = this.listarTodos();
        if (condicion == null) {
            return resultado;
        }
        return resultado.stream()
                .filter(v -> condicion.equals(v.getCondicion()))
                .collect(Collectors.toList());
    }

    /**
     * Filtra el inventario por un rango inclusivo de anios de fabricacion.
     *
     * @param anioDesde anio minimo del rango
     * @param anioHasta anio maximo del rango
     * @return lista de vehiculos cuyo anio cae dentro del rango
     * @throws DatoInvalidoException si algun limite es nulo, esta fuera de rango o desde es mayor que hasta
     */
    public List<Vehiculo> filtrarPorRangoAnio(Integer anioDesde, Integer anioHasta)
            throws DatoInvalidoException {
        this.validarRangoAnios(anioDesde, anioHasta);
        return this.listarTodos().stream()
                .filter(v -> v.getAnio() != null
                        && v.getAnio().intValue() >= anioDesde.intValue()
                        && v.getAnio().intValue() <= anioHasta.intValue())
                .collect(Collectors.toList());
    }

    /**
     * Aplica varios filtros simultaneamente. Los criterios nulos o vacios se ignoran.
     *
     * @param marca      marca buscada, o {@code null} para no filtrar
     * @param categoria  categoria buscada, o {@code null} para no filtrar
     * @param condicion  condicion buscada, o {@code null} para no filtrar
     * @param anioDesde  anio minimo, o {@code null} para no filtrar
     * @param anioHasta  anio maximo, o {@code null} para no filtrar
     * @return lista de vehiculos que cumplen todos los criterios indicados
     * @throws DatoInvalidoException si el rango de anios es inconsistente
     */
    public List<Vehiculo> aplicarFiltros(String marca, String categoria, CondicionVehiculo condicion,
                                         Integer anioDesde, Integer anioHasta)
            throws DatoInvalidoException {
        Integer desde = (anioDesde == null) ? Integer.valueOf(Integer.MIN_VALUE) : anioDesde;
        Integer hasta = (anioHasta == null) ? Integer.valueOf(Integer.MAX_VALUE) : anioHasta;
        if (anioDesde != null || anioHasta != null) {
            this.validarRangoAnios(anioDesde, anioHasta);
        }
        String marcaBuscada = this.estaVacio(marca) ? null : marca.trim().toLowerCase(Locale.ROOT);
        String categoriaBuscada = this.estaVacio(categoria) ? null : categoria.trim();
        return this.listarTodos().stream()
                .filter(v -> marcaBuscada == null || (v.getMarca() != null
                        && v.getMarca().toLowerCase(Locale.ROOT).contains(marcaBuscada)))
                .filter(v -> categoriaBuscada == null
                        || categoriaBuscada.equalsIgnoreCase(String.valueOf(v.getCategoria())))
                .filter(v -> condicion == null || condicion.equals(v.getCondicion()))
                .filter(v -> v.getAnio() != null
                        && v.getAnio().intValue() >= desde.intValue()
                        && v.getAnio().intValue() <= hasta.intValue())
                .collect(Collectors.toList());
    }

    /**
     * Devuelve la cantidad total de vehiculos del inventario.
     *
     * @return cantidad de vehiculos registrados
     */
    public Integer contarVehiculos() {
        return Integer.valueOf(this.inventario.size());
    }

    /**
     * Suma el precio final de todo el inventario usando el contrato {@code Cotizable}.
     *
     * @return valor total del stock con impuestos y recargos aplicados
     */
    public Double valorTotalInventario() {
        // reduce con identidad 0.0: si el inventario esta vacio devuelve cero.
        return this.inventario.values().stream()
                .map(Vehiculo::calcularPrecioFinal)
                .reduce(Double.valueOf(0.0), (total, precio) -> Double.valueOf(total + precio));
    }

    /**
     * Lista los vehiculos que requieren service segun el contrato {@code Mantenible}.
     *
     * @return lista de vehiculos con mantenimiento pendiente
     */
    public List<Vehiculo> listarConServicePendiente() {
        return this.listarTodos().stream()
                .filter(v -> Boolean.TRUE.equals(v.requiereService()))
                .collect(Collectors.toList());
    }

    /**
     * Cuenta cuantos vehiculos hay por cada condicion comercial.
     *
     * @return mapa con NUEVO y USADO, incluso cuando alguna categoria no tiene vehiculos
     */
    public Map<CondicionVehiculo, Long> contarPorCondicion() {
        Map<CondicionVehiculo, Long> conteo = new EnumMap<>(CondicionVehiculo.class);
        // Se inicializan ambas claves para que el reporte siempre muestre los dos valores.
        conteo.put(CondicionVehiculo.NUEVO, Long.valueOf(0L));
        conteo.put(CondicionVehiculo.USADO, Long.valueOf(0L));
        for (Vehiculo vehiculo : this.inventario.values()) {
            CondicionVehiculo condicion = vehiculo.getCondicion();
            if (condicion != null) {
                conteo.put(condicion, Long.valueOf(conteo.get(condicion).longValue() + 1L));
            }
        }
        return conteo;
    }

    /**
     * Valida todas las reglas de negocio de un vehiculo antes de registrarlo o modificarlo.
     *
     * @param vehiculo vehiculo a validar
     * @throws DatoInvalidoException si algun dato falta o contradice las reglas de la concesionaria
     */
    private void validarVehiculo(Vehiculo vehiculo) throws DatoInvalidoException {
        if (vehiculo == null) {
            throw new DatoInvalidoException("El vehiculo a validar no puede ser nulo.");
        }
        if (this.estaVacio(vehiculo.getIdentificador())) {
            throw new DatoInvalidoException("El identificador es obligatorio.");
        }
        if (this.estaVacio(vehiculo.getMarca()) || this.estaVacio(vehiculo.getModelo())
                || this.estaVacio(vehiculo.getColor())) {
            throw new DatoInvalidoException("Marca, modelo y color son datos obligatorios.");
        }
        Integer anio = vehiculo.getAnio();
        if (anio == null || anio.intValue() < ANIO_MINIMO.intValue()
                || anio.intValue() > this.anioActual.intValue() + 1) {
            throw new DatoInvalidoException("El anio debe estar entre " + ANIO_MINIMO + " y "
                    + (this.anioActual.intValue() + 1) + ".");
        }
        Double precio = vehiculo.getPrecioBase();
        if (precio == null || precio.doubleValue() <= 0.0) {
            throw new DatoInvalidoException("El precio base debe ser mayor que cero.");
        }
        Integer kilometraje = vehiculo.getKilometraje();
        if (kilometraje == null || kilometraje.intValue() < 0) {
            throw new DatoInvalidoException("El kilometraje no puede ser negativo ni nulo.");
        }
        CondicionVehiculo condicion = vehiculo.getCondicion();
        if (condicion == null) {
            throw new DatoInvalidoException("La condicion del vehiculo es obligatoria.");
        }
        // Regla central: NUEVO implica 0 km, USADO implica kilometraje positivo.
        if (CondicionVehiculo.NUEVO.equals(condicion) && kilometraje.intValue() != 0) {
            throw new DatoInvalidoException("Un vehiculo NUEVO debe tener 0 kilometros.");
        }
        if (CondicionVehiculo.USADO.equals(condicion) && kilometraje.intValue() == 0) {
            throw new DatoInvalidoException("Un vehiculo USADO debe tener mas de 0 kilometros.");
        }
        this.validarDetalleEspecifico(vehiculo);
    }

    /**
     * Valida los atributos propios de cada subclase concreta.
     *
     * @param vehiculo vehiculo cuyo detalle especifico se valida
     * @throws DatoInvalidoException si falta la carroceria, el tipo de moto o la cilindrada
     */
    private void validarDetalleEspecifico(Vehiculo vehiculo) throws DatoInvalidoException {
        if (vehiculo instanceof Automovil) {
            if (((Automovil) vehiculo).getTipoCarroceria() == null) {
                throw new DatoInvalidoException("El automovil requiere un tipo de carroceria.");
            }
        } else if (vehiculo instanceof Camioneta) {
            if (((Camioneta) vehiculo).getTipoCarroceria() == null) {
                throw new DatoInvalidoException("La camioneta requiere un tipo de carroceria.");
            }
        } else if (vehiculo instanceof Motocicleta) {
            Motocicleta motocicleta = (Motocicleta) vehiculo;
            if (motocicleta.getTipoMotocicleta() == null) {
                throw new DatoInvalidoException("La motocicleta requiere un tipo.");
            }
            if (motocicleta.getCilindrada() == null || motocicleta.getCilindrada().intValue() <= 0) {
                throw new DatoInvalidoException("La cilindrada debe ser mayor que cero.");
            }
        } else {
            throw new DatoInvalidoException("Tipo de vehiculo no soportado.");
        }
    }

    /**
     * Valida que los limites del rango de anios sean coherentes.
     *
     * @param anioDesde anio minimo, puede ser nulo cuando no se filtra
     * @param anioHasta anio maximo, puede ser nulo cuando no se filtra
     * @throws DatoInvalidoException si los limites estan fuera de rango o invertidos
     */
    private void validarRangoAnios(Integer anioDesde, Integer anioHasta) throws DatoInvalidoException {
        if (anioDesde == null || anioHasta == null) {
            throw new DatoInvalidoException("Debe indicar el anio desde y el anio hasta.");
        }
        if (anioDesde.intValue() < ANIO_MINIMO.intValue() || anioHasta.intValue() < ANIO_MINIMO.intValue()) {
            throw new DatoInvalidoException("Los anios deben ser mayores o iguales a " + ANIO_MINIMO + ".");
        }
        if (anioDesde.intValue() > anioHasta.intValue()) {
            throw new DatoInvalidoException("El anio desde no puede ser mayor que el anio hasta.");
        }
    }

    /**
     * Indica si un texto es nulo o no contiene caracteres utiles.
     *
     * @param valor texto a evaluar; puede ser nulo
     * @return {@code Boolean.TRUE} si el texto es nulo, vacio o solo espacios
     */
    private Boolean estaVacio(String valor) {
        return Boolean.valueOf(valor == null || valor.trim().isEmpty());
    }
}
