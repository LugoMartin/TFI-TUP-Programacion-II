package com.concesionaria.vista;

import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

/**
 * Utilidad de entrada para la interfaz de consola.
 * <p>
 * Lee siempre lineas completas con {@link Scanner#nextLine()} y convierte los
 * valores con las clases Wrapper ({@code Integer.valueOf}, {@code Double.valueOf})
 * dentro de bloques {@code try-catch}. De este modo se evita la excepcion
 * {@code InputMismatchException} que produce {@code nextInt()} o {@code nextDouble()}
 * cuando el usuario ingresa texto. Tambien ofrece seleccion numerada de enums.
 * </p>
 * <p>
 * Esta clase pertenece a la vista: no accede a persistencia ni valida reglas de negocio.
 * </p>
 *
 * @author Desarrollo TFI - Programacion II
 * @version 1.0
 */
public class ConsolaHelper {

    /** Lector de la entrada estandar o del flujo inyectado. */
    private final Scanner scanner;

    /**
     * Crea el helper leyendo desde la entrada estandar del sistema.
     */
    public ConsolaHelper() {
        this(System.in);
    }

    /**
     * Crea el helper leyendo desde un flujo indicado, util para pruebas.
     *
     * @param entrada flujo de entrada de datos; si es nulo se usa {@link System#in}
     */
    public ConsolaHelper(InputStream entrada) {
        this.scanner = new Scanner(entrada == null ? System.in : entrada);
    }

    /**
     * Muestra un mensaje y lee una linea completa sin procesarla.
     *
     * @param mensaje texto que se muestra como prompt
     * @return linea leida, sin el salto de linea final
     */
    public String leerLinea(String mensaje) {
        System.out.print(mensaje);
        return this.scanner.nextLine();
    }

    /**
     * Lee un texto obligatorio, repitiendo la pregunta si el usuario deja el campo vacio.
     *
     * @param mensaje texto que se muestra como prompt
     * @return texto ingresado, sin espacios sobrantes al inicio o al final
     */
    public String leerTexto(String mensaje) {
        while (true) {
            String valor = this.leerLinea(mensaje).trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("El campo no puede quedar vacio. Intente nuevamente.");
        }
    }

    /**
     * Lee un texto opcional: si el usuario presiona ENTER se devuelve una cadena vacia.
     *
     * @param mensaje texto que se muestra como prompt
     * @return texto ingresado, o cadena vacia si no se escribio nada
     */
    public String leerTextoOpcional(String mensaje) {
        return this.leerLinea(mensaje).trim();
    }

    /**
     * Lee un numero entero validando la conversion con {@code Integer.valueOf}.
     *
     * @param mensaje texto que se muestra como prompt
     * @return el numero entero ingresado
     */
    public Integer leerEntero(String mensaje) {
        while (true) {
            String valor = this.leerLinea(mensaje).trim();
            try {
                return Integer.valueOf(valor);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero entero valido.");
            }
        }
    }

    /**
     * Lee un numero decimal validando la conversion con {@code Double.valueOf}.
     * <p>
     * Ademas de capturar {@code NumberFormatException}, se descartan los valores
     * especiales NaN e infinito para no contaminar los calculos de la capa de negocio.
     * </p>
     *
     * @param mensaje texto que se muestra como prompt
     * @return el numero decimal ingresado
     */
    public Double leerDecimal(String mensaje) {
        while (true) {
            String valor = this.leerLinea(mensaje).trim();
            try {
                Double numero = Double.valueOf(valor);
                if (numero.isNaN() || numero.isInfinite()) {
                    throw new NumberFormatException("Numero no finito.");
                }
                return numero;
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero decimal valido.");
            }
        }
    }

    /**
     * Muestra los valores de un enum numerados y devuelve el seleccionado por el usuario.
     *
     * @param <T>      tipo enum que se quiere seleccionar
     * @param mensaje  texto que se muestra antes de la lista de opciones
     * @param tipoEnum clase del enum, por ejemplo {@code CondicionVehiculo.class}
     * @return el valor del enum elegido
     */
    public <T extends Enum<T>> T leerEnum(String mensaje, Class<T> tipoEnum) {
        T[] valores = tipoEnum.getEnumConstants();
        System.out.println(mensaje);
        for (int i = 0; i < valores.length; i++) {
            System.out.println("  " + (i + 1) + ") " + valores[i].name());
        }
        while (true) {
            Integer opcion = this.leerEntero("Seleccione una opcion (1-" + valores.length + "): ");
            if (opcion.intValue() >= 1 && opcion.intValue() <= valores.length) {
                return valores[opcion.intValue() - 1];
            }
            System.out.println("Opcion fuera de rango. Intente nuevamente.");
        }
    }

    /**
     * Lee una respuesta afirmativa o negativa.
     *
     * @param mensaje texto que se muestra como prompt
     * @return {@code Boolean.TRUE} para s/si y {@code Boolean.FALSE} para n/no
     */
    public Boolean leerBooleano(String mensaje) {
        while (true) {
            String valor = this.leerLinea(mensaje + " (s/n): ").trim().toLowerCase(Locale.ROOT);
            if (valor.equals("s") || valor.equals("si") || valor.equals("sí")) {
                return Boolean.TRUE;
            }
            if (valor.equals("n") || valor.equals("no")) {
                return Boolean.FALSE;
            }
            System.out.println("Ingrese 's' para afirmar o 'n' para negar.");
        }
    }

    /**
     * Espera a que el usuario presione ENTER para continuar.
     */
    public void pausar() {
        System.out.print("Presione ENTER para continuar...");
        // Si quedo una linea pendiente se descarta; si no, la llamada espera al usuario.
        if (this.scanner.hasNextLine()) {
            this.scanner.nextLine();
        }
    }

    /**
     * Cierra el scanner y libera el flujo de entrada asociado.
     */
    public void cerrar() {
        this.scanner.close();
    }
}
