/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package es.ugr.ddsi.seminario1;

import java.sql.Connection;

/**
 * Punto de entrada del programa (SEM-04, Laura).
 * Abre la conexión, lanza el menú y garantiza que se cierre al salir.
 */
public class Seminario1 {

    public static void main(String[] args) {
        Connection con = null;
        try {
            con = Conexion.conectar();
            Menu.menuPrincipal(con);
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
}