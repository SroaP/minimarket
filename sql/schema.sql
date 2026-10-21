-- Esquema INFERIDO del codigo VB.NET original (no venia en el RAR).
-- Ajustalo si tu tabla real es distinta; el programa solo usa productos(codigo, producto, precio).
-- Base de datos del original: "MINIMARKET" (con comillas, en mayusculas).
CREATE TABLE IF NOT EXISTS productos (
    codigo   BIGINT PRIMARY KEY,
    producto VARCHAR(200) NOT NULL,
    precio   NUMERIC(14,2) NOT NULL
);

-- Las tablas usuarios, ventas y venta_items las crea el programa solo al iniciar
-- (CREATE TABLE IF NOT EXISTS), no hace falta ejecutarlas a mano.
