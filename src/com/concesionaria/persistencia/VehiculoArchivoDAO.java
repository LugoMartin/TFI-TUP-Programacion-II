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

/**
 * Implementacion de {@link RepositorioGenerico} que persiste el stock en un archivo
 * de texto plano con formato CSV delimitado por punto y coma.
 * <p>
 * Cada linea representa un vehiculo y contiene 11 campos en este orden:
 * identificador, codigo de tipo, marca, modelo, anio, color, kilometraje,
 * precioBase, condicion, detalle (carroceria o tipo de moto) y cilindrada.
 * El punto y coma se usa como separador para que las comas de marcas o modelos
 * no rompan el formato; los caracteres especiales se escapan con contrabarra.
 * </p>
 * <p>
 * La lectura y escritura se realizan con {@link BufferedReader} y
 * {@link BufferedWriter}; la conversion de tipos se hace con los metodos
 * estaticos de las clases Wrapper ({@code Integer.parseInt},
 * {@code Double.parseDouble}, {@code Boolean.valueOf}).
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class VehiculoArchivoDAO implements RepositorioGenerico<Vehiculo, String> {

    /** Nombre del archivo usado cuando no se indica una ruta. */
    private static final String ARCHIVO_PREDETERMINADO = "stock_vehiculos.csv";

    /** Separador de campos dentro de cada registro. */
    private static final String SEPARADOR = ";";

    /** Caracter de escape para el separador y la propia contrabarra. */
    private static final String ESCAPE = "\\";

    /** Expresion regular que separa por ';' solo cuando no esta escapado. */
    private static final String PATRON_SEPARADOR = "(?<!\\\\)" + SEPARADOR;

    /** Cantidad de campos que debe tener cada linea valida. */
    private static final int CANTIDAD_CAMPOS = 11;

    /** Codigo de tipo usado en el archivo para un automovil. */
    private static final String CODIGO_AUTOMOVIL = "AUTOMOVIL";

    /** Codigo de tipo usado en el archivo para una camioneta. */
    private static final String CODIGO_CAMIONETA = "CAMIONETA";

    /** Codigo de tipo usado en el archivo para una motocicleta. */
    private static final String CODIGO_MOTOCICLETA = "MOTOCICLETA";

    /** Ruta del archivo que contiene el stock persistido. */
    private final String rutaArchivo;

    /**
     * Crea el DAO usando el archivo por defecto {@code stock_vehiculos.csv}.
     */
    public VehiculoArchivoDAO() {
        this(ARCHIVO_PREDETERMINADO);
    }

    /**
     * Crea el DAO con una ruta de archivo personalizada.
     *
     * @param rutaArchivo ruta del archivo de stock; si es nula o vacia se usa la ruta por defecto
     */
    public VehiculoArchivoDAO(String rutaArchivo) {
        // Se aplica un valor por defecto para mantener el constructor nulo-seguro.
        this.rutaArchivo = (rutaArchivo == null || rutaArchivo.isBlank())
                ? ARCHIVO_PREDETERMINADO : rutaArchivo;
    }

    /**
     * Devuelve la ruta del archivo utilizado por el DAO.
     *
     * @return ruta completa o relativa del archivo de stock
     */
    public String getRutaArchivo() {
        return this.rutaArchivo;
    }

    /**
     * Agrega un vehiculo al final del archivo si su identificador no existe.
     *
     * @param entidad vehiculo a registrar
     * @throws IOException                si no puede escribirse el archivo
     * @throws DatoInvalidoException      si el vehiculo es nulo o le faltan datos obligatorios
     * @throws VehiculoDuplicadoException si el identificador ya esta registrado
     */
    @Override
    public void guardar(Vehiculo entidad)
            throws IOException, DatoInvalidoException, VehiculoDuplicadoException {
        if (entidad == null) {
            throw new DatoInvalidoException("No se puede guardar un vehiculo nulo.");
        }
        // Se valida la unicidad antes de escribir para mantener el archivo consistente.
        if (this.existe(entidad.getIdentificador())) {
            throw new VehiculoDuplicadoException(entidad.getIdentificador());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.rutaArchivo, true))) {
            writer.write(this.convertirALinea(entidad));
            writer.newLine();
        }
    }

    /**
     * Busca un vehiculo por su identificador recorriendo el archivo completo.
     *
     * @param id identificador unico buscado
     * @return el vehiculo encontrado
     * @throws IOException                  si no puede leerse el archivo
     * @throws DatoInvalidoException        si alguna linea del archivo es invalida
     * @throws VehiculoNoEncontradoException si no hay ningun vehiculo con ese identificador
     */
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

    /**
     * Lee todas las lineas del archivo y las convierte en objetos del dominio.
     *
     * @return lista de vehiculos persistidos; vacia si el archivo no existe o esta vacio
     * @throws IOException           si no puede leerse el archivo
     * @throws DatoInvalidoException si alguna linea tiene un formato o un valor invalido
     */
    @Override
    public List<Vehiculo> listarTodos() throws IOException, DatoInvalidoException {
        List<Vehiculo> vehiculos = new ArrayList<>();
        // Si el archivo aun no existe, el inventario persistido se considera vacio.
        if (!Files.exists(Paths.get(this.rutaArchivo))) {
            return vehiculos;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(this.rutaArchivo))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue; // Se ignoran lineas en blanco dejadas por ediciones manuales.
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

    /**
     * Reemplaza el registro con el mismo identificador por los datos actualizados.
     *
     * @param entidad vehiculo con los datos modificados
     * @throws IOException                  si no puede escribirse el archivo
     * @throws DatoInvalidoException        si el vehiculo es nulo o le faltan datos obligatorios
     * @throws VehiculoNoEncontradoException si el identificador no esta registrado
     */
    @Override
    public void actualizar(Vehiculo entidad)
            throws IOException, DatoInvalidoException, VehiculoNoEncontradoException {
        if (entidad == null) {
            throw new DatoInvalidoException("No se puede actualizar un vehiculo nulo.");
        }
        List<Vehiculo> vehiculos = this.listarTodos();
        boolean encontrado = false;
        // Se reconstruye la coleccion para reescribir el archivo con una sola pasada.
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

    /**
     * Quita del archivo el registro cuyo identificador coincide con el parametro.
     *
     * @param id identificador del vehiculo a eliminar
     * @throws IOException                  si no puede escribirse el archivo
     * @throws DatoInvalidoException        si el contenido persistido es invalido
     * @throws VehiculoNoEncontradoException si el identificador no esta registrado
     */
    @Override
    public void eliminar(String id)
            throws IOException, DatoInvalidoException, VehiculoNoEncontradoException {
        List<Vehiculo> vehiculos = this.listarTodos();
        // removeIf permite filtrar por identificador sin exponer la coleccion interna.
        boolean eliminado = vehiculos.removeIf(v -> Objects.equals(v.getIdentificador(), id));
        if (!eliminado) {
            throw new VehiculoNoEncontradoException(id);
        }
        this.sobrescribirArchivo(vehiculos);
    }

    /**
     * Verifica si un identificador ya fue registrado en el archivo.
     *
     * @param id identificador a verificar
     * @return {@code true} si existe un vehiculo con ese identificador
     * @throws IOException           si no puede leerse el archivo
     * @throws DatoInvalidoException si el contenido persistido es invalido
     */
    @Override
    public boolean existe(String id) throws IOException, DatoInvalidoException {
        if (id == null || id.isBlank()) {
            return false;
        }
        // anyMatch corta la lectura en cuanto encuentra la primera coincidencia.
        return this.listarTodos().stream()
                .anyMatch(v -> Objects.equals(v.getIdentificador(), id));
    }

    /**
     * Convierte un vehiculo en una linea CSV con los 11 campos acordados.
     *
     * @param vehiculo vehiculo a serializar
     * @return linea lista para escribir en el archivo
     * @throws DatoInvalidoException si el vehiculo es nulo, no tiene identificador o es de un tipo desconocido
     */
    private String convertirALinea(Vehiculo vehiculo) throws DatoInvalidoException {
        if (vehiculo == null) {
            throw new DatoInvalidoException("No se puede serializar un vehiculo nulo.");
        }
        String identificador = this.aTexto(vehiculo.getIdentificador());
        if (identificador.isBlank()) {
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

    /**
     * Convierte una linea CSV en la subclase concreta correspondiente.
     *
     * @param linea linea leida del archivo
     * @return vehiculo reconstruido con todos sus atributos
     * @throws DatoInvalidoException si la cantidad de campos o algun valor es invalido
     */
    private Vehiculo parsearLinea(String linea) throws DatoInvalidoException {
        String[] campos = linea.split(PATRON_SEPARADOR, -1);
        if (campos.length != CANTIDAD_CAMPOS) {
            throw new DatoInvalidoException("Se esperaban " + CANTIDAD_CAMPOS
                    + " campos pero se recibieron " + campos.length + ".");
        }
        // Se restaura el contenido original antes de interpretar cada campo.
        for (int i = 0; i < campos.length; i++) {
            campos[i] = this.desescapar(campos[i]);
        }
        Vehiculo vehiculo = this.crearVehiculo(campos[1]);
        vehiculo.setIdentificador(campos[0]);
        vehiculo.setMarca(campos[2]);
        vehiculo.setModelo(campos[3]);
        vehiculo.setColor(campos[5]);
        try {
            // parseInt y parseDouble devuelven primitivos que se autoboxean a Wrapper.
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

    /**
     * Instancia la subclase indicada por el codigo de tipo almacenado.
     *
     * @param codigoTipo codigo del tipo (AUTOMOVIL, CAMIONETA o MOTOCICLETA)
     * @return una instancia vacia de la subclase correspondiente
     * @throws DatoInvalidoException si el codigo de tipo no es reconocido
     */
    private Vehiculo crearVehiculo(String codigoTipo) throws DatoInvalidoException {
        // El switch de Java 17 permite devolver directamente la instancia y lanzar si no existe.
        return switch (codigoTipo) {
            case CODIGO_AUTOMOVIL -> new Automovil();
            case CODIGO_CAMIONETA -> new Camioneta();
            case CODIGO_MOTOCICLETA -> new Motocicleta();
            default -> throw new DatoInvalidoException("Codigo de tipo desconocido: " + codigoTipo);
        };
    }

    /**
     * Resuelve el codigo de tipo persistido segun la subclase concreta.
     *
     * @param vehiculo vehiculo del cual se quiere obtener el codigo
     * @return codigo de tipo almacenado en el archivo
     * @throws DatoInvalidoException si la clase del vehiculo no esta soportada
     */
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

    /**
     * Extrae el detalle especifico que se persiste en el penultimo campo.
     *
     * @param vehiculo vehiculo del cual se extrae el detalle
     * @return nombre de la carroceria o del tipo de motocicleta
     * @throws DatoInvalidoException si la clase del vehiculo no esta soportada
     */
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

    /**
     * Asigna el detalle especifico ya parseado a la subclase correspondiente.
     *
     * @param vehiculo   vehiculo al que se le completan los atributos propios
     * @param detalle    nombre de la carroceria o del tipo de motocicleta
     * @param cilindrada cilindrada en texto, vacia para automoviles y camionetas
     * @throws DatoInvalidoException si algun valor enumerado o numerico es invalido
     */
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

    /**
     * Convierte el texto de la carroceria al enum correspondiente.
     *
     * @param texto nombre del enum guardado, o cadena vacia si no habia valor
     * @return el enum {@link TipoCarroceria}, o {@code null} si el texto esta vacio
     * @throws DatoInvalidoException si el texto no coincide con ningun valor del enum
     */
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

    /**
     * Convierte el texto del tipo de motocicleta al enum correspondiente.
     *
     * @param texto nombre del enum guardado, o cadena vacia si no habia valor
     * @return el enum {@link TipoMotocicleta}, o {@code null} si el texto esta vacio
     * @throws DatoInvalidoException si el texto no coincide con ningun valor del enum
     */
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

    /**
     * Reescribe el archivo completo con la coleccion indicada.
     *
     * @param vehiculos vehiculos que deben quedar persistidos
     * @throws IOException           si no puede escribirse el archivo
     * @throws DatoInvalidoException si algun vehiculo no puede serializarse
     */
    private void sobrescribirArchivo(List<Vehiculo> vehiculos) throws IOException, DatoInvalidoException {
        // El flag false trunca el archivo para evitar registros duplicados.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.rutaArchivo, false))) {
            for (Vehiculo vehiculo : vehiculos) {
                writer.write(this.convertirALinea(vehiculo));
                writer.newLine();
            }
        }
    }

    /**
     * Escapa el separador y la contrabarra dentro de un campo de texto.
     *
     * @param texto texto original del campo
     * @return texto listo para ser escrito en el archivo
     */
    private String escapar(String texto) {
        return texto.replace(ESCAPE, ESCAPE + ESCAPE).replace(SEPARADOR, ESCAPE + SEPARADOR);
    }

    /**
     * Revierte el escape aplicado por {@link #escapar(String)}.
     *
     * @param texto texto leido del archivo
     * @return texto original del campo
     */
    private String desescapar(String texto) {
        return texto.replace(ESCAPE + SEPARADOR, SEPARADOR).replace(ESCAPE + ESCAPE, ESCAPE);
    }

    /**
     * Transforma un valor cualquiera en texto, tratando {@code null} como cadena vacia.
     *
     * @param valor valor a convertir; puede ser {@code null}
     * @return representacion textual del valor, o cadena vacia si es nulo
     */
    private String aTexto(Object valor) {
        return (valor == null) ? "" : valor.toString();
    }
}
