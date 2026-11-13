
# Reparto DDSI: Seminario 1 y Práctica 2

Equipo: Ivan, Nour, Laura, Carlos y Manu.


## Seminario 1

### Estado a 13/11 (día de entrega)

| Tarea | Responsable | Estado |
|---|---|---|
| SEM-01 Lenguaje y SGBD | Todos | Hecho: Java 17 + JDBC (`ojdbc8`) + Oracle ETSIIT |
| SEM-02 Repo y esqueleto | Manu | Hecho |
| SEM-03 Conexión | Ivan | Hecho (`Conexion`, credenciales en `config.properties`). Falta que **cada uno** conecte desde su portátil |
| SEM-04 Menú principal | Laura | Hecho (`Seminario1`) |
| SEM-05 Opción 1 | Nour | Hecho (`GestionTablas` y scripts en `seminario1/sql/`) |
| SEM-06 Mostrar tablas | Carlos (en Trello figuraba Laura) | Hecho (`MostrarTablas`) |
| SEM-07 y SEM-08 Alta de pedido | Manu | Hecho (`AltaPedido`) |
| SEM-09 Pruebas | Nour | Probado sin Oracle (H2, 20 controles OK). **Falta repetir `PRUEBAS.md` en Oracle** |
| SEM-10 PDF | Ivan | Pendiente de cerrar |
| SEM-11 Zip y PRADO | Carlos | Pendiente |
| SEM-12 Defensa | Todos | Guion en `DEFENSA.md` |

Correcciones del 13/11, tras revisar `main`:
- `Conexion.cerrar()` hace `rollback` antes de cerrar, porque el driver de Oracle hace `COMMIT` al cerrar si queda algo pendiente.
- `MostrarTablas` ordena por la primera columna.
- Fuera del repo la carpeta `.vs/` de Visual Studio (subida por error), añadida al `.gitignore`.


### SEM-01 · Decidir lenguaje y SGBD
**Responsable:** Todos (convoca Manu)  
**Depende de:** —

El enunciado pide elegir un lenguaje de propósito general (Java, C, C++, Python o Ruby) y un SGBD relacional con transacciones. Propuesta: Java + JDBC + Oracle del servidor de la ETSIIT. JDBC trae en la propia API el control de transacciones (autocommit, savepoints, commit y rollback) y Oracle de la ETSIIT es el SGBD recomendado, así que no hace falta pedir permiso al profesor.

Checklist:
- [ ] Reunión de 15 min para decidir
- [ ] Anotar la decisión en esta tarjeta
- [ ] Si alguien propone otro lenguaje o SGBD: consultarlo antes con el profesor

**Hecho cuando:** Decisión tomada y escrita en la tarjeta.

### SEM-02 · Repositorio y esqueleto común del programa
**Responsable:** Manu  
**Depende de:** SEM-01

Crear el repo en GitHub, invitar a los 5 y dejar el esqueleto con las funciones vacías que usará cada uno, para trabajar en paralelo sin pisarse.

Checklist:
- [ ] Crear repo y dar acceso a los 5
- [ ] Estructura de carpetas y README con cómo ejecutar
- [ ] Firmas acordadas: conectar(), cerrar(), menuPrincipal(), crearTablas(), mostrarTablas(), altaPedido()
- [ ] Convención: una rama por persona, PR hacia main

**Hecho cuando:** Los 5 pueden clonar y compilar el esqueleto.

### SEM-03 · Entorno y conexión a Oracle
**Responsable:** Ivan  
**Depende de:** SEM-01

Dejar funcionando la conexión a la BD de la ETSIIT y la clase de conexión que usarán todos. Datos del Anexo I: host oracle0.ugr.es, puerto 1521, service name practbd, usuario = DNI sin letra con el primer dígito cambiado por una x minúscula, contraseña inicial igual al usuario (cambiarla al entrar).

