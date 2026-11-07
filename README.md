# DDSI · Seminario 1 — Acceso a bases de datos (Java + JDBC + Oracle)

Grupo: Ivan, Nour, Laura, Carlos y Manu.
Reparto de tareas y dependencias: [`Roadmap.md`](Roadmap.md).

Programa de consola en Java que se conecta por JDBC al Oracle de la ETSIIT y
gestiona pedidos usando **transacciones** (Savepoint, Rollback y Commit).

## 1. Requisitos

- **JDK 17 o superior** (`java -version`).
- **Maven** (NetBeans e IntelliJ lo traen integrado; en terminal, `mvn -v`).
- **SQL Developer** (cliente de la BD).
- **VPN de la UGR** (o Eduroam) activa para conectar al servidor Oracle.

## 2. Poner el proyecto en marcha

1. Clonar **fuera de Google Drive y OneDrive** (git y la sincronización
   de esas carpetas se estropean entre sí):
   ```
   git clone https://github.com/manu-izquierdo/ddsi-seminario-1
   ```
2. Abrir la carpeta `seminario1/` como proyecto Maven (NetBeans:
   *File → Open Project*). No subas ficheros del IDE: el `.gitignore` ya
   los excluye.
3. Compilar y ejecutar (vale en Windows, Linux y macOS):
   ```
   cd seminario1
   mvn compile exec:java
   ```
   La clase principal es `es.ugr.ddsi.seminario1.Seminario1`.

## 3. Base de datos de la ETSIIT

| Dato | Valor |
|---|---|
| Host | `oracle0.ugr.es` |
| Puerto | `1521` |
| Service name | `practbd` |
| Usuario | tu DNI sin letra, con el primer dígito cambiado por una `x` minúscula |
| Contraseña inicial | igual que el usuario (**cámbiala al entrar**) |
| URL JDBC | `jdbc:oracle:thin:@//oracle0.ugr.es:1521/practbd` |

- **Nunca subas usuario ni contraseña al repo.** `config.properties` está
  en el `.gitignore`.
- Cada usuario tiene como máximo 3 o 4 conexiones simultáneas.
  Cierra siempre la conexión al salir.
- Si se quedan sesiones colgadas, en SQL Developer:
  ```sql
  set serveroutput on
  select * from mis_sesiones;
  execute mata_sesion(SID, SERIAL#);   -- solo las INACTIVE
  ```

### Probar tu código contra Oracle (sin subir credenciales)

Para probar tu parte sin esperar a que esté la clase `Conexion` del grupo:

1. Crea `seminario1/config.properties` (junto al `pom.xml`). Ya está en el
   `.gitignore`, así que no se sube. Las claves se llaman `usuario` y `clave`;
   la clase `Conexion` del grupo debe leer estas mismas:
   ```
   usuario=TU_USUARIO
   clave=TU_CLAVE
   ```
2. Crea estas dos clases en `seminario1/src/main/java/es/ugr/ddsi/seminario1/`.
   Están en el `.gitignore` (`ConexionLocal.java` y `Prueba.java`): **no se suben**.
   ```java
   // ConexionLocal.java
   package es.ugr.ddsi.seminario1;

   import java.io.File;
   import java.io.FileInputStream;
   import java.sql.Connection;
   import java.sql.DriverManager;
   import java.util.Properties;

   public class ConexionLocal {
       public static Connection abrir() throws Exception {
           // NetBeans ejecuta desde seminario1/ y VS Code desde la raíz del repo
           File f = new File("config.properties");
           if (!f.exists()) f = new File("seminario1/config.properties");
           Properties p = new Properties();
           try (FileInputStream in = new FileInputStream(f)) { p.load(in); }
           Connection con = DriverManager.getConnection(
               "jdbc:oracle:thin:@//oracle0.ugr.es:1521/practbd",
               p.getProperty("usuario"), p.getProperty("clave"));
           con.setAutoCommit(false);   // antes de cualquier sentencia
           return con;
       }
   }
   ```
   ```java
   // Prueba.java
   package es.ugr.ddsi.seminario1;

   import java.sql.Connection;

   public class Prueba {
       public static void main(String[] args) throws Exception {
           Connection con = ConexionLocal.abrir();
           try {
               AltaPedido.altaPedido(con);   // cambia esta línea por lo que quieras probar
           } finally {
               con.close();
           }
       }
   }
   ```
3. Con la VPN activa, ejecuta `Prueba`: en NetBeans, clic derecho sobre el
   fichero → *Run File*; en terminal, desde `seminario1/`:
   ```
   mvn compile exec:java -Dexec.mainClass=es.ugr.ddsi.seminario1.Prueba
   ```

## 4. Tablas (nombres comunes para todo el código)

Los guiones del enunciado no son válidos en Oracle, así que usamos guion bajo
y sin tildes. **Todo el código y todo el SQL usa exactamente estos nombres:**

- `STOCK (CPRODUCTO, CANTIDAD)`
- `PEDIDO (CPEDIDO, CCLIENTE, FECHA_PEDIDO)`
- `DETALLE_PEDIDO (CPEDIDO, CPRODUCTO, CANTIDAD)`

Todos los dominios son numéricos salvo `FECHA_PEDIDO` (`DATE`).

### Preparar la BD para probar a mano

Mientras `crearTablas()` (opción 1, SEM-05) no esté lista, o para probar
sin pasar por el programa:

