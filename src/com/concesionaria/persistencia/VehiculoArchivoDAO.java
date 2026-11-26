package com.concesionaria.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.concesionaria.modelo.Automovil;
import com.concesionaria.modelo.Camioneta;
import com.concesionaria.modelo.Motocicleta;
import com.concesionaria.modelo.Vehiculo;
import com.concesionaria.modelo.enums.CondicionVehiculo;
import com.concesionaria.modelo.enums.TipoCarroceria;
import com.concesionaria.modelo.enums.TipoMotocicleta;
import com.concesionaria.modelo.excepciones.DatoInvalidoException;
import com.concesionaria.modelo.excepciones.VehiculoDuplicadoException;
import com.concesionaria.modelo.excepciones.VehiculoNoEncontradoException;

public class VehiculoArchivoDAO implements RepositorioGenerico<Vehiculo, String> {

    private static final String ARCHIVO_PREDETERMINADO = "stock_vehiculos.csv";

    private static final String SEPARADOR = ";";

    private static final String ESCAPE = "\\";

    private static final String PATRON_SEPARADOR = "(?<!\\\\)" + SEPARADOR;

    private static final int CANTIDAD_CAMPOS = 11;

    private static final String CODIGO_AUTOMOVIL = "AUTOMOVIL";

    private static final String CODIGO_CAMIONETA = "CAMIONETA";

    private static final String CODIGO_MOTOCICLETA = "MOTOCICLETA";

    private final String rutaArchivo;

    public VehiculoArchivoDAO() {
        this(ARCHIVO_PREDETERMINADO);
    }

    public VehiculoArchivoDAO(String rutaArchivo) {

        this.rutaArchivo = (rutaArchivo == null || rutaArchivo.trim().isEmpty())
                ? ARCHIVO_PREDETERMINADO : rutaArchivo;
    }

    public String getRutaArchivo() {
        return this.rutaArchivo;
    }

