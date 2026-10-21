package py.elpuente.minimarket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Red de seguridad: si faltan las tablas usuarios, ventas o venta_items las crea (misma definicion que
 * sql/minimarket_db.sql). Funciona en PostgreSQL 8.x en adelante. La tabla productos no se toca.
 */
final class Esquema {
    private Esquema() { }

    static void asegurar() throws SQLException {
        try (Connection cn = Conexion.abrir(); Statement st = cn.createStatement()) {
            if (!existe(cn, "usuarios")) {
                st.execute("CREATE TABLE usuarios ("
                    + " usuario VARCHAR(20) PRIMARY KEY CHECK (usuario ~ '^[0-9]+$'),"
                    + " salt VARCHAR(64) NOT NULL,"
                    + " hash VARCHAR(128) NOT NULL,"
                    + " creado TIMESTAMP NOT NULL DEFAULT now())");
            }
            if (!existe(cn, "ventas")) {
                st.execute("CREATE TABLE ventas ("
                    + " id BIGSERIAL PRIMARY KEY,"
                    + " fecha TIMESTAMP NOT NULL,"
                    + " usuario VARCHAR(20),"
                    + " total NUMERIC(14,2) NOT NULL)");
                st.execute("CREATE INDEX ventas_fecha_idx ON ventas (fecha)");
            }
            if (!existe(cn, "venta_items")) {
                st.execute("CREATE TABLE venta_items ("
                    + " id BIGSERIAL PRIMARY KEY,"
                    + " venta_id BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,"
                    + " codigo VARCHAR(50) NOT NULL,"
                    + " producto VARCHAR(200) NOT NULL,"
                    + " precio NUMERIC(14,2) NOT NULL,"
                    + " cantidad NUMERIC(12,3) NOT NULL,"
                    + " subtotal NUMERIC(14,2) NOT NULL)");
                st.execute("CREATE INDEX venta_items_venta_idx ON venta_items (venta_id)");
            }
            if (!existe(cn, "venta_pagos")) {
                st.execute("CREATE TABLE venta_pagos ("
                    + " id BIGSERIAL PRIMARY KEY,"
                    + " venta_id BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,"
                    + " metodo VARCHAR(20) NOT NULL,"
                    + " monto NUMERIC(14,2) NOT NULL,"
                    + " recibido NUMERIC(14,2) NOT NULL)");
                st.execute("CREATE INDEX venta_pagos_venta_idx ON venta_pagos (venta_id)");
            }
        }
    }

    private static boolean existe(Connection cn, String tabla) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?")) {
            ps.setString(1, tabla);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
