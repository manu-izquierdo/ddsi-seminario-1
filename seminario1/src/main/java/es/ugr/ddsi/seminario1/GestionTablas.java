
package es.ugr.ddsi.seminario1;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GestionTablas {

    public static void crearTablas(Connection con) throws SQLException {

        Statement st = con.createStatement();

        // 1. Eliminar las tablas si ya existen.
        // Primero eliminamos la tabla que tiene las claves foraneas.

        eliminarTabla(st, "DETALLE_PEDIDO");
        eliminarTabla(st, "PEDIDO");
        eliminarTabla(st, "STOCK");

        // 2. Crear la tabla STOCK

        st.executeUpdate(
            "CREATE TABLE STOCK (" +
            "CPRODUCTO NUMBER PRIMARY KEY, " +
            "CANTIDAD NUMBER NOT NULL CHECK (CANTIDAD >= 0))"
        );

        // 3. Crear la tabla PEDIDO

        st.executeUpdate(
            "CREATE TABLE PEDIDO (" +
            "CPEDIDO NUMBER PRIMARY KEY, " +
            "CCLIENTE NUMBER NOT NULL, " +
            "FECHA_PEDIDO DATE NOT NULL)"
        );

        // 4. Crear la tabla DETALLE_PEDIDO

        st.executeUpdate(
            "CREATE TABLE DETALLE_PEDIDO (" +
            "CPEDIDO NUMBER NOT NULL, " +
            "CPRODUCTO NUMBER NOT NULL, " +
            "CANTIDAD NUMBER NOT NULL CHECK (CANTIDAD > 0), " +
            "PRIMARY KEY (CPEDIDO, CPRODUCTO), " +
            "FOREIGN KEY (CPEDIDO) REFERENCES PEDIDO(CPEDIDO), " +
            "FOREIGN KEY (CPRODUCTO) REFERENCES STOCK(CPRODUCTO))"
        );

        st.close();

        // 5. Insertar los 10 productos iniciales

        int[][] productos = {
            {1, 50},
            {2, 30},
            {3, 75},
            {4, 20},
            {5, 100},
            {6, 40},
            {7, 60},
            {8, 15},
            {9, 90},
            {10, 25}
        };

        String sql = "INSERT INTO STOCK (CPRODUCTO, CANTIDAD) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int[] producto : productos) {
                ps.setInt(1, producto[0]);
                ps.setInt(2, producto[1]);
                ps.executeUpdate();
            }

            // Confirmar la insercion de los productos.
            con.commit();

        } catch (SQLException e) {
            con.rollback();
            throw e;
        }

        System.out.println("Tablas creadas y datos iniciales insertados.");
    }

    // Metodo auxiliar para eliminar una tabla si existe.
    private static void eliminarTabla(Statement st, String nombre)
            throws SQLException {

        try {
            st.executeUpdate("DROP TABLE " + nombre + " PURGE");

        } catch (SQLException e) {

            // ORA-00942: la tabla o vista no existe.
            if (e.getErrorCode() != 942) {
                throw e;
            }
        }
    }
}
