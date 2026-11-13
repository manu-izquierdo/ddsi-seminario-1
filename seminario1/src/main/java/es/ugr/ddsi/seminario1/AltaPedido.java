package es.ugr.ddsi.seminario1;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Alta de un nuevo pedido con control de transacciones (SEM-07 y SEM-08).
 *
 * Toda la operación es UNA transacción que se abre con el INSERT en Pedido y
 * se cierra con COMMIT (opción 4) o con ROLLBACK (opción 3):
 *
 *   INSERT Pedido  ->  SAVEPOINT SP_PEDIDO  ->  detalles (opción 1)  ->  COMMIT / ROLLBACK
 *
 * - SP_PEDIDO se crea justo después de insertar el pedido y antes de cualquier
 *   detalle. La opción 2 hace ROLLBACK TO SP_PEDIDO: se deshacen los detalles y
 *   las bajadas de stock, pero el pedido sigue vivo y la transacción continúa.
 * - Antes de cada detalle se crea otro savepoint (sin nombre) para que el INSERT
 *   en Detalle_Pedido y el UPDATE en Stock sean una unidad: si falla alguna, se
 *   vuelve ahí sin tocar los detalles anteriores. Va sin nombre porque algunos
 *   SGBD no dejan repetir el nombre de un savepoint dentro de la misma transacción.
 * - La opción 3 hace ROLLBACK completo: desaparece el pedido, sus detalles y los
 *   cambios de stock.
 *
 * Requisito: la conexión debe tener el autocommit desactivado.
 */
public class AltaPedido {

    // Un único Scanner para toda la clase. No se cierra: cerraría System.in.
    private static final Scanner ENTRADA = new Scanner(System.in);

    public static void altaPedido(Connection con) {
        try {
            // Sin esto cada sentencia se confirmaría sola y no habría transacción.
            con.setAutoCommit(false);

            int cpedido = leerEntero("Código de pedido: ");
            int ccliente = leerEntero("Código de cliente: ");
            Date fecha = leerFecha();

            // Primera sentencia DML: aquí empieza la transacción.
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO pedido (cpedido, ccliente, fecha_pedido) VALUES (?, ?, ?)")) {
                ps.setInt(1, cpedido);
                ps.setInt(2, ccliente);
                ps.setDate(3, fecha);
                ps.executeUpdate();
            } catch (SQLException e) {
                con.rollback();
                System.out.println("No se ha podido crear el pedido: " + e.getMessage());
                return;
            }

            // Punto al que volverá la opción 2: pedido creado, sin detalles.
            Savepoint spPedido = con.setSavepoint("SP_PEDIDO");

            while (true) {
                System.out.println();
                System.out.println("--- Pedido " + cpedido + " ---");
                System.out.println("1. Añadir detalle de producto");
                System.out.println("2. Eliminar todos los detalles de producto");
                System.out.println("3. Cancelar pedido");
                System.out.println("4. Finalizar pedido");
                int opcion = leerEntero("Opción: ");

                switch (opcion) {
                    case 1 -> {
                        anadirDetalle(con, cpedido);
                        MostrarTablas.mostrarTablas(con);
                    }
                    case 2 -> {
                        // Deshace detalles y stock; el pedido y la transacción siguen.
                        con.rollback(spPedido);
                        System.out.println("Detalles eliminados (el pedido sigue abierto).");
                        MostrarTablas.mostrarTablas(con);
                    }
                    case 3 -> {
                        // Deshace todo, incluido el INSERT en Pedido: fin de la transacción.
                        con.rollback();
                        System.out.println("Pedido cancelado.");
                        MostrarTablas.mostrarTablas(con);
                        return;
                    }
                    case 4 -> {
                        // Cambios permanentes (visibles para otras sesiones): fin de la transacción.
                        con.commit();
                        System.out.println("Pedido " + cpedido + " finalizado.");
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            }
        } catch (SQLException e) {
            // Error inesperado: no dejamos nada a medias.
            System.out.println("Error en el alta de pedido: " + e.getMessage());
            try {
                con.rollback();
            } catch (SQLException e2) {
                System.out.println("No se ha podido deshacer la transacción: " + e2.getMessage());
            }
        }
    }

    /**
     * Opción 1: añade un detalle al pedido actual si hay stock. Si no se puede,
     * lo rechaza sin dejar cambios a medias.
     */
    private static void anadirDetalle(Connection con, int cpedido) throws SQLException {
        int cproducto = leerEntero("Código de producto: ");
        int cantidad = leerEntero("Cantidad: ");
        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que 0.");
            return;
        }

        // La clave de Detalle_Pedido es (cpedido, cproducto): no se repite producto.
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM detalle_pedido WHERE cpedido = ? AND cproducto = ?")) {
            ps.setInt(1, cpedido);
            ps.setInt(2, cproducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Ese producto ya está en el pedido. Elimina los detalles (opción 2) para cambiarlo.");
                    return;
                }
            }
        }

        // Comprobamos el stock bloqueando la fila (FOR UPDATE) para que otra
        // sesión no lo cambie entre la comprobación y el UPDATE.
        int disponible;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT cantidad FROM stock WHERE cproducto = ? FOR UPDATE")) {
            ps.setInt(1, cproducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("El producto " + cproducto + " no existe.");
                    return;
                }
                disponible = rs.getInt(1);
            }
        }
        if (disponible < cantidad) {
            System.out.println("Stock insuficiente: quedan " + disponible + " unidades del producto " + cproducto + ".");
            return;
        }

        // INSERT + UPDATE forman una unidad: si falla una, se deshacen las dos.
        Savepoint spDetalle = con.setSavepoint();
        try {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO detalle_pedido (cpedido, cproducto, cantidad) VALUES (?, ?, ?)")) {
                ps.setInt(1, cpedido);
                ps.setInt(2, cproducto);
                ps.setInt(3, cantidad);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE stock SET cantidad = cantidad - ? WHERE cproducto = ?")) {
                ps.setInt(1, cantidad);
                ps.setInt(2, cproducto);
                ps.executeUpdate();
            }
            System.out.println("Detalle añadido: producto " + cproducto + ", cantidad " + cantidad + ".");
        } catch (SQLException e) {
            con.rollback(spDetalle);
            System.out.println("No se ha podido añadir el detalle: " + e.getMessage());
        }
    }

    // ---------- Lectura de datos por consola ----------

    private static int leerEntero(String texto) {
        while (true) {
            System.out.print(texto);
            String linea = ENTRADA.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero.");
            }
        }
    }

    private static Date leerFecha() {
        while (true) {
            System.out.print("Fecha del pedido (aaaa-mm-dd, vacío = hoy): ");
            String linea = ENTRADA.nextLine().trim();
            if (linea.isEmpty()) {
                return Date.valueOf(LocalDate.now());
            }
            try {
                return Date.valueOf(linea);
            } catch (IllegalArgumentException e) {
                System.out.println("Formato no válido. Ejemplo: 2026-11-07");
            }
        }
    }
}
