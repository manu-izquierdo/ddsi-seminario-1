CREATE TABLE Stock (
    Cproducto NUMBER PRIMARY KEY,
    Cantidad NUMBER NOT NULL CHECK (Cantidad >= 0)
);

CREATE TABLE Pedido (
    Cpedido NUMBER PRIMARY KEY,
    Ccliente NUMBER NOT NULL,
    Fecha_pedido DATE NOT NULL
);

CREATE TABLE Detalle_Pedido (
    Cpedido NUMBER,
    Cproducto NUMBER,
    Cantidad NUMBER NOT NULL CHECK (Cantidad > 0),

    PRIMARY KEY (Cpedido, Cproducto),

    FOREIGN KEY (Cpedido)
        REFERENCES Pedido(Cpedido),

    FOREIGN KEY (Cproducto)
        REFERENCES Stock(Cproducto)
);
commit;

 