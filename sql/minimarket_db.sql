-- =====================================================================================
--  MINIMARKET EL PUENTE  -  Base de datos PostgreSQL  (compatible con pgAdmin III, PG 8.4+)
-- =====================================================================================
--  Se ejecuta en DOS PASOS (CREATE DATABASE no puede ir junto con el resto del script):
--
--  PASO 1: conectado a la base "postgres", ejecute SOLO esta linea (sin comentarios):
--
--          CREATE DATABASE "MINIMARKET";
--
--  PASO 2: seleccione/conectese a la base "MINIMARKET" (doble clic en ella en el arbol,
--          luego Herramientas > Editor de consultas) y ejecute TODO lo que sigue.
--
--  Importante: el nombre va con comillas y en mayusculas porque asi lo usa el programa.
-- =====================================================================================


-- ---------------------------------------------------------------- PRODUCTOS
CREATE TABLE productos (
    codigo   BIGINT        PRIMARY KEY,                       -- codigo de barras / interno
    producto VARCHAR(200)  NOT NULL,
    precio   NUMERIC(14,2) NOT NULL CHECK (precio >= 0)
);

-- ---------------------------------------------------------------- USUARIOS
-- usuario y clave son numericos y pueden coincidir. La clave NUNCA se guarda en claro:
-- el programa guarda un hash PBKDF2-HMAC-SHA256 y su sal (columnas hash y salt).
CREATE TABLE usuarios (
    usuario VARCHAR(20)  PRIMARY KEY CHECK (usuario ~ '^[0-9]+$'),
    salt    VARCHAR(64)  NOT NULL,
    hash    VARCHAR(128) NOT NULL,
    creado  TIMESTAMP    NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------- VENTAS
CREATE TABLE ventas (
    id      BIGSERIAL     PRIMARY KEY,
    fecha   TIMESTAMP     NOT NULL,
    usuario VARCHAR(20),                                      -- quien cobro la venta
    total   NUMERIC(14,2) NOT NULL
);
CREATE INDEX ventas_fecha_idx ON ventas (fecha);

CREATE TABLE venta_items (
    id       BIGSERIAL     PRIMARY KEY,
    venta_id BIGINT        NOT NULL REFERENCES ventas (id) ON DELETE CASCADE,
    codigo   VARCHAR(50)   NOT NULL,
    producto VARCHAR(200)  NOT NULL,                          -- nombre al momento de la venta
    precio   NUMERIC(14,2) NOT NULL,                          -- precio al momento de la venta
    cantidad NUMERIC(12,3) NOT NULL,                          -- admite kilos (0,500)
    subtotal NUMERIC(14,2) NOT NULL
);
CREATE INDEX venta_items_venta_idx ON venta_items (venta_id);

-- ---------------------------------------------------------------- VISTA PARA CONSULTAS
-- Para revisar ventas desde pgAdmin:  SELECT * FROM v_ventas_detalle ORDER BY fecha DESC;
CREATE VIEW v_ventas_detalle AS
SELECT v.id AS venta, v.fecha, v.usuario, i.codigo, i.producto, i.precio, i.cantidad, i.subtotal
FROM ventas v
JOIN venta_items i ON i.venta_id = v.id;

-- ---------------------------------------------------------------- DATOS DE EJEMPLO (opcional)
-- Quite los "--" de las 4 lineas siguientes si quiere productos de prueba.
-- INSERT INTO productos (codigo, producto, precio) VALUES (7790001, 'Leche entera 1L', 8500);
-- INSERT INTO productos (codigo, producto, precio) VALUES (7790002, 'Queso paraguay (kg)', 42000);
-- INSERT INTO productos (codigo, producto, precio) VALUES (7790003, 'Coca Cola 2L', 12000);
-- INSERT INTO productos (codigo, producto, precio) VALUES (7790004, 'Pan lactal', 9500);

-- ---------------------------------------------------------------- USUARIO DE APLICACION (opcional)
-- Recomendado cuando la base este en un servidor: que el programa NO use el superusuario "postgres".
-- Cambie la clave, quite los "--" y ejecute. Luego configure en el programa:
--   MINIMARKET_USER=minimarket_app   MINIMARKET_PASSWORD=<la clave>
-- CREATE ROLE minimarket_app LOGIN PASSWORD 'CAMBIAR_ESTA_CLAVE';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON productos, usuarios, ventas, venta_items TO minimarket_app;
-- GRANT SELECT ON v_ventas_detalle TO minimarket_app;
-- GRANT USAGE, SELECT ON SEQUENCE ventas_id_seq, venta_items_id_seq TO minimarket_app;

-- NOTA: no se crea ningun usuario de acceso aqui. La primera vez que abra el programa,
-- la pantalla de inicio le pedira crear el primero (usuario y clave numericos).
