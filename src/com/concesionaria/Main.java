package com.concesionaria;

import com.concesionaria.modelo.Vehiculo;
import com.concesionaria.persistencia.RepositorioGenerico;
import com.concesionaria.persistencia.VehiculoArchivoDAO;
import com.concesionaria.servicio.ConcesionariaService;
import com.concesionaria.vista.ConsolaHelper;
import com.concesionaria.vista.MenuConsola;

public final class Main {

    private static final String ARCHIVO_PREDETERMINADO = "stock_vehiculos.csv";

    private Main() {

    }

    public static void main(String[] args) {
        String rutaArchivo = ARCHIVO_PREDETERMINADO;
        if (args != null && args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            rutaArchivo = args[0].trim();
        }

        RepositorioGenerico<Vehiculo, String> repositorio = new VehiculoArchivoDAO(rutaArchivo);
        ConcesionariaService servicio = new ConcesionariaService(repositorio);

        ConsolaHelper entrada = new ConsolaHelper(System.in);
        MenuConsola menu = new MenuConsola(servicio, entrada);
        menu.iniciar();
    }
}
