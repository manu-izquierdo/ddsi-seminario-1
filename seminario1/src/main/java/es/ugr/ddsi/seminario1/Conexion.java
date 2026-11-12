/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package es.ugr.ddsi.seminario1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexión a la base de datos Oracle de la ETSIIT.
 * Carga credenciales desde config.properties y desactiva el autocommit.
 */
public class Conexion {

    private static final String URL = "jdbc:oracle:thin:@//oracle0.ugr.es:1521/practbd";

    public static Connection conectar() throws SQLException {
        // Busca config.properties tanto si se ejecuta desde seminario1/ (NetBeans)
        // como desde la raíz del repositorio (VS Code)
        File f = new File("config.properties");
        if (!f.exists()) {
            f = new File("seminario1/config.properties");
        }

        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            prop.load(in);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer config.properties: " + e.getMessage());
        }

        String usuario = prop.getProperty("usuario");
        String clave = prop.getProperty("clave");

        Connection con = DriverManager.getConnection(URL, usuario, clave);
        con.setAutoCommit(false); // Requisito obligatorio para gestionar transacciones
        return con;
    }

    public static void cerrar(Connection con) {
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                    System.out.println("Conexión cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
