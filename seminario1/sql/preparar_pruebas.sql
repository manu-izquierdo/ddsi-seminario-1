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
