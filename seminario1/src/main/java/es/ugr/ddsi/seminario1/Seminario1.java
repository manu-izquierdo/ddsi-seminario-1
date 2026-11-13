/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package es.ugr.ddsi.seminario1;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Menú principal y salida del programa.
 * Responsable: Laura.
 *
 * Se usa un único Connection (autocommit desactivado en Conexion.conectar())
 * que se pasa por parámetro a las funciones de cada compañero.
 */
public class Seminario1 {

    public static void main(String[] args) {
        Connection con = null;
        try {
            con = Conexion.conectar();
            menuPrincipal(con);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Siempre se cierra la conexión, también si algo falla,
            // para no dejar sesiones colgadas en Oracle.
            if (con != null) {
                Conexion.cerrar(con);
            }
            System.out.println("Programa finalizado.");
        }
    }

    /** Bucle del menú: lee y valida la opción y llama a la función correspondiente.
     * @param con */
    public static void menuPrincipal(Connection con) {
        // Scanner compartido con AltaPedido (ver Entrada). No se cierra.
        Scanner sc = Entrada.SC;
        boolean salir = false;

        while (!salir) {
            mostrarMenu();
            int opcion = leerOpcion(sc);

            try {
                switch (opcion) {
                    case 1 -> GestionTablas.crearTablas(con); //Nour
                    case 2 -> AltaPedido.altaPedido(con); //Manu
                    case 3 -> MostrarTablas.mostrarTablas(con); //Carlos
                    case 4 -> salir = true;
                    default -> System.out.println("Opción no válida. Elige un número del 1 al 4.");
                }
            } catch (Exception e) {
                // Un fallo en una opción no debe tumbar el menú.
                System.err.println("Error en la operación: " + e.getMessage());
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("No se pudo hacer rollback: " + ex.getMessage());
                }
            }
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
            return 4; // fin de la entrada (Ctrl+D / Ctrl+Z): salir limpiamente
        }
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
