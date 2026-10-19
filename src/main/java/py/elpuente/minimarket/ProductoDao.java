package py.elpuente.minimarket;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla productos. Reemplaza las consultas concatenadas del original
 * (consulta / executarsql) por PreparedStatement, que evita la inyeccion SQL.
 */
public class ProductoDao {

    private static final String SELECT = "SELECT codigo, producto, precio FROM productos";

    @FunctionalInterface
    private interface Binder { void bind(PreparedStatement ps) throws SQLException; }

    public List<Producto> listar() throws SQLException {
        return consultar(SELECT + " ORDER BY producto", ps -> { });
    }

    public List<Producto> buscarPorCodigo(String codigo) throws SQLException {
        return consultar(SELECT + " WHERE codigo = ?", ps -> ps.setString(1, codigo.trim()));
    }

    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        return consultar(SELECT + " WHERE producto ILIKE ?", ps -> ps.setString(1, like(texto)));
    }

    public boolean insertar(String codigo, String nombre, BigDecimal precio) throws SQLException {
        return ejecutar("INSERT INTO productos (codigo, producto, precio) VALUES (?, ?, ?)", ps -> {
            ps.setString(1, codigo.trim());
            ps.setString(2, nombre.trim());
            ps.setBigDecimal(3, precio);
        });
    }

    public boolean actualizarPrecioPorCodigo(String codigo, BigDecimal precio) throws SQLException {
        return ejecutar("UPDATE productos SET precio = ? WHERE codigo = ?", ps -> {
            ps.setBigDecimal(1, precio);
            ps.setString(2, codigo.trim());
        });
    }

    /** Igual que el original: actualiza TODOS los productos cuyo nombre contenga el texto. */
    public boolean actualizarPrecioPorNombre(String texto, BigDecimal precio) throws SQLException {
        return ejecutar("UPDATE productos SET precio = ? WHERE producto ILIKE ?", ps -> {
            ps.setBigDecimal(1, precio);
            ps.setString(2, like(texto));
        });
    }

    public boolean eliminarPorCodigo(String codigo) throws SQLException {
        return ejecutar("DELETE FROM productos WHERE codigo = ?", ps -> ps.setString(1, codigo.trim()));
    }

    // ---- helpers -------------------------------------------------------------------------

    private static String like(String texto) {
        return "%" + texto.trim() + "%";
    }

    private List<Producto> consultar(String sql, Binder binder) throws SQLException {
        try (Connection cn = Conexion.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Producto(rs.getString("codigo"), rs.getString("producto"), rs.getBigDecimal("precio")));
                }
                return out;
            }
        }
    }

    /** @return true si afecto al menos una fila (igual que executarsql del original). */
    private boolean ejecutar(String sql, Binder binder) throws SQLException {
        try (Connection cn = Conexion.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate() > 0;
        }
    }
}
