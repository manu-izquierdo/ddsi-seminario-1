# Plan de pruebas contra Oracle (Seminario 1)

Con la VPN activa. Para las pruebas de visibilidad hace falta una **segunda sesión**: en SQL Developer, `Ctrl+Mayús+N` (*Open Unshared SQL Worksheet*). Dos hojas normales comparten sesión y no sirven.

Datos de Stock tras la opción 1: producto 1→50, 2→30, 3→75, 4→20, 5→100, 6→40, 7→60, 8→15, 9→90, 10→25.

| # | Caso | Pasos | Resultado esperado | H2 | Oracle |
|---|---|---|---|---|---|
| 1 | Opción 1 sin tablas | Borrar las tablas en SQL Developer; opción 1 | Mensaje de éxito, sin error por tablas inexistentes; Stock con 10 filas | ✔ | [ ] |
| 2 | Opción 1 repetida | Opción 1 dos veces seguidas | Igual que el caso 1, sin errores | ✔ | [ ] |
| 3 | Opción 1 con pedidos existentes | Crear y finalizar un pedido; opción 1 | Se borran las 3 tablas sin error de clave externa y quedan vacías Pedido y Detalle | ✔ | [ ] |
| 4 | Visibilidad de la opción 1 | Tras la opción 1, `SELECT * FROM stock` en la 2ª sesión | Se ven las 10 filas (hay COMMIT) | ✔ | [ ] |
| 5 | Opción 3 | Mostrar tablas | Se imprimen las 3 tablas, ordenadas | ✔ | [ ] |
| 6 | Alta con stock suficiente | Pedido 1, cliente 100, fecha vacía; añadir producto 1 × 10 | Detalle creado; stock del producto 1 = 40; se muestra la BD | ✔ | [ ] |
| 7 | Stock insuficiente | Producto 8 × 16 | Rechazado ("Stock insuficiente: quedan 15"); sin detalle; stock sin cambios | ✔ | [ ] |
| 8 | Producto inexistente | Producto 99 × 1 | Mensaje "no existe"; sin cambios | ✔ | [ ] |
| 9 | Producto repetido | Añadir dos veces el producto 1 al mismo pedido | Aviso; el segundo no se inserta | ✔ | [ ] |
| 10 | Cantidad no válida | Cantidad 0, -3 y "abc" | 0 y -3: "debe ser mayor que 0"; "abc": pide otro número | ✔ | [ ] |
| 11 | Opción 2 | Añadir producto 1 × 10 y 2 × 5; opción 2 | Se vacía Detalle_Pedido; stock vuelve a 50 y 30; el pedido sigue; se muestra la BD | ✔ | [ ] |
| 12 | Opción 2 repetida | Tras el caso 11, añadir un detalle y volver a la opción 2 | Funciona otra vez (el Savepoint se puede reutilizar) | ✔ | [ ] |
| 13 | Opción 3 | Añadir 2 detalles; opción 3 | Desaparecen el pedido, los detalles y el cambio de stock; vuelve al menú; se muestra la BD | ✔ | [ ] |
| 14 | Opción 4 y visibilidad | Añadir un detalle; ANTES de la opción 4 mirar `pedido` y `stock` en la 2ª sesión; opción 4; mirar otra vez | Antes: la 2ª sesión no ve el pedido y ve el stock original. Después: ve todo | ✔ | [ ] |
| 15 | Pedido repetido | Dar de alta con un Cpedido ya existente | Mensaje de error y vuelta al menú, sin transacción abierta | ✔ | [ ] |
| 16 | Fecha | Fecha vacía; fecha `2026-11-13`; fecha `13/11/2026` | Hoy; la indicada; "formato no válido" y la vuelve a pedir | ✔ | [ ] |
| 17 | Menú | Opción 7, "hola", línea vacía | "Opción no válida"; el menú sigue | ✔ | [ ] |
| 18 | Cierre a mitad de pedido | Con un pedido sin finalizar, cerrar el programa (Ctrl+C) | En la 2ª sesión no queda nada del pedido | — | [ ] |
| 19 | Salir | Opción 4 | Programa finalizado; en `select * from mis_sesiones` no queda sesión del usuario | ✔ | [ ] |

La columna **H2** marca los casos que pasaron en la prueba automática sin Oracle (13/11); la columna **Oracle** es la que cuenta. El caso 18 depende de cómo cierra Oracle una sesión cortada y solo se puede comprobar allí.

Fallos: anotar caso, pasos y salida, y avisar a quien escribió esa parte.
