/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package es.ugr.ddsi.seminario1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * SEM-04 · Menú principal y salida del programa.
 * Interfaz por consola con las 4 opciones del enunciado.
 */
public class Menu {

    // Ajusta estos datos a tu entorno Oracle
    private static final String URL  = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
    private static final String USER = "usuario";
    private static final String PASS = "clave";

    public static void main(String[] args) {
        Connection conn = null;
        Scanner sc = new Scanner(System.in);

        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false); // cada función hará commit/rollback
            System.out.println("Conexión establecida con Oracle.");

            boolean salir = false;
            while (!salir) {
                mostrarMenu();
                int opcion = leerOpcion(sc);

                switch (opcion) {
                    case 1 -> // Borrar y recrear tablas e insertar 10 tuplas
                        Operaciones.reiniciarTablas(conn);
                    case 2 -> // Dar de alta nuevo pedido
                        Operaciones.altaPedido(conn, sc);
                    case 3 -> // Mostrar contenido de las tablas
                        Operaciones.mostrarTablas(conn);
                    case 4 -> salir = true;
                    default -> System.out.println("Opción no válida. Elige un número del 1 al 4.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        } finally {
            cerrarConexion(conn);
            sc.close();
            System.out.println("Programa finalizado.");
        }
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== MENÚ PRINCIPAL =====");
        System.out.println("1. Borrar y recrear tablas e insertar 10 tuplas");
        System.out.println("2. Dar de alta nuevo pedido");
        System.out.println("3. Mostrar contenido de las tablas");
        System.out.println("4. Salir");
        System.out.print("Elige una opción: ");
    }

    /** Lee una línea y la convierte a entero; devuelve -1 si no es un número. */
    private static int leerOpcion(Scanner sc) {
        if (!sc.hasNextLine()) {
            return 4; // fin de entrada (Ctrl+D / Ctrl+Z): salir limpiamente
        }
        String linea = sc.nextLine().trim();
        try {
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Cierra la conexión sin dejar sesiones colgadas ni transacciones abiertas. */
    private static void cerrarConexion(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed()) {
                try (conn) {
                    conn.rollback(); // descarta cualquier transacción pendiente
                } // descarta cualquier transacción pendiente
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
