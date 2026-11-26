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

public class ConcesionariaService {

    private static final Integer ANIO_MINIMO = Integer.valueOf(1900);

    private final Map<String, Vehiculo> inventario;

    private final RepositorioGenerico<Vehiculo, String> repositorio;

    private final Integer anioActual;

    public ConcesionariaService(RepositorioGenerico<Vehiculo, String> repositorio) {
        this(repositorio, Integer.valueOf(Year.now().getValue()));
    }

    public ConcesionariaService(RepositorioGenerico<Vehiculo, String> repositorio, Integer anioActual) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio de persistencia es obligatorio.");
        }
        this.repositorio = repositorio;
        this.anioActual = (anioActual == null) ? Integer.valueOf(Year.now().getValue()) : anioActual;

        this.inventario = new LinkedHashMap<>();
    }

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

    public void agregarVehiculo(Vehiculo vehiculo)
            throws IOException, DatoInvalidoException, VehiculoDuplicadoException {
        this.validarVehiculo(vehiculo);
        String identificador = vehiculo.getIdentificador().trim();
        if (this.inventario.containsKey(identificador)) {
            throw new VehiculoDuplicadoException(identificador);
        }

        vehiculo.setIdentificador(identificador);
        this.repositorio.guardar(vehiculo);
        this.inventario.put(identificador, vehiculo);
    }

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

    public List<Vehiculo> listarTodos() {

        return this.inventario.values().stream()
                .sorted((a, b) -> String.valueOf(a.getIdentificador())
                        .compareTo(String.valueOf(b.getIdentificador())))
                .collect(Collectors.toList());
    }

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

    public List<Vehiculo> filtrarPorCondicion(CondicionVehiculo condicion) {
        List<Vehiculo> resultado = this.listarTodos();
        if (condicion == null) {
            return resultado;
        }
        return resultado.stream()
                .filter(v -> condicion.equals(v.getCondicion()))
                .collect(Collectors.toList());
    }

    public List<Vehiculo> filtrarPorRangoAnio(Integer anioDesde, Integer anioHasta)
            throws DatoInvalidoException {
        this.validarRangoAnios(anioDesde, anioHasta);
        return this.listarTodos().stream()
                .filter(v -> v.getAnio() != null
                        && v.getAnio().intValue() >= anioDesde.intValue()
                        && v.getAnio().intValue() <= anioHasta.intValue())
                .collect(Collectors.toList());
    }

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

    public Integer contarVehiculos() {
        return Integer.valueOf(this.inventario.size());
    }

    public Double valorTotalInventario() {

        return this.inventario.values().stream()
                .map(Vehiculo::calcularPrecioFinal)
                .reduce(Double.valueOf(0.0), (total, precio) -> Double.valueOf(total + precio));
    }

    public List<Vehiculo> listarConServicePendiente() {
        return this.listarTodos().stream()
                .filter(v -> Boolean.TRUE.equals(v.requiereService()))
                .collect(Collectors.toList());
    }

    public Map<CondicionVehiculo, Long> contarPorCondicion() {
        Map<CondicionVehiculo, Long> conteo = new EnumMap<>(CondicionVehiculo.class);

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

        if (CondicionVehiculo.NUEVO.equals(condicion) && kilometraje.intValue() != 0) {
            throw new DatoInvalidoException("Un vehiculo NUEVO debe tener 0 kilometros.");
        }
        if (CondicionVehiculo.USADO.equals(condicion) && kilometraje.intValue() == 0) {
            throw new DatoInvalidoException("Un vehiculo USADO debe tener mas de 0 kilometros.");
        }
        this.validarDetalleEspecifico(vehiculo);
    }

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

    private Boolean estaVacio(String valor) {
        return Boolean.valueOf(valor == null || valor.trim().isEmpty());
    }
}
