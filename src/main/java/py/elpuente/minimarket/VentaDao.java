package py.elpuente.minimarket;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Guarda las ventas cobradas y las consulta por rango de fechas. */
final class VentaDao {

    /** Guarda la venta y sus items en una sola transaccion. @return id de la venta. */
    long guardar(List<LineaVenta> lineas, BigDecimal total, String usuario, List<Pago> pagos) throws SQLException {
        try (Connection cn = Conexion.abrir()) {
            cn.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO ventas (fecha, usuario, total) VALUES (?, ?, ?) RETURNING id")) {
                    ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(2, usuario);
                    ps.setBigDecimal(3, total);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        id = rs.getLong(1);
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO venta_items (venta_id, codigo, producto, precio, cantidad, subtotal)"
                                + " VALUES (?, ?, ?, ?, ?, ?)")) {
                    for (LineaVenta l : lineas) {
                        ps.setLong(1, id);
                        ps.setString(2, l.codigo());
                        ps.setString(3, l.nombre());
                        ps.setBigDecimal(4, l.precio());
                        ps.setBigDecimal(5, BigDecimal.valueOf(l.cantidad()));
                        ps.setBigDecimal(6, l.subtotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO venta_pagos (venta_id, metodo, monto, recibido) VALUES (?, ?, ?, ?)")) {
                    for (Pago p : pagos) {
                        ps.setLong(1, id);
                        ps.setString(2, p.metodo().name());
                        ps.setBigDecimal(3, p.monto());
                        ps.setBigDecimal(4, p.recibido());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                cn.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /** Total cobrado por metodo de pago en el periodo (efectivo ya descontado el vuelto). */
    java.util.Map<String, BigDecimal> totalesPorMetodo(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT p.metodo, SUM(p.monto) FROM venta_pagos p JOIN ventas v ON v.id = p.venta_id"
                + " WHERE v.fecha >= ? AND v.fecha < ? GROUP BY p.metodo ORDER BY p.metodo";
        try (Connection cn = Conexion.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                java.util.Map<String, BigDecimal> out = new java.util.LinkedHashMap<>();
                while (rs.next()) {
                    String m = rs.getString(1);
                    String nombre;
                    try { nombre = MetodoPago.valueOf(m).toString(); } catch (IllegalArgumentException e) { nombre = m; }
                    out.put(nombre, rs.getBigDecimal(2));
                }
                return out;
            }
        }
    }

    /** Ventas con fecha en [desde, hasta). */
    List<VentaResumen> ventas(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT id, fecha, usuario, total FROM ventas WHERE fecha >= ? AND fecha < ? ORDER BY fecha, id";
        try (Connection cn = Conexion.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                List<VentaResumen> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new VentaResumen(rs.getLong("id"), rs.getTimestamp("fecha").toLocalDateTime(),
                            rs.getString("usuario"), rs.getBigDecimal("total")));
                }
                return out;
            }
        }
    }

    /** Una fila por producto vendido, con los datos de su venta. */
    List<VentaDetalle> detalle(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT v.id, v.fecha, v.usuario, i.codigo, i.producto, i.precio, i.cantidad, i.subtotal"
                + " FROM ventas v JOIN venta_items i ON i.venta_id = v.id"
                + " WHERE v.fecha >= ? AND v.fecha < ? ORDER BY v.fecha, v.id, i.id";
        try (Connection cn = Conexion.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                List<VentaDetalle> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new VentaDetalle(rs.getLong(1), rs.getTimestamp(2).toLocalDateTime(), rs.getString(3),
                            rs.getString(4), rs.getString(5), rs.getBigDecimal(6), rs.getBigDecimal(7),
                            rs.getBigDecimal(8)));
                }
                return out;
            }
        }
    }
}
