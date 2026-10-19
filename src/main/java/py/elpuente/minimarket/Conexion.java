package py.elpuente.minimarket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Equivale al modulo "Conexión" del original. Los valores por defecto son los del
 * original (postgres/root @ localhost:5432, base MINIMARKET) y se pueden sobreescribir
 * con las variables de entorno MINIMARKET_URL, MINIMARKET_USER y MINIMARKET_PASSWORD.
 */
public final class Conexion {

    private static final String URL = env("MINIMARKET_URL", "jdbc:postgresql://localhost:5432/MINIMARKET");
    private static final String USUARIO = env("MINIMARKET_USER", "postgres");
    private static final String CLAVE = env("MINIMARKET_PASSWORD", "root");

    private Conexion() { }

    public static Connection abrir() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USUARIO);
        props.setProperty("password", CLAVE);
        // El codigo puede ser numerico o texto en la BD: dejamos que PostgreSQL infiera el tipo
        // de los parametros String, asi las consultas parametrizadas funcionan con ambos.
        props.setProperty("stringtype", "unspecified");
        return DriverManager.getConnection(URL, props);
    }

    private static String env(String nombre, String porDefecto) {
        String v = System.getenv(nombre);
        return (v == null || v.isBlank()) ? porDefecto : v;
    }
}
