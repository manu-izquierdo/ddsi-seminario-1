# DDSI · Seminario 1: acceso a bases de datos (Java + JDBC + Oracle)

Grupo C: Ivan, Nour, Laura, Carlos y Manu.

Programa de consola en Java que se conecta por JDBC al Oracle de la ETSIIT y
da de alta pedidos dentro de una **transacción** (autocommit desactivado,
`Savepoint`, `rollback` y `commit`).

| Documento | Para qué |
|---|---|
| Este README | Instalar, configurar, ejecutar y entender el código |
| [`PRUEBAS.md`](PRUEBAS.md) | Casos de prueba contra Oracle (con casilla para marcar) |
| [`DEFENSA.md`](DEFENSA.md) | Guion de la demo y preguntas probables |
| [`Roadmap.md`](Roadmap.md) | Reparto de tareas y estado de cada una |

## 1. Qué hace el programa

Menú principal:

1. **Borrar y recrear las tablas** e insertar 10 tuplas en `STOCK`.
2. **Dar de alta un pedido.** Pide código de pedido, de cliente y fecha, inserta
   en `PEDIDO` (aquí empieza la transacción) y abre este submenú:
   1. Añadir un detalle: comprueba el stock, inserta en `DETALLE_PEDIDO` y descuenta de `STOCK`.
   2. Eliminar todos los detalles: `rollback` al savepoint `SP_PEDIDO` (el pedido sigue).
   3. Cancelar el pedido: `rollback` completo y vuelta al menú.
   4. Finalizar el pedido: `commit` y vuelta al menú.

   Tras las opciones 1, 2 y 3 del submenú se muestra el contenido de las tablas.
3. **Mostrar el contenido** de las tres tablas.
4. **Salir**, cerrando la conexión.

## 2. Requisitos

- **JDK 17 o superior** (`java -version`).
- **Maven**: NetBeans e IntelliJ lo traen integrado; en terminal, `mvn -v`.
- **Driver JDBC de Oracle**: `ojdbc8` 19.22.0.0. No hay que bajarlo a mano,
  Maven lo descarga a partir del `pom.xml`.
- **SQL Developer**, para ver la BD y abrir una segunda sesión en las pruebas.
- **VPN de la UGR** (o Eduroam) activa: sin ella no se llega a `oracle0.ugr.es`.

## 3. Puesta en marcha

1. Clona el repo **fuera de Google Drive y OneDrive**, porque su sincronización
   estropea la carpeta `.git`:
   ```
   git clone https://github.com/manu-izquierdo/ddsi-seminario-1
   ```
2. Crea el fichero **`seminario1/config.properties`** (junto al `pom.xml`) con tus
   credenciales de Oracle:
   ```
   usuario=TU_USUARIO
   clave=TU_CLAVE
   ```
   Está en el `.gitignore`: **nunca se sube**. `Conexion` lo busca primero en la
   carpeta desde la que se ejecuta y luego en `seminario1/`, así que funciona
   tanto desde NetBeans como desde la raíz del repo en VS Code.
3. Activa la VPN.
4. Ejecuta el programa:
   - **NetBeans**: *File → Open Project* → carpeta `seminario1` → *Run* (F6).
   - **Terminal**:
     ```
     cd seminario1
     mvn compile exec:java
     ```
   La clase principal es `es.ugr.ddsi.seminario1.Seminario1`.
5. La primera vez, elige la **opción 1** para crear las tablas.

### Errores típicos al arrancar

| Mensaje | Causa |
|---|---|
| `No se pudo leer config.properties` | Falta el fichero o está en otra carpeta (paso 2) |
| `IO Error: The Network Adapter could not establish the connection` | VPN apagada o sin red |
| `ORA-01017: invalid username/password` | Usuario o clave mal en `config.properties` |
| `ORA-28001: the password has expired` | Cambia la contraseña desde SQL Developer |
| `ORA-02391: exceeded simultaneous SESSIONS_PER_USER limit` | Demasiadas sesiones abiertas (ver sección 4) |

## 4. Base de datos de la ETSIIT