    @Override
    public void guardar(Vehiculo entidad)
            throws IOException, DatoInvalidoException, VehiculoDuplicadoException {
        if (entidad == null) {
            throw new DatoInvalidoException("No se puede guardar un vehiculo nulo.");
        }

        if (this.existe(entidad.getIdentificador())) {
            throw new VehiculoDuplicadoException(entidad.getIdentificador());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.rutaArchivo, true))) {
            writer.write(this.convertirALinea(entidad));
            writer.newLine();
        }
    }

    @Override
    public Vehiculo buscarPorId(String id)
            throws IOException, DatoInvalidoException, VehiculoNoEncontradoException {
        for (Vehiculo vehiculo : this.listarTodos()) {
            if (Objects.equals(vehiculo.getIdentificador(), id)) {
                return vehiculo;
            }
        }
        throw new VehiculoNoEncontradoException(id);
    }

    @Override
    public List<Vehiculo> listarTodos() throws IOException, DatoInvalidoException {
        List<Vehiculo> vehiculos = new ArrayList<>();

        if (!Files.exists(Paths.get(this.rutaArchivo))) {
            return vehiculos;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(this.rutaArchivo))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                if (linea.trim().isEmpty()) {
                    continue;
                }
                try {
                    vehiculos.add(this.parsearLinea(linea));
                } catch (DatoInvalidoException e) {
                    throw new DatoInvalidoException("Error en la linea " + numeroLinea + ": " + e.getMessage());
                }
            }
        }
        return vehiculos;
    }

    @Override
    public void actualizar(Vehiculo entidad)
            throws IOException, DatoInvalidoException, VehiculoNoEncontradoException {
        if (entidad == null) {
            throw new DatoInvalidoException("No se puede actualizar un vehiculo nulo.");
        }
        List<Vehiculo> vehiculos = this.listarTodos();
        boolean encontrado = false;

        for (int i = 0; i < vehiculos.size(); i++) {
            if (Objects.equals(vehiculos.get(i).getIdentificador(), entidad.getIdentificador())) {
                vehiculos.set(i, entidad);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            throw new VehiculoNoEncontradoException(entidad.getIdentificador());
        }
        this.sobrescribirArchivo(vehiculos);
    }

    @Override
    public void eliminar(String id)
            throws IOException, DatoInvalidoException, VehiculoNoEncontradoException {
        List<Vehiculo> vehiculos = this.listarTodos();

        boolean eliminado = vehiculos.removeIf(v -> Objects.equals(v.getIdentificador(), id));
        if (!eliminado) {
            throw new VehiculoNoEncontradoException(id);
        }
        this.sobrescribirArchivo(vehiculos);
    }

    @Override
    public boolean existe(String id) throws IOException, DatoInvalidoException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        return this.listarTodos().stream()
                .anyMatch(v -> Objects.equals(v.getIdentificador(), id));
    }

    private String convertirALinea(Vehiculo vehiculo) throws DatoInvalidoException {
        if (vehiculo == null) {
            throw new DatoInvalidoException("No se puede serializar un vehiculo nulo.");
        }
        String identificador = this.aTexto(vehiculo.getIdentificador());
        if (identificador.trim().isEmpty()) {
            throw new DatoInvalidoException("El identificador del vehiculo es obligatorio.");
        }
        String cilindrada = (vehiculo instanceof Motocicleta)
                ? this.aTexto(((Motocicleta) vehiculo).getCilindrada()) : "";
        return String.join(SEPARADOR,
                this.escapar(identificador),
                this.escapar(this.resolverCodigoTipo(vehiculo)),
                this.escapar(this.aTexto(vehiculo.getMarca())),
                this.escapar(this.aTexto(vehiculo.getModelo())),
                this.escapar(this.aTexto(vehiculo.getAnio())),
                this.escapar(this.aTexto(vehiculo.getColor())),
                this.escapar(this.aTexto(vehiculo.getKilometraje())),
                this.escapar(this.aTexto(vehiculo.getPrecioBase())),
                this.escapar(this.aTexto(vehiculo.getCondicion())),
                this.escapar(this.resolverDetalle(vehiculo)),
                this.escapar(cilindrada));
    }

    private Vehiculo parsearLinea(String linea) throws DatoInvalidoException {
        String[] campos = linea.split(PATRON_SEPARADOR, -1);
        if (campos.length != CANTIDAD_CAMPOS) {
            throw new DatoInvalidoException("Se esperaban " + CANTIDAD_CAMPOS
                    + " campos pero se recibieron " + campos.length + ".");
        }

        for (int i = 0; i < campos.length; i++) {
            campos[i] = this.desescapar(campos[i]);
        }
        Vehiculo vehiculo = this.crearVehiculo(campos[1]);
        vehiculo.setIdentificador(campos[0]);
        vehiculo.setMarca(campos[2]);
        vehiculo.setModelo(campos[3]);
        vehiculo.setColor(campos[5]);
        try {

            vehiculo.setAnio(Integer.parseInt(campos[4]));
            vehiculo.setKilometraje(campos[6].isEmpty() ? null : Integer.parseInt(campos[6]));
            vehiculo.setPrecioBase(Double.parseDouble(campos[7]));
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("Valor numerico invalido en el registro '" + campos[0] + "'.");
        }
        try {
            vehiculo.setCondicion(CondicionVehiculo.valueOf(campos[8]));
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException("Condicion desconocida en el registro '" + campos[0] + "': " + campos[8]);
        }
        this.asignarDetalle(vehiculo, campos[9], campos[10]);
        return vehiculo;
    }

    private Vehiculo crearVehiculo(String codigoTipo) throws DatoInvalidoException {

        switch (codigoTipo) {
            case CODIGO_AUTOMOVIL:
                return new Automovil();
            case CODIGO_CAMIONETA:
                return new Camioneta();
            case CODIGO_MOTOCICLETA:
                return new Motocicleta();
            default:
                throw new DatoInvalidoException("Codigo de tipo desconocido: " + codigoTipo);
        }
    }

    private String resolverCodigoTipo(Vehiculo vehiculo) throws DatoInvalidoException {
        if (vehiculo instanceof Automovil) {
            return CODIGO_AUTOMOVIL;
        }
        if (vehiculo instanceof Camioneta) {
            return CODIGO_CAMIONETA;
        }
        if (vehiculo instanceof Motocicleta) {
            return CODIGO_MOTOCICLETA;
        }
        throw new DatoInvalidoException("Tipo no soportado: " + vehiculo.getClass().getSimpleName());
    }

    private String resolverDetalle(Vehiculo vehiculo) throws DatoInvalidoException {
        if (vehiculo instanceof Automovil) {
            return this.aTexto(((Automovil) vehiculo).getTipoCarroceria());
        }
        if (vehiculo instanceof Camioneta) {
            return this.aTexto(((Camioneta) vehiculo).getTipoCarroceria());
        }
        if (vehiculo instanceof Motocicleta) {
            return this.aTexto(((Motocicleta) vehiculo).getTipoMotocicleta());
        }
        throw new DatoInvalidoException("Tipo no soportado: " + vehiculo.getClass().getSimpleName());
    }

    private void asignarDetalle(Vehiculo vehiculo, String detalle, String cilindrada)
            throws DatoInvalidoException {
        if (vehiculo instanceof Automovil) {
            ((Automovil) vehiculo).setTipoCarroceria(this.parsearCarroceria(detalle));
        } else if (vehiculo instanceof Camioneta) {
            ((Camioneta) vehiculo).setTipoCarroceria(this.parsearCarroceria(detalle));
        } else if (vehiculo instanceof Motocicleta) {
            Motocicleta motocicleta = (Motocicleta) vehiculo;
            motocicleta.setTipoMotocicleta(this.parsearTipoMotocicleta(detalle));
            try {
                motocicleta.setCilindrada(cilindrada.isEmpty() ? null : Integer.parseInt(cilindrada));
            } catch (NumberFormatException e) {
                throw new DatoInvalidoException("Cilindrada invalida: " + cilindrada);
            }
        }
    }

    private TipoCarroceria parsearCarroceria(String texto) throws DatoInvalidoException {
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return TipoCarroceria.valueOf(texto);
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException("Carroceria desconocida: " + texto);
        }
    }

    private TipoMotocicleta parsearTipoMotocicleta(String texto) throws DatoInvalidoException {
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return TipoMotocicleta.valueOf(texto);
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException("Tipo de motocicleta desconocido: " + texto);
        }
    }

    private void sobrescribirArchivo(List<Vehiculo> vehiculos) throws IOException, DatoInvalidoException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.rutaArchivo, false))) {
            for (Vehiculo vehiculo : vehiculos) {
                writer.write(this.convertirALinea(vehiculo));
                writer.newLine();
            }
        }
    }

    private String escapar(String texto) {
        return texto.replace(ESCAPE, ESCAPE + ESCAPE).replace(SEPARADOR, ESCAPE + SEPARADOR);
    }

    private String desescapar(String texto) {
        return texto.replace(ESCAPE + SEPARADOR, SEPARADOR).replace(ESCAPE + ESCAPE, ESCAPE);
    }

    private String aTexto(Object valor) {
        return (valor == null) ? "" : valor.toString();
    }
}
