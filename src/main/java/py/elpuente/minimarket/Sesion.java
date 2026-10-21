package py.elpuente.minimarket;

/** Usuario que inicio sesion. */
final class Sesion {
    private static String usuario = "";
    private Sesion() { }
    static String usuario() { return usuario; }
    static void iniciar(String u) { usuario = u; }
}