| Dato | Valor |
|---|---|
| Host | `oracle0.ugr.es` |
| Puerto | `1521` |
| Service name | `practbd` |
| Usuario | tu DNI sin letra, con el primer dígito cambiado por una `x` minúscula |
| Contraseña inicial | igual que el usuario (**cámbiala al entrar**) |
| URL JDBC | `jdbc:oracle:thin:@//oracle0.ugr.es:1521/practbd` |

- Cada usuario puede tener 3 o 4 conexiones a la vez. El programa cierra siempre
  la suya al salir.
- Si se quedan sesiones colgadas, en SQL Developer:
  ```sql
  set serveroutput on
  select * from mis_sesiones;
  execute mata_sesion(SID, SERIAL#);   -- solo las INACTIVE
  ```

### Tablas

Los guiones del enunciado (`Fecha-pedido`, `Detalle-Pedido`) no son válidos en
Oracle, así que se usa guion bajo:

| Tabla | Columnas | Claves |
|---|---|---|
| `STOCK` | `CPRODUCTO`, `CANTIDAD` (≥ 0) | PK `CPRODUCTO` |
| `PEDIDO` | `CPEDIDO`, `CCLIENTE`, `FECHA_PEDIDO` (`DATE`) | PK `CPEDIDO` |
| `DETALLE_PEDIDO` | `CPEDIDO`, `CPRODUCTO`, `CANTIDAD` (> 0) | PK (`CPEDIDO`, `CPRODUCTO`); FK a `PEDIDO` y a `STOCK` |

Todos los dominios son numéricos salvo `FECHA_PEDIDO`. Stock inicial tras la opción 1:

| Producto | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|---|---|---|---|---|---|---|---|---|---|---|
| Cantidad | 50 | 30 | 75 | 20 | 100 | 40 | 60 | 15 | 90 | 25 |

### Scripts SQL (`seminario1/sql/`)

El programa **no los usa**: la opción 1 hace lo mismo desde Java. Sirven para
preparar o revisar la BD a mano en SQL Developer (ejecutar con *Run Script*, F5).

| Fichero | Contenido |
|---|---|
| `database.sql` | `CREATE TABLE` de las 3 tablas, igual que la opción 1 |
| `data.sql` | Las 10 tuplas de `STOCK`, iguales que en la opción 1 |
| `preparar_pruebas.sql` | Borra y recrea todo con otro stock pensado para casos límite (productos con 0, 1 y 2 unidades). No lleva los `CHECK` |

### Segunda sesión para ver las transacciones

Para comprobar qué ve otra sesión antes y después del `commit`, abre en SQL
Developer una hoja **sin compartir**: `Ctrl+Mayús+N` (*Open Unshared SQL
Worksheet*). Dos hojas normales de la misma conexión comparten sesión y no sirven.
Programa + 2 hojas = 3 conexiones, dentro del límite.

## 5. Estructura del código

Paquete `es.ugr.ddsi.seminario1`, en `seminario1/src/main/java/`:

| Clase | Qué hace | Autor |
|---|---|---|
| `Seminario1` | `main`: abre la conexión, lanza el menú y la cierra siempre (`finally`). `menuPrincipal`: bucle del menú, valida la opción y, si una opción falla, hace `rollback` y sigue | Laura |
| `Conexion` | `conectar()`: lee `config.properties`, conecta y **desactiva el autocommit**. `cerrar()`: `rollback` de lo pendiente y cierre | Ivan |
| `GestionTablas` | Opción 1: `DROP ... PURGE` de las 3 tablas (ignora ORA-00942 si no existen), `CREATE`, 10 `INSERT` y `commit` | Nour |
| `MostrarTablas` | Imprime las 3 tablas con sus columnas (`ResultSetMetaData`), ordenadas por la primera columna | Carlos |
| `AltaPedido` | Opción 2 completa: transacción, savepoints, submenú y control de stock con `SELECT ... FOR UPDATE` | Manu |
| `Entrada` | Un único `Scanner` sobre `System.in` que comparten el menú y el alta de pedido | Manu |

Decisiones comunes:

