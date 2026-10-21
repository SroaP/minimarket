package py.elpuente.minimarket;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

/**
 * Usuarios y contrasenas numericos. El usuario y la clave PUEDEN ser iguales.
 * Las claves nunca se guardan en claro: se almacena PBKDF2-HMAC-SHA256 con sal aleatoria.
 */
final class UsuarioDao {
    static final int MIN_DIGITOS = 4;
    static final int MAX_DIGITOS = 20;
    private static final int ITERACIONES = 120_000;

    /** Solo digitos, entre MIN_DIGITOS y MAX_DIGITOS. */
    static boolean valido(String s) {
        return s != null && s.matches("[0-9]{" + MIN_DIGITOS + "," + MAX_DIGITOS + "}");
    }

    boolean hayUsuarios() throws SQLException {
        try (Connection cn = Conexion.abrir(); Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT EXISTS (SELECT 1 FROM usuarios)")) {
            rs.next();
            return rs.getBoolean(1);
        }
    }

    /** @return false si el usuario ya existe. */
    boolean crear(String usuario, String clave) throws SQLException {
        if (!valido(usuario) || !valido(clave)) throw new IllegalArgumentException("Usuario y clave deben ser numericos");
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        String sql = "INSERT INTO usuarios (usuario, salt, hash) VALUES (?, ?, ?)";
        try (Connection cn = Conexion.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, Base64.getEncoder().encodeToString(sal));
            ps.setString(3, derivar(clave, sal));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) return false;   // unique_violation: el usuario ya existe
            throw e;
        }
    }

    boolean verificar(String usuario, String clave) throws SQLException {
        if (!valido(usuario) || !valido(clave)) return false;
        try (Connection cn = Conexion.abrir();
             PreparedStatement ps = cn.prepareStatement("SELECT salt, hash FROM usuarios WHERE usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                byte[] sal = Base64.getDecoder().decode(rs.getString("salt"));
                byte[] esperado = rs.getString("hash").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                byte[] actual = derivar(clave, sal).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                return MessageDigest.isEqual(esperado, actual);
            }
        }
    }

    private static String derivar(String clave, byte[] sal) {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, ITERACIONES, 256);
        try {
            byte[] h = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(h);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        } finally {
            spec.clearPassword();
        }
    }
}
