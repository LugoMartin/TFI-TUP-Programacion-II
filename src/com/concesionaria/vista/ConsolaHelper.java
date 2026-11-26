package com.concesionaria.vista;

import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

public class ConsolaHelper {

    private final Scanner scanner;

    public ConsolaHelper() {
        this(System.in);
    }

    public ConsolaHelper(InputStream entrada) {
        this.scanner = new Scanner(entrada == null ? System.in : entrada);
    }

    public String leerLinea(String mensaje) {
        System.out.print(mensaje);
        return this.scanner.nextLine();
    }

    public String leerTexto(String mensaje) {
        while (true) {
            String valor = this.leerLinea(mensaje).trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("El campo no puede quedar vacio. Intente nuevamente.");
        }
    }

    public String leerTextoOpcional(String mensaje) {
        return this.leerLinea(mensaje).trim();
    }

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

    public void pausar() {
        System.out.print("Presione ENTER para continuar...");

        if (this.scanner.hasNextLine()) {
            this.scanner.nextLine();
        }
    }

    public void cerrar() {
        this.scanner.close();
    }
}