- **Una sola `Connection`** para todo el programa, que se pasa por parámetro.
  Así todas las operaciones están en la misma sesión y se respeta el límite de conexiones.
- **Autocommit desactivado** desde que se abre la conexión.
- **Un único `Scanner`** (`Entrada.SC`) y nunca se cierra. Con dos `Scanner`
  sobre `System.in`, el primero se queda en su buffer las líneas que tocaban al
  segundo (pasa al pegar varias líneas en la consola).
- **`PreparedStatement`** para todo lo que lleva datos del usuario.

### La transacción del alta de pedido

```
INSERT Pedido ─► SAVEPOINT SP_PEDIDO ─► [savepoint por detalle ─► INSERT Detalle + UPDATE Stock] x N ─► COMMIT (opción 4)
                        ▲                                                                       └─► ROLLBACK (opción 3)
                        └──────────── ROLLBACK TO SP_PEDIDO (opción 2: se pierden detalles y stock, el pedido sigue)
```

- `SP_PEDIDO` se crea justo después de insertar el pedido. La opción 2 vuelve
  ahí, y se puede usar varias veces en el mismo pedido.
- Cada detalle tiene su propio savepoint (sin nombre). El `INSERT` en
  `DETALLE_PEDIDO` y el `UPDATE` de `STOCK` son así una unidad: si falla uno,
  se deshacen los dos sin tocar los detalles anteriores.
- `SELECT cantidad FROM stock ... FOR UPDATE` bloquea la fila del producto hasta
  el final de la transacción, para que otra sesión no cambie el stock entre la
  comprobación y el `UPDATE`.
- Se rechaza, sin dejar cambios: producto inexistente, producto ya incluido en
  el pedido, cantidad ≤ 0 y stock insuficiente.
- Un código de pedido repetido lo rechaza la clave primaria. Se hace
  `rollback` y se vuelve al menú.

## 6. Pruebas

- Casos contra Oracle: [`PRUEBAS.md`](PRUEBAS.md). Hay que hacerlos con la VPN
  y una segunda sesión abierta, y marcar cada casilla.
- El 13/11 se ejecutó el programa completo sobre H2 en modo Oracle, como prueba
  automática **sin** Oracle. Fuera del repo hubo dos adaptaciones: quitar
  `PURGE` y traducir el error de tabla inexistente a ORA-00942. Pasaron los 20
  controles: opción 1 repetida y con pedidos ya creados, visibilidad antes y después del `commit`,
  bloqueo `FOR UPDATE`, opciones 2 (repetida), 3 y 4, rechazos de stock y salida. **Esto
  no sustituye a la prueba en Oracle.**

## 7. Cómo trabajamos con git

- `main` **siempre compila**. Nadie hace push directo a `main`: se integra con
  **Pull Request**, revisado por otra persona.
- Una rama por persona y tarea, `nombre/tarea` (ej. `manu/alta-pedido`), y se
  borra después del merge.
- No se suben ficheros del IDE (`.vs/`, `nbproject/private/`, `.idea/`...) ni
  `config.properties`. Antes de hacer commit, revisa `git status`.

Al empezar cada sesión de trabajo:
```
git checkout main
git pull
git checkout -b nombre/tarea-nueva     # o: git checkout nombre/tarea && git merge main
```

### Estado de las ramas (13/11)

| Rama | Estado |
|---|---|
| `main` | Programa completo |
| `Laura`, `carlos/mostrar-tablas`, `gestion-tablas-pr`, `sql-tablas-solo-pr`, `manu/alta-pedido` | Ya integradas en `main`: se pueden borrar |
| `Nour` | Antigua y **no se debe fusionar**: devolvería `Conexion` a una clase vacía y movería los SQL a `database/`. Su contenido útil ya está en `main` |

## 8. Entrega

Un `.zip` con el PDF breve (lenguaje, instalación y paquetes, fuentes de código
ajeno y reparto del trabajo) y el código fuente: carpeta `seminario1/` **sin**
`target/` ni `config.properties`. Lo sube una sola persona a PRADO. En la defensa
**todos** tienen que saber explicar todo el código; ver [`DEFENSA.md`](DEFENSA.md).