1. Conectar con SQL Developer a tu cuenta.
2. Abrir [`seminario1/sql/preparar_pruebas.sql`](seminario1/sql/preparar_pruebas.sql).
3. Ejecutar con **Run Script (F5)**. Borra y recrea las 3 tablas y deja
   10 filas en `STOCK` (con `COMMIT` incluido).

<details>
<summary>Ver el script SQL (copia del fichero, que es la fuente)</summary>

```sql
-- ============================================================
-- Seminario 1 DDSI - Entorno de pruebas en SQL Developer
-- Borra y recrea las 3 tablas y deja 10 filas en STOCK.
-- Ejecutar con "Run Script" (F5), NO con "Run Statement".
-- Es solo para probar a mano: la version real, en Java, es
-- crearTablas() (SEM-05).
-- ============================================================

-- 1) Borrado (de hijas a padres). Si la tabla no existe (ORA-00942), se ignora.
BEGIN EXECUTE IMMEDIATE 'DROP TABLE detalle_pedido';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE pedido';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE stock';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

-- 2) Creacion (de padres a hijas)
CREATE TABLE stock (
    cproducto NUMBER(10) CONSTRAINT pk_stock PRIMARY KEY,
    cantidad  NUMBER(10) NOT NULL
);

CREATE TABLE pedido (
    cpedido      NUMBER(10) CONSTRAINT pk_pedido PRIMARY KEY,
    ccliente     NUMBER(10) NOT NULL,
    fecha_pedido DATE       NOT NULL
);

CREATE TABLE detalle_pedido (
    cpedido   NUMBER(10) NOT NULL,
    cproducto NUMBER(10) NOT NULL,
    cantidad  NUMBER(10) NOT NULL,
    CONSTRAINT pk_detalle_pedido PRIMARY KEY (cpedido, cproducto),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (cpedido)   REFERENCES pedido (cpedido),
    CONSTRAINT fk_detalle_stock  FOREIGN KEY (cproducto) REFERENCES stock (cproducto)
);

-- 3) Datos de prueba en STOCK (hay productos con poco stock a proposito)
INSERT INTO stock VALUES (1, 100);
INSERT INTO stock VALUES (2, 50);
INSERT INTO stock VALUES (3, 25);
INSERT INTO stock VALUES (4, 10);
INSERT INTO stock VALUES (5, 5);
INSERT INTO stock VALUES (6, 2);
INSERT INTO stock VALUES (7, 1);
INSERT INTO stock VALUES (8, 0);
INSERT INTO stock VALUES (9, 75);
INSERT INTO stock VALUES (10, 30);

-- IMPORTANTE: sin COMMIT, el resto de sesiones no veran estas filas.
COMMIT;

SELECT * FROM stock ORDER BY cproducto;
```

</details>

### Segunda sesión para probar transacciones

Para comprobar qué se ve antes y después del `COMMIT` hace falta una
**segunda sesión**: en SQL Developer, `Ctrl+Mayús+N` (*Open Unshared SQL
Worksheet*). Dos hojas normales de la misma conexión comparten sesión y
no sirven. Recuerda el límite de conexiones: programa + 2 hojas = 3.

## 5. Estructura del código

Paquete `es.ugr.ddsi.seminario1`. Una clase por responsable para no pisarnos:

| Clase | Métodos | Responsable |
|---|---|---|
| `Seminario1` | `main`, `menuPrincipal` (lo añade Laura) | Laura (SEM-04) |
| `Conexion` | `conectar()`, `cerrar(Connection)` | Ivan (SEM-03) |
| `GestionTablas` | `crearTablas(Connection)` | Nour (SEM-05) |
| `MostrarTablas` | `mostrarTablas(Connection)` | Carlos (SEM-06) |
| `AltaPedido` | `altaPedido(Connection)` | Manu (SEM-07 y SEM-08) |

- Los métodos son `public static`.
- Se comparte **un único `Connection`**, que se pasa por parámetro.
  Autocommit **desactivado** desde que se abre la conexión.
- `AltaPedido` usa su propio `Scanner` sobre `System.in`. En el resto de clases
  no llaméis a `scanner.close()` (cierra `System.in` y rompe a los demás) y,
  si podéis, usad un solo `Scanner` en el menú.

## 6. Cómo trabajamos con git

- `main` **siempre compila**. Nadie trabaja directamente en `main`.
- Una rama por persona y tarea: `nombre/tarea` (ej. `manu/alta-pedido`).
- Se integra con **Pull Request** hacia `main`, revisado por otra persona,
  con *Squash and merge*, y se borra la rama después.
- Cada uno toca solo su clase. Los ficheros compartidos
  (`pom.xml`, `README.md`, `Roadmap.md`) se cambian en PRs pequeños.

### Rutina para estar siempre al día

Al empezar cada sesión de trabajo:
```
git checkout main
git pull
git checkout nombre/tarea
git merge main
```
Haz lo mismo justo antes de abrir el PR y compila después.
Commits pequeños y `git push` al terminar cada sesión.

Una tarea nueva empieza siempre con una rama nueva desde un `main`
recién actualizado:
```
git checkout main
git pull
git checkout -b nombre/tarea-nueva
```

## 7. Entrega

Un `.zip` con el PDF breve y el código fuente, subido a PRADO una sola vez
por grupo (SEM-10 y SEM-11 del Roadmap). En la defensa **todos** tienen que
saber explicar todo el código.
