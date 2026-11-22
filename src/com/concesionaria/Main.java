package com.concesionaria;

import com.concesionaria.modelo.Vehiculo;
import com.concesionaria.persistencia.RepositorioGenerico;
import com.concesionaria.persistencia.VehiculoArchivoDAO;
import com.concesionaria.servicio.ConcesionariaService;
import com.concesionaria.vista.ConsolaHelper;
import com.concesionaria.vista.MenuConsola;

/**
 * Punto de entrada de la aplicacion de gestion de la concesionaria.
 * <p>
 * Actua como composicion de dependencias: crea el repositorio concreto, lo
 * inyecta en el servicio, arma la vista y delega el control al menu. De este
 * modo la vista nunca necesita conocer la capa de persistencia.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public final class Main {

    /** Ruta del archivo de stock utilizada si no se recibe un argumento. */
    private static final String ARCHIVO_PREDETERMINADO = "stock_vehiculos.csv";

    /**
     * Constructor privado: esta clase solo expone el metodo de entrada.
     */
    private Main() {
        // Se evita la instanciacion de una clase de utilidad.
    }

    /**
     * Inicializa las capas de la aplicacion y muestra el menu por consola.
     *
     * @param args argumento opcional 0: ruta del archivo CSV de stock
     */
    public static void main(String[] args) {
        String rutaArchivo = ARCHIVO_PREDETERMINADO;
        if (args != null && args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            rutaArchivo = args[0].trim();
        }

        // Se construye el DAO concreto y se inyecta en el servicio (inversion de dependencias).
        RepositorioGenerico<Vehiculo, String> repositorio = new VehiculoArchivoDAO(rutaArchivo);
        ConcesionariaService servicio = new ConcesionariaService(repositorio);

        // La vista recibe unicamente el servicio, nunca el repositorio.
        ConsolaHelper entrada = new ConsolaHelper(System.in);
        MenuConsola menu = new MenuConsola(servicio, entrada);
        menu.iniciar();
    }
}
