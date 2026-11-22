package com.concesionaria.vista;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
import com.concesionaria.servicio.ConcesionariaService;

/**
 * Interfaz de consola de la concesionaria.
 * <p>
 * Ofrece altas, bajas, modificaciones, busquedas, listado general y reportes
 * para directivos. Solo se comunica con {@link ConcesionariaService}: no accede
 * a la capa de persistencia ni implementa reglas de negocio. Todo mensaje de error
 * se obtiene de las excepciones que lanza el servicio.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class MenuConsola {

    /** Opciones de tipo de vehiculo exclusivas de esta pantalla. */
    private enum OpcionVehiculo {
        /** Alta de un automovil. */
        AUTOMOVIL,
        /** Alta de una camioneta. */
        CAMIONETA,
        /** Alta de una motocicleta. */
        MOTOCICLETA
    }

    /** Servicio con el que se comunica esta vista. */
    private final ConcesionariaService servicio;

    /** Utilidad de lectura por consola. */
    private final ConsolaHelper entrada;

    /**
     * Crea el menu de consola con sus dependencias ya inicializadas.
     *
     * @param servicio servicio de negocio a utilizar; no puede ser nulo
     * @param entrada  helper de entrada por consola; no puede ser nulo
     * @throws IllegalArgumentException si alguna dependencia es nula
     */
    public MenuConsola(ConcesionariaService servicio, ConsolaHelper entrada) {
        if (servicio == null || entrada == null) {
            throw new IllegalArgumentException("El servicio y el helper de consola son obligatorios.");
        }
        this.servicio = servicio;
        this.entrada = entrada;
    }

    /**
     * Inicia el ciclo de vida del menu: carga el stock y atiende las opciones
     * del usuario hasta que elige salir.
     */
    public void iniciar() {
        this.cargarStockInicial();
        Boolean salir = Boolean.FALSE;
        while (!salir.booleanValue()) {
            this.mostrarMenuPrincipal();
            Integer opcion = this.entrada.leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion.intValue()) {
                    case 1:
                        this.darAlta();
                        break;
                    case 2:
                        this.darBaja();
                        break;
                    case 3:
                        this.modificar();
                        break;
                    case 4:
                        this.buscar();
                        break;
                    case 5:
                        this.listarGeneral();
                        break;
                    case 6:
                        this.mostrarReportes();
                        break;
                    case 0:
                        salir = Boolean.TRUE;
                        break;
                    default:
                        System.out.println("Opcion invalida. Intente nuevamente.");
                        break;
                }
            } catch (DatoInvalidoException | VehiculoDuplicadoException
                    | VehiculoNoEncontradoException | IOException e) {
                // La vista solo informa el mensaje; la decision vino del servicio.
                System.out.println("Error: " + e.getMessage());
            }
            if (!salir.booleanValue()) {
                this.entrada.pausar();
            }
        }
        System.out.println("Gracias por utilizar el sistema de gestion de la concesionaria.");
        this.entrada.cerrar();
    }

    /**
     * Muestra el menu principal con todas las operaciones disponibles.
     */
    private void mostrarMenuPrincipal() {
        System.out.println();
        System.out.println("===== CONCESIONARIA MULTIMARCAS =====");
        System.out.println("1) Alta de vehiculo");
        System.out.println("2) Baja de vehiculo");
        System.out.println("3) Modificacion de vehiculo");
        System.out.println("4) Busqueda por identificador");
        System.out.println("5) Listado general");
        System.out.println("6) Reportes y filtros para directivos");
        System.out.println("0) Salir");
    }

    /**
     * Carga el stock desde la capa de servicio al iniciar la aplicacion.
     */
    private void cargarStockInicial() {
        try {
            this.servicio.cargarStock();
            System.out.println("Stock cargado. Vehiculos registrados: " + this.servicio.contarVehiculos());
        } catch (IOException | DatoInvalidoException e) {
            System.out.println("No se pudo cargar el stock inicial: " + e.getMessage());
        }
    }

    /**
     * Solicita los datos de un vehiculo nuevo y lo da de alta.
     *
     * @throws IOException                si el servicio no puede persistir el alta
     * @throws DatoInvalidoException      si el usuario ingresa datos que violan las reglas de negocio
     * @throws VehiculoDuplicadoException si el identificador ya existe
     */
    private void darAlta() throws IOException, DatoInvalidoException, VehiculoDuplicadoException {
        System.out.println("--- ALTA DE VEHICULO ---");
        Vehiculo vehiculo = this.solicitarVehiculo(null);
        this.servicio.agregarVehiculo(vehiculo);
        System.out.println("Vehiculo registrado correctamente: " + vehiculo.getIdentificador());
    }

    /**
     * Solicita un identificador, muestra el vehiculo y pide confirmacion para borrarlo.
     *
     * @throws IOException                  si el servicio no puede persistir la baja
     * @throws DatoInvalidoException        si el identificador esta vacio
     * @throws VehiculoNoEncontradoException si el identificador no existe
     */
    private void darBaja()
            throws IOException, DatoInvalidoException, VehiculoNoEncontradoException {
        System.out.println("--- BAJA DE VEHICULO ---");
        String identificador = this.entrada.leerTexto("Identificador a dar de baja: ");
        Vehiculo vehiculo = this.servicio.buscarPorId(identificador);
        this.mostrarVehiculo(vehiculo);
        if (this.entrada.leerBooleano("Confirma la baja").booleanValue()) {
            this.servicio.eliminarVehiculo(identificador);
            System.out.println("Vehiculo eliminado correctamente.");
        } else {
            System.out.println("Operacion cancelada.");
        }
    }

    /**
     * Busca un vehiculo, muestra sus datos y solicita los nuevos valores.
     *
     * @throws IOException                  si el servicio no puede persistir la modificacion
     * @throws DatoInvalidoException        si los nuevos datos violan las reglas de negocio
     * @throws VehiculoNoEncontradoException si el identificador no existe
     */
    private void modificar()
            throws IOException, DatoInvalidoException, VehiculoNoEncontradoException {
        System.out.println("--- MODIFICACION DE VEHICULO ---");
        String identificador = this.entrada.leerTexto("Identificador del vehiculo a modificar: ");
        Vehiculo original = this.servicio.buscarPorId(identificador);
        this.mostrarVehiculo(original);
        Vehiculo modificado = this.solicitarModificacion(original);
        this.servicio.modificarVehiculo(modificado);
        System.out.println("Vehiculo modificado correctamente.");
    }

    /**
     * Busca y muestra un vehiculo por su identificador unico.
     *
     * @throws DatoInvalidoException        si el identificador esta vacio
     * @throws VehiculoNoEncontradoException si el identificador no existe
     */
    private void buscar() throws DatoInvalidoException, VehiculoNoEncontradoException {
        System.out.println("--- BUSQUEDA POR IDENTIFICADOR ---");
        String identificador = this.entrada.leerTexto("Identificador a buscar: ");
        this.mostrarVehiculo(this.servicio.buscarPorId(identificador));
    }

    /**
     * Muestra todos los vehiculos registrados en el inventario.
     */
    private void listarGeneral() {
        System.out.println("--- LISTADO GENERAL ---");
        this.mostrarListado(this.servicio.listarTodos());
    }

    /**
     * Muestra el submenu de reportes y aplica el filtro elegido.
     *
     * @throws DatoInvalidoException si los parametros del filtro son inconsistentes
     */
    private void mostrarReportes() throws DatoInvalidoException {
        System.out.println();
        System.out.println("--- REPORTES Y FILTROS ---");
        System.out.println("1) Filtrar por marca");
        System.out.println("2) Filtrar por tipo de vehiculo");
        System.out.println("3) Filtrar por condicion");
        System.out.println("4) Filtrar por rango de anio");
        System.out.println("5) Filtro combinado");
        System.out.println("6) Valor total del inventario");
        System.out.println("7) Vehiculos con service pendiente");
        System.out.println("8) Cantidad por condicion");
        System.out.println("0) Volver al menu principal");
        Integer opcion = this.entrada.leerEntero("Seleccione un reporte: ");
        switch (opcion.intValue()) {
            case 1:
                String marca = this.entrada.leerTexto("Marca a buscar: ");
                this.mostrarListado(this.servicio.filtrarPorMarca(marca));
                break;
            case 2:
                String categoria = this.entrada.leerTexto("Tipo (Automovil/Camioneta/Motocicleta): ");
                this.mostrarListado(this.servicio.filtrarPorCategoria(categoria));
                break;
            case 3:
                CondicionVehiculo condicion = this.entrada.leerEnum("Condicion: ", CondicionVehiculo.class);
                this.mostrarListado(this.servicio.filtrarPorCondicion(condicion));
                break;
            case 4:
                Integer desde = this.entrada.leerEntero("Anio desde: ");
                Integer hasta = this.entrada.leerEntero("Anio hasta: ");
                this.mostrarListado(this.servicio.filtrarPorRangoAnio(desde, hasta));
                break;
            case 5:
                this.mostrarFiltroCombinado();
                break;
            case 6:
                System.out.println("Valor total del inventario: $" + this.servicio.valorTotalInventario());
                break;
            case 7:
                this.mostrarListado(this.servicio.listarConServicePendiente());
                break;
            case 8:
                this.mostrarConteoPorCondicion();
                break;
            case 0:
                break;
            default:
                System.out.println("Opcion invalida.");
                break;
        }
    }

    /**
     * Compone un filtro combinado; ENTER en un campo significa "no filtrar".
     *
     * @throws DatoInvalidoException si el rango de anios indicado es inconsistente
     */
    private void mostrarFiltroCombinado() throws DatoInvalidoException {
        String marca = this.entrada.leerTextoOpcional("Marca (ENTER para todos): ");
        String categoria = this.entrada.leerTextoOpcional("Tipo (ENTER para todos): ");
        CondicionVehiculo condicion = null;
        if (this.entrada.leerBooleano("Filtrar por condicion").booleanValue()) {
            condicion = this.entrada.leerEnum("Condicion: ", CondicionVehiculo.class);
        }
        Integer desde = this.entrada.leerEntero("Anio desde (0 para sin limite): ");
        Integer hasta = this.entrada.leerEntero("Anio hasta (0 para sin limite): ");
        this.mostrarListado(this.servicio.aplicarFiltros(marca, categoria, condicion,
                desde.intValue() == 0 ? null : desde, hasta.intValue() == 0 ? null : hasta));
    }

    /**
     * Muestra el resultado del reporte de cantidades por condicion.
     */
    private void mostrarConteoPorCondicion() {
        Map<CondicionVehiculo, Long> conteo = this.servicio.contarPorCondicion();
        for (Map.Entry<CondicionVehiculo, Long> entrada : conteo.entrySet()) {
            System.out.println(entrada.getKey() + ": " + entrada.getValue() + " vehiculo(s)");
        }
    }

    /**
     * Solicita todos los datos necesarios para construir un vehiculo.
     *
     * @param identificador identificador ya conocido, o {@code null} si debe pedirse al usuario
     * @return vehiculo nuevo armado segun la subclase elegida
     */
    private Vehiculo solicitarVehiculo(String identificador) {
        OpcionVehiculo opcion = this.entrada.leerEnum("Tipo de vehiculo: ", OpcionVehiculo.class);
        String id = (identificador == null)
                ? this.entrada.leerTexto("Identificador (patente o chasis): ") : identificador;
        String marca = this.entrada.leerTexto("Marca: ");
        String modelo = this.entrada.leerTexto("Modelo: ");
        Integer anio = this.entrada.leerEntero("Anio de fabricacion: ");
        String color = this.entrada.leerTexto("Color: ");
        Integer kilometraje = this.entrada.leerEntero("Kilometraje: ");
        Double precioBase = this.entrada.leerDecimal("Precio base: ");
        CondicionVehiculo condicion = this.entrada.leerEnum("Condicion: ", CondicionVehiculo.class);
        switch (opcion) {
            case AUTOMOVIL:
                TipoCarroceria carroceriaAuto = this.entrada.leerEnum("Carroceria: ", TipoCarroceria.class);
                return new Automovil(id, marca, modelo, anio, color, kilometraje,
                        precioBase, condicion, carroceriaAuto);
            case CAMIONETA:
                TipoCarroceria carroceriaCamioneta = this.entrada.leerEnum("Carroceria: ", TipoCarroceria.class);
                return new Camioneta(id, marca, modelo, anio, color, kilometraje,
                        precioBase, condicion, carroceriaCamioneta);
            case MOTOCICLETA:
                TipoMotocicleta tipoMoto = this.entrada.leerEnum("Tipo de motocicleta: ", TipoMotocicleta.class);
                Integer cilindrada = this.entrada.leerEntero("Cilindrada (cc): ");
                return new Motocicleta(id, marca, modelo, anio, color, kilometraje,
                        precioBase, condicion, tipoMoto, cilindrada);
            default:
                throw new IllegalStateException("Tipo de vehiculo no contemplado en el alta.");
        }
    }

    /**
     * Solicita los nuevos datos de un vehiculo existente, conservando su tipo y su identificador.
     *
     * @param original vehiculo que se esta modificando
     * @return nueva instancia con los datos actualizados
     */
    private Vehiculo solicitarModificacion(Vehiculo original) {
        String id = original.getIdentificador();
        String marca = this.entrada.leerTexto("Marca (" + original.getMarca() + "): ");
        String modelo = this.entrada.leerTexto("Modelo (" + original.getModelo() + "): ");
        Integer anio = this.entrada.leerEntero("Anio de fabricacion (" + original.getAnio() + "): ");
        String color = this.entrada.leerTexto("Color (" + original.getColor() + "): ");
        Integer kilometraje = this.entrada.leerEntero("Kilometraje (" + original.getKilometraje() + "): ");
        Double precioBase = this.entrada.leerDecimal("Precio base (" + original.getPrecioBase() + "): ");
        CondicionVehiculo condicion = this.entrada.leerEnum("Condicion actual: ", CondicionVehiculo.class);
        if (original instanceof Automovil) {
            TipoCarroceria carroceria = this.entrada.leerEnum("Carroceria: ", TipoCarroceria.class);
            return new Automovil(id, marca, modelo, anio, color, kilometraje,
                    precioBase, condicion, carroceria);
        }
        if (original instanceof Camioneta) {
            TipoCarroceria carroceria = this.entrada.leerEnum("Carroceria: ", TipoCarroceria.class);
            return new Camioneta(id, marca, modelo, anio, color, kilometraje,
                    precioBase, condicion, carroceria);
        }
        if (original instanceof Motocicleta) {
            TipoMotocicleta tipoMoto = this.entrada.leerEnum("Tipo de motocicleta: ", TipoMotocicleta.class);
            Integer cilindrada = this.entrada.leerEntero("Cilindrada (cc): ");
            return new Motocicleta(id, marca, modelo, anio, color, kilometraje,
                    precioBase, condicion, tipoMoto, cilindrada);
        }
        throw new IllegalStateException("Tipo de vehiculo no contemplado en la modificacion.");
    }

    /**
     * Muestra el detalle completo de un vehiculo, incluyendo precio final y service.
     *
     * @param vehiculo vehiculo a mostrar
     */
    private void mostrarVehiculo(Vehiculo vehiculo) {
        System.out.println(vehiculo);
        System.out.println("Categoria: " + vehiculo.getCategoria());
        System.out.println("Detalle especifico: " + vehiculo.obtenerDetalleEspecifico());
        System.out.println("Precio final: $" + vehiculo.calcularPrecioFinal());
        System.out.println("Requiere service: "
                + (Boolean.TRUE.equals(vehiculo.requiereService()) ? "SI" : "NO"));
    }

    /**
     * Muestra una lista resumida de vehiculos.
     *
     * @param vehiculos lista a mostrar; puede estar vacia
     */
    private void mostrarListado(List<Vehiculo> vehiculos) {
        if (vehiculos.isEmpty()) {
            System.out.println("No se encontraron vehiculos para los criterios indicados.");
            return;
        }
        System.out.printf("%-16s | %-12s | %-12s | %6s | %-8s | %-12s | %12s%n",
                "IDENTIFICADOR", "MARCA", "MODELO", "ANIO", "CONDICION", "TIPO", "PRECIO FINAL");
        for (Vehiculo vehiculo : vehiculos) {
            System.out.printf("%-16s | %-12s | %-12s | %6s | %-8s | %-12s | %12.2f%n",
                    vehiculo.getIdentificador(), vehiculo.getMarca(), vehiculo.getModelo(),
                    vehiculo.getAnio(), vehiculo.getCondicion(), vehiculo.getCategoria(),
                    vehiculo.calcularPrecioFinal());
        }
        System.out.println("Total de resultados: " + vehiculos.size());
    }
}