Checklist:
- [ ] Activar VPN de la UGR (o Eduroam) y probar acceso
- [ ] Instalar SQL Developer (o SQLcl) y comprobar conexión con cada cuenta
- [ ] Añadir el driver JDBC de Oracle al proyecto
- [ ] Clase de conexión: abrir, cerrar y dejar autocommit desactivado
- [ ] Guardar usuario y contraseña fuera del repo
- [ ] Escribir las tareas de instalación para el PDF (SEM-10)
- [ ] Anotar en el README cómo matar sesiones colgadas (Anexo II: select * from mis_sesiones; execute mata_sesion(SID,SERIAL#))

**Hecho cuando:** Un programa mínimo conecta, hace un SELECT y cierra, en el portátil de cada miembro.

### SEM-04 · Menú principal y salida del programa
**Responsable:** Laura  
**Depende de:** SEM-02

Interfaz sencilla por consola con las 4 opciones del enunciado: (1) borrar y recrear tablas e insertar 10 tuplas, (2) dar de alta nuevo pedido, (3) mostrar contenido de las tablas, (4) salir y cerrar conexión.

Checklist:
- [ ] Bucle de menú con lectura y validación de opción
- [ ] Cada opción llama a la función del compañero correspondiente
- [ ] Salir cierra la conexión sin dejar sesiones colgadas

**Hecho cuando:** El menú funciona con las funciones vacías del esqueleto.

### SEM-05 · Opción 1: borrar/crear tablas e insertar 10 tuplas en Stock
**Responsable:** Nour  
**Depende de:** SEM-03

Tablas: Stock(Cproducto, Cantidad), Pedido(Cpedido, Ccliente, Fecha-pedido), Detalle-Pedido(Cpedido, Cproducto, Cantidad). Dominios numéricos salvo Fecha-pedido. Las 10 tuplas de Stock van fijadas en el código.

Checklist:
- [ ] Sentencias DROP de las 3 tablas (que no falle si no existen)
- [ ] CREATE con claves primarias y externas
- [ ] 10 INSERT predefinidos en Stock
- [ ] Ojo: una sentencia DDL termina la transacción en curso (lo dice el seminario)

**Hecho cuando:** Pulsar la opción deja las 3 tablas creadas y Stock con 10 filas, comprobado en SQL Developer.

### SEM-06 · Mostrar contenido de las tablas
**Responsable:** Carlos  
**Depende de:** SEM-03

Función que imprime las 3 tablas. Se usa desde la opción 3 del menú y la tienen que llamar también las opciones 1, 2 y 3 del alta de pedido.

Checklist:
- [ ] SELECT de Stock, Pedido y Detalle-Pedido
- [ ] Formato de salida legible
- [ ] Que sea reutilizable desde otras funciones (sin abrir otra conexión)

**Hecho cuando:** Se puede llamar desde cualquier punto del programa.

### SEM-07 · Alta de pedido (1/2): inicio de transacción y opción 1
**Responsable:** Manu  
**Depende de:** SEM-05, SEM-06

Captura los datos básicos del pedido, los inserta en Pedido e inicia la transacción. Muestra el submenú (1 añadir detalle, 2 eliminar detalles, 3 cancelar, 4 finalizar). La opción 1 pide producto y cantidad, comprueba que hay stock, inserta en Detalle-Pedido y actualiza Stock, y vuelve al submenú.

Checklist:
- [x] Captura de Cpedido, Ccliente y Fecha-pedido
- [x] INSERT en Pedido
- [x] Submenú de 4 opciones
- [x] Opción 1: comprobar stock, INSERT en Detalle-Pedido y UPDATE en Stock
- [x] Decidir con Manu dónde se pone el Savepoint (`SP_PEDIDO`, justo después del INSERT en Pedido)
- [ ] Mostrar contenido de la BD tras la opción 1 (la llamada ya está hecha; falta `mostrarTablas`, SEM-06)

**Estado:** implementado en `AltaPedido.java` (rama `manu/alta-pedido`). Probado en Oracle: crear pedido, producto inexistente, añadir detalle y finalizar. Pendiente de probar en Oracle: rechazo por falta de stock sin dejar cambios.

**Hecho cuando:** Se puede crear un pedido y añadir detalles; si no hay stock lo rechaza sin dejar cambios a medias.

### SEM-08 · Alta de pedido (2/2): opciones 2, 3 y 4 con Savepoint/Rollback/Commit
**Responsable:** Manu  
**Depende de:** SEM-07

Opción 2: elimina los detalles insertados del pedido actual (no el pedido). Opción 3: elimina el pedido y sus detalles y vuelve al menú principal. Opción 4: cambios permanentes y vuelta al menú. Tras las opciones 1, 2 y 3 hay que mostrar el contenido de la BD. Pista: pensad a qué estado hay que volver en la opción 2 y qué Savepoint lo permite.

Checklist:
- [x] Definir el Savepoint (lo decide quien hace SEM-07: `SP_PEDIDO` tras el INSERT en Pedido, y un savepoint por detalle para que INSERT y UPDATE sean una unidad)
- [ ] Opción 2 con Rollback a Savepoint
- [ ] Opción 3 con Rollback completo
- [x] Opción 4 con Commit
- [x] Comprobar que el autocommit está desactivado
- [ ] Mostrar BD tras 1, 2 y 3

**Estado:** implementado en `AltaPedido.java`. Probado en Oracle: opción 4 (Commit, visible desde otra sesión). Pendiente de probar en Oracle: opción 2 (Rollback a Savepoint, repetida) y opción 3 (Rollback completo). Marcar esas casillas al probarlas.

**Hecho cuando:** Las 4 opciones se comportan como pide el enunciado.

### SEM-09 · Pruebas de transacciones y casos límite
**Responsable:** Nour  
**Depende de:** SEM-08

Probar el flujo completo. Para verificar la parte transaccional, abrid una segunda sesión en SQL Developer: lo no confirmado no debe verse desde otra sesión.

Checklist:
- [ ] Pedido con stock suficiente y finalizar
- [ ] Pedido sin stock suficiente
- [ ] Añadir detalles y vaciarlos (opción 2)
- [ ] Cancelar pedido (opción 3)
- [ ] Cerrar el programa a mitad de pedido
- [ ] Comprobar con segunda sesión qué es visible antes y después del Commit
- [ ] Lista de fallos a los responsables

**Hecho cuando:** Lista de casos probados con resultado, sin fallos abiertos.

### SEM-10 · PDF breve de entrega
**Responsable:** Ivan (cada uno escribe su parte)  
**Depende de:** SEM-03, SEM-08

El enunciado pide un PDF muy breve con: lenguaje utilizado, tareas de instalación (lenguaje y paquetes de conexión), fuentes de código ajeno si lo hay, y el reparto de trabajo entre las personas del grupo.

Checklist:
- [ ] Lenguaje y SGBD (Ivan)
- [ ] Instalación y paquetes de conexión (Ivan)
- [ ] Fuentes externas citadas, si hay (quien las use)
- [ ] Reparto de trabajo (a partir de este tablero)
- [ ] Revisar y exportar a PDF

**Hecho cuando:** PDF de 1-2 páginas listo.

### SEM-11 · Empaquetar y entregar en PRADO
**Responsable:** Carlos  
**Depende de:** SEM-09, SEM-10

Un .zip con el PDF y el código fuente. Se entrega una sola vez por grupo.

Checklist:
- [ ] Comprobar que el código compila desde cero
- [ ] Meter PDF y fuentes en el zip
- [ ] Subir a PRADO y verificar que se ve
- [ ] Avisar al grupo

**Hecho cuando:** Entrega hecha y confirmada.

### SEM-12 · Preparar la defensa
**Responsable:** Todos  
**Depende de:** SEM-11

La nota sale de la entrega y de una defensa grupal en la que se responden preguntas sobre el código y se hace una demo. Todos tienen que saber explicar todo el código, también el de los demás.

Checklist:
- [ ] Cada uno explica su parte al resto en una sesión
- [ ] Cada uno se lee el código de los demás
- [ ] Ensayo de la demo con SQL Developer abierto
- [ ] Practicar preguntas cruzadas sobre transacciones

**Hecho cuando:** Todos pueden explicar cualquier función sin mirar.

## Práctica 2


### P2-00 · Repaso común: DFD, E/R y esquemas externos
**Responsable:** Todos  
**Depende de:** —

Material de la carpeta Pr2: 02-2 Herramientas DFDs, 02-3 Cómo conectar las herramientas, Modelado conceptual y 02-4/02-5/02-6. Lo importante: un esquema externo es un E/R que modela los datos que un proceso lee o escribe en un almacén, o los que guarda un almacén. Son descripciones de datos, no de acciones.

Checklist:
- [ ] Leer 02-2 y 02-3
- [ ] Leer Modelado conceptual (E/R)
- [ ] Ver los ejemplos de 02-5 y 02-6
- [ ] Dudas a una lista común

**Hecho cuando:** Todos han leído el material.

### P2-01 · Cerrar caja negra y DFD0
**Responsable:** Manu  
**Depende de:** —

Caja negra y DFD0 ya generados como imágenes. Falta validarlos con el profesor y dejar las dos versiones coherentes entre sí: las entidades externas de la caja negra y las del DFD0 hoy no coinciden. Confirmar también si el profesor quiere flujos de petición y confirmación o solo los datos que entrega el sistema.

Checklist:
- [ ] Decidir entidades externas únicas para ambos diagramas
- [ ] Cuadrar flujos de la caja negra con los del DFD0
- [ ] Enseñarlo al profesor y anotar correcciones
- [ ] Versión final en PNG y SVG

**Hecho cuando:** Caja negra y DFD0 aprobados.

### P2-02 · Maquetar la entrega ampliando el documento de la Práctica 1
**Responsable:** Ivan  
**Depende de:** —

La entrega es un único PDF que amplía el de la Práctica 1. Preparar la estructura con todos los apartados vacíos y los títulos, para que cada uno pegue su parte.

Checklist:
- [ ] Plantilla con los apartados del enunciado
- [ ] Estilos de título para el índice
- [ ] Plantilla común para cada esquema externo
- [ ] Compartir con el grupo

**Hecho cuando:** Documento base compartido.

### P2-03 · Esquema externo del DFD0: Proceso 1 · Gestión de TFM
**Responsable:** Manu  
**Depende de:** P2-00, P2-01

Esquema externo (E/R dentro de un hexágono) de Proceso 1 · Gestión de TFM: los datos que lee o escribe (proceso) o que guarda (almacén), con las restricciones semánticas que apliquen. Usad los nombres de flujo del DFD0 como guía.

Checklist:
- [ ] Listar los flujos de datos que entran y salen del elemento
- [ ] Identificar entidades, relaciones y atributos
- [ ] Comprobar que sustantivos son entidades y no agentes externos ni acciones
- [ ] Dibujar el E/R y anotar restricciones
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquema externo dibujado y revisado por otra persona.

### P2-04 · Esquema externo del DFD0: Proceso 2 · Ordenación docente de grupos de asignaturas
**Responsable:** Nour  
**Depende de:** P2-00, P2-01

Esquema externo (E/R dentro de un hexágono) de Proceso 2 · Ordenación docente de grupos de asignaturas: los datos que lee o escribe (proceso) o que guarda (almacén), con las restricciones semánticas que apliquen. Usad los nombres de flujo del DFD0 como guía.

Checklist:
- [ ] Listar los flujos de datos que entran y salen del elemento
- [ ] Identificar entidades, relaciones y atributos
- [ ] Comprobar que sustantivos son entidades y no agentes externos ni acciones
- [ ] Dibujar el E/R y anotar restricciones
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquema externo dibujado y revisado por otra persona.

### P2-05 · Esquema externo del DFD0: Proceso 3 · Gestión de TFG
**Responsable:** Carlos  
**Depende de:** P2-00, P2-01

Esquema externo (E/R dentro de un hexágono) de Proceso 3 · Gestión de TFG: los datos que lee o escribe (proceso) o que guarda (almacén), con las restricciones semánticas que apliquen. Usad los nombres de flujo del DFD0 como guía.

Checklist:
- [ ] Listar los flujos de datos que entran y salen del elemento
- [ ] Identificar entidades, relaciones y atributos
- [ ] Comprobar que sustantivos son entidades y no agentes externos ni acciones
- [ ] Dibujar el E/R y anotar restricciones
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquema externo dibujado y revisado por otra persona.

### P2-06 · Esquema externo del DFD0: Proceso 4 · Asignación docente
**Responsable:** Ivan  
**Depende de:** P2-00, P2-01

Esquema externo (E/R dentro de un hexágono) de Proceso 4 · Asignación docente: los datos que lee o escribe (proceso) o que guarda (almacén), con las restricciones semánticas que apliquen. Usad los nombres de flujo del DFD0 como guía.

Checklist:
- [ ] Listar los flujos de datos que entran y salen del elemento
- [ ] Identificar entidades, relaciones y atributos
- [ ] Comprobar que sustantivos son entidades y no agentes externos ni acciones
- [ ] Dibujar el E/R y anotar restricciones
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquema externo dibujado y revisado por otra persona.

### P2-07 · Esquema externo del DFD0: Almacén D1 · Asignaturas
**Responsable:** Manu  
**Depende de:** P2-00, P2-01

Esquema externo (E/R dentro de un hexágono) de Almacén D1 · Asignaturas: los datos que lee o escribe (proceso) o que guarda (almacén), con las restricciones semánticas que apliquen. Usad los nombres de flujo del DFD0 como guía.

Checklist:
- [ ] Listar los flujos de datos que entran y salen del elemento
- [ ] Identificar entidades, relaciones y atributos
- [ ] Comprobar que sustantivos son entidades y no agentes externos ni acciones
- [ ] Dibujar el E/R y anotar restricciones
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquema externo dibujado y revisado por otra persona.

### P2-08 · Esquema externo del DFD0: Almacén D2 · Datos de ordenación
**Responsable:** Laura  
**Depende de:** P2-00, P2-01

Esquema externo (E/R dentro de un hexágono) de Almacén D2 · Datos de ordenación: los datos que lee o escribe (proceso) o que guarda (almacén), con las restricciones semánticas que apliquen. Usad los nombres de flujo del DFD0 como guía.

Checklist:
- [ ] Listar los flujos de datos que entran y salen del elemento
- [ ] Identificar entidades, relaciones y atributos
- [ ] Comprobar que sustantivos son entidades y no agentes externos ni acciones
- [ ] Dibujar el E/R y anotar restricciones
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquema externo dibujado y revisado por otra persona.

### P2-09 · Esquema externo del DFD0: Almacén D3 · Datos docentes
**Responsable:** Carlos  
**Depende de:** P2-00, P2-01

Esquema externo (E/R dentro de un hexágono) de Almacén D3 · Datos docentes: los datos que lee o escribe (proceso) o que guarda (almacén), con las restricciones semánticas que apliquen. Usad los nombres de flujo del DFD0 como guía.

Checklist:
- [ ] Listar los flujos de datos que entran y salen del elemento
- [ ] Identificar entidades, relaciones y atributos
- [ ] Comprobar que sustantivos son entidades y no agentes externos ni acciones
- [ ] Dibujar el E/R y anotar restricciones
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquema externo dibujado y revisado por otra persona.

### P2-10 · ER0: ER armazón
**Responsable:** Laura  
**Depende de:** P2-03 a P2-09

Unir los 7 esquemas externos del DFD0 en un único diagrama E/R, conectados como se ha fijado en el DFD0.

Checklist:
- [ ] Reunir los 7 esquemas
- [ ] Unificar entidades repetidas y sus nombres
- [ ] Resolver incoherencias entre autores
- [ ] Dibujar el ER0 completo

**Hecho cuando:** ER0 completo y revisado.

### P2-11 · Refinamiento (DFD de detalle): Proceso 1 · Gestión de TFM
**Responsable:** Manu  
**Depende de:** P2-10

DFD que refina Proceso 1 · Gestión de TFM: subprocesos, agentes externos, almacenes y flujos conectados con este subsistema, respetando los flujos del DFD0.

Checklist:
- [ ] Descomponer en subprocesos
- [ ] Mantener todos los flujos del DFD0 que entran y salen
- [ ] Añadir los almacenes y flujos internos necesarios
- [ ] Numerar los procesos (1.1, 1.2…)
- [ ] Revisar consistencia con el DFD0

**Hecho cuando:** DFD de refinamiento revisado.

### P2-12 · Refinamiento (DFD de detalle): Proceso 2 · Ordenación docente de grupos
**Responsable:** Nour y Laura  
**Depende de:** P2-10

DFD que refina Proceso 2 · Ordenación docente de grupos: subprocesos, agentes externos, almacenes y flujos conectados con este subsistema, respetando los flujos del DFD0.

Checklist:
- [ ] Descomponer en subprocesos
- [ ] Mantener todos los flujos del DFD0 que entran y salen
- [ ] Añadir los almacenes y flujos internos necesarios
- [ ] Numerar los procesos (1.1, 1.2…)
- [ ] Revisar consistencia con el DFD0

**Hecho cuando:** DFD de refinamiento revisado.

### P2-13 · Refinamiento (DFD de detalle): Proceso 3 · Gestión de TFG
**Responsable:** Carlos  
**Depende de:** P2-10

DFD que refina Proceso 3 · Gestión de TFG: subprocesos, agentes externos, almacenes y flujos conectados con este subsistema, respetando los flujos del DFD0.

Checklist:
- [ ] Descomponer en subprocesos
- [ ] Mantener todos los flujos del DFD0 que entran y salen
- [ ] Añadir los almacenes y flujos internos necesarios
- [ ] Numerar los procesos (1.1, 1.2…)
- [ ] Revisar consistencia con el DFD0

**Hecho cuando:** DFD de refinamiento revisado.

### P2-14 · Refinamiento (DFD de detalle): Proceso 4 · Asignación docente
**Responsable:** Ivan  
**Depende de:** P2-10

DFD que refina Proceso 4 · Asignación docente: subprocesos, agentes externos, almacenes y flujos conectados con este subsistema, respetando los flujos del DFD0.

Checklist:
- [ ] Descomponer en subprocesos
- [ ] Mantener todos los flujos del DFD0 que entran y salen
- [ ] Añadir los almacenes y flujos internos necesarios
- [ ] Numerar los procesos (1.1, 1.2…)
- [ ] Revisar consistencia con el DFD0

**Hecho cuando:** DFD de refinamiento revisado.

### P2-15 · Refinamiento (DFD de detalle): Almacén D1 · Asignaturas
**Responsable:** Manu  
**Depende de:** P2-10

DFD que refina Almacén D1 · Asignaturas: el almacén detallado, rodeado de los procesos del DFD0 (sin refinar) y conectados con él como en el DFD0.

Checklist:
- [ ] Detallar el almacén en sus partes
- [ ] Rodearlo de los procesos del DFD0 conectados
- [ ] Mantener los flujos del DFD0
- [ ] Revisar consistencia

**Hecho cuando:** DFD de refinamiento revisado.

### P2-16 · Refinamiento (DFD de detalle): Almacén D2 · Datos de ordenación
**Responsable:** Laura  
**Depende de:** P2-10

DFD que refina Almacén D2 · Datos de ordenación: el almacén detallado, rodeado de los procesos del DFD0 (sin refinar) y conectados con él como en el DFD0.

Checklist:
- [ ] Detallar el almacén en sus partes
- [ ] Rodearlo de los procesos del DFD0 conectados
- [ ] Mantener los flujos del DFD0
- [ ] Revisar consistencia

**Hecho cuando:** DFD de refinamiento revisado.

### P2-17 · Refinamiento (DFD de detalle): Almacén D3 · Datos docentes
**Responsable:** Carlos  
**Depende de:** P2-10

DFD que refina Almacén D3 · Datos docentes: el almacén detallado, rodeado de los procesos del DFD0 (sin refinar) y conectados con él como en el DFD0.

Checklist:
- [ ] Detallar el almacén en sus partes
- [ ] Rodearlo de los procesos del DFD0 conectados
- [ ] Mantener los flujos del DFD0
- [ ] Revisar consistencia

**Hecho cuando:** DFD de refinamiento revisado.

### P2-18 · DFD1: unir todos los refinamientos
**Responsable:** Manu  
**Depende de:** P2-11 a P2-17

DFD1 resultante de representar los DFD en los que se refinó cada subsistema y almacén, conectados como se fijó en el DFD0.

Checklist:
- [ ] Reunir los 7 DFD de detalle
- [ ] Unir conservando los flujos del DFD0
- [ ] Comprobar numeración y nombres
- [ ] Dibujar DFD1 legible (si es grande, partirlo en láminas)

**Hecho cuando:** DFD1 completo.

### P2-19 · Esquemas externos del DFD1: Proceso 1 y almacén D1
**Responsable:** Manu  
**Depende de:** P2-18

Esquemas externos de los elementos del DFD1 que salen del refinamiento de Proceso 1 y almacén D1.

Checklist:
- [ ] Un esquema externo por cada subproceso y almacén refinado
- [ ] Coherencia con los esquemas del DFD0
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquemas externos revisados.

### P2-20 · Esquemas externos del DFD1: Proceso 2
**Responsable:** Nour  
**Depende de:** P2-18

Esquemas externos de los elementos del DFD1 que salen del refinamiento de Proceso 2.

Checklist:
- [ ] Un esquema externo por cada subproceso y almacén refinado
- [ ] Coherencia con los esquemas del DFD0
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquemas externos revisados.

### P2-21 · Esquemas externos del DFD1: Almacén D2
**Responsable:** Laura  
**Depende de:** P2-18

Esquemas externos de los elementos del DFD1 que salen del refinamiento de Almacén D2.

Checklist:
- [ ] Un esquema externo por cada subproceso y almacén refinado
- [ ] Coherencia con los esquemas del DFD0
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquemas externos revisados.

### P2-22 · Esquemas externos del DFD1: Proceso 3 y almacén D3
**Responsable:** Carlos  
**Depende de:** P2-18

Esquemas externos de los elementos del DFD1 que salen del refinamiento de Proceso 3 y almacén D3.

Checklist:
- [ ] Un esquema externo por cada subproceso y almacén refinado
- [ ] Coherencia con los esquemas del DFD0
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquemas externos revisados.

### P2-23 · Esquemas externos del DFD1: Proceso 4
**Responsable:** Ivan  
**Depende de:** P2-18

Esquemas externos de los elementos del DFD1 que salen del refinamiento de Proceso 4.

Checklist:
- [ ] Un esquema externo por cada subproceso y almacén refinado
- [ ] Coherencia con los esquemas del DFD0
- [ ] Pasarlo a revisión

**Hecho cuando:** Esquemas externos revisados.

### P2-24 · ER1: integrar los esquemas externos del DFD1
**Responsable:** Nour  
**Depende de:** P2-19 a P2-23

ER1 resultante de representar todos los esquemas externos del DFD1 en un único diagrama, conectados como en el ER0.

Checklist:
- [ ] Reunir esquemas del DFD1
- [ ] Unificar entidades y atributos
- [ ] Comparar con el ER0 y explicar cambios
- [ ] Dibujar ER1

**Hecho cuando:** ER1 completo.

### P2-25 · Paso a tablas del ER1
**Responsable:** Carlos y Laura  
**Depende de:** P2-24

Conjunto de tablas obtenido del esquema E/R (diseño lógico relacional).

Checklist:
- [ ] Entidades a tablas con clave primaria
- [ ] Relaciones según cardinalidad, con claves externas
- [ ] Revisar atributos multivaluados y jerarquías si hay
- [ ] Lista final de tablas

**Hecho cuando:** Tablas con claves primarias y externas.

### P2-26 · Dependencias funcionales y normalización
**Responsable:** Ivan y Nour  
**Depende de:** P2-25

Aplicar la teoría del tema 3: dependencias funcionales, proceso de normalización realizado y conjunto de tablas resultante con claves primarias y externas.

Checklist:
- [ ] Dependencias funcionales de cada tabla
- [ ] Claves candidatas
- [ ] Comprobar formas normales y descomponer si hace falta
- [ ] Tablas finales con PK y FK
- [ ] Explicar el proceso en el documento

**Hecho cuando:** Normalización documentada y tablas finales.

### P2-27 · Revisión cruzada de consistencia
**Responsable:** Todos  
**Depende de:** P2-24

Asegurar que el DFD es consistente con los esquemas externos de sus elementos (objetivo de la práctica). Cada uno revisa el trabajo de otra persona.

Checklist:
- [ ] Cada flujo del DFD tiene sus datos en algún esquema externo
- [ ] Los almacenes coinciden con el ER
- [ ] Nombres y numeración iguales en todo el documento
- [ ] Anotar y corregir incidencias

**Hecho cuando:** Sin incoherencias abiertas.

### P2-28 · Ensamblar y entregar el PDF final
**Responsable:** Manu  
**Depende de:** P2-26, P2-27

Un único PDF que amplía el de la Práctica 1 con caja negra, DFD0, esquemas externos, ER0, refinamientos, DFD1, ER1, tablas y normalización.

Checklist:
- [ ] Insertar todos los apartados en orden
- [ ] Índice y numeración correctos
- [ ] Revisión de ortografía
- [ ] Exportar PDF y revisar página a página
- [ ] Subir a PRADO

**Hecho cuando:** Entrega hecha y confirmada.