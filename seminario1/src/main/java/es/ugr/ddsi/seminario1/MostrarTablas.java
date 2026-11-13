package es.ugr.ddsi.seminario1;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class MostrarTablas {

    /**
     * Muestra el contenido de las tablas Stock, Pedido y Detalle_Pedido.
     * @param conexion Conexión activa a la base de datos.
     */
    public static void mostrarTablas(Connection conexion) {
        // En SQL, los guiones (-) no son válidos para nombres de tablas, se asume Detalle_Pedido
        String[] tablas = {"Stock", "Pedido", "Detalle_Pedido"};

        for (String tabla : tablas) {
            System.out.println("\n============================================================");
            System.out.println(" TABLA: " + tabla.toUpperCase());
            System.out.println("============================================================");
            
            // ORDER BY 1: misma salida en cada ejecución (por la primera columna).
            String query = "SELECT * FROM " + tabla + " ORDER BY 1";
            
            try (Statement stmt = conexion.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                // 1. Imprimir las cabeceras (nombres de las columnas)
                for (int i = 1; i <= columnCount; i++) {
                    // %-20s ajusta cada columna a 20 caracteres alineados a la izquierda
                    System.out.printf("%-20s", metaData.getColumnName(i));
                }
                System.out.println();
                
                // Línea separadora
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print("--------------------");
                }
                System.out.println();
                
                // 2. Imprimir las filas de datos
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String valor = rs.getString(i);
                        System.out.printf("%-20s", (valor == null ? "NULL" : valor));
                    }
                    System.out.println();
                }
                
            } catch (SQLException e) {
                System.err.println("Error al hacer SELECT en la tabla " + tabla + ": " + e.getMessage());
            }
        }
        System.out.println("\n");
    }
}
