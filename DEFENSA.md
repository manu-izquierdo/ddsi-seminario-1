# Guía para la defensa (Seminario 1)

La defensa es en grupo: preguntas sobre el código y una demo con un cliente de BD (SQL Developer). Todos tienen que poder explicar todo.

## Demo (orden sugerido, 5 minutos)
1. SQL Developer abierto con una segunda sesión (hoja sin compartir) lista para consultar `pedido`, `detalle_pedido` y `stock`.
2. Opción 1: se crean las tablas. Enseñarlo en SQL Developer.
3. Opción 2 de menú → pedido nuevo → añadir detalle → en la 2ª sesión NO se ve → opción 4 → ahora SÍ se ve.
4. Otro pedido: añadir 2 detalles → opción 2 (se deshacen detalles y stock, el pedido sigue) → añadir otro → opción 3 (desaparece todo).
5. Producto con stock insuficiente: rechazado sin dejar cambios.

## Preguntas probables y respuesta corta
- **¿Por qué el autocommit está desactivado?** Con autocommit cada sentencia se confirma sola y no hay transacción. Se desactiva en `Conexion.conectar()` antes de ejecutar nada.
- **¿Dónde empieza y dónde termina la transacción del pedido?** Empieza con el `INSERT` en Pedido (primer DML). Termina con `commit` (opción 4) o `rollback` completo (opción 3).
- **¿Qué es `SP_PEDIDO` y por qué ahí?** Savepoint creado justo después del `INSERT` en Pedido y antes de cualquier detalle. La opción 2 hace `rollback` a ese punto: se pierden los detalles y las bajadas de stock, pero el pedido sigue vivo.
- **¿Por qué otro savepoint por cada detalle?** Para que el `INSERT` en Detalle_Pedido y el `UPDATE` de Stock sean una unidad: si falla uno, se deshacen los dos sin tocar los detalles anteriores. No lleva nombre porque algunos SGBD no dejan repetir nombres de savepoint en la misma transacción.
- **¿Qué pasa con el stock al cancelar?** Vuelve al valor original, porque el `UPDATE` de Stock forma parte de la transacción y se deshace con el `rollback`.
- **¿Para qué el `FOR UPDATE` al comprobar el stock?** Bloquea esa fila para que otra sesión no la cambie entre la comprobación y el `UPDATE`.
- **¿Por qué el `COMMIT` al final de la opción 1?** Una sentencia DDL (CREATE, DROP) termina la transacción en curso, pero los `INSERT` posteriores no se confirman solos: sin `COMMIT` otras sesiones no verían las 10 filas.
- **¿Qué pasa si el programa se cae a mitad de pedido?** La sesión se cierra y Oracle deshace lo no confirmado. `Conexion.cerrar` también hace `rollback` antes de cerrar.
- **¿Por qué una sola conexión que se pasa por parámetro?** Todas las operaciones comparten la misma transacción y se respeta el límite de conexiones por usuario (3 o 4).
- **¿Por qué un único Scanner?** Dos Scanner sobre `System.in` se comen la entrada entre sí; por eso hay una clase `Entrada` con uno compartido.
- **¿Por qué `PreparedStatement`?** Evita concatenar valores del usuario en el SQL (inyección) y deja al driver el formato de fechas y números.
- **¿Qué pasa si se repite el código de pedido?** La clave primaria lo rechaza (`ORA-00001`); se captura, se hace `rollback` y se vuelve al menú.

## Conceptos que hay que tener claros
Transacción y propiedades ACID, autocommit, Savepoint, Rollback total y parcial (a un Savepoint), Commit, visibilidad entre sesiones, bloqueo de filas, efecto de un DDL sobre la transacción.
