package py.elpuente.minimarket;

import javax.swing.*;
import java.awt.*;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        aplicarTema();

        boolean primerUso;
        try {
            Esquema.asegurar();
            primerUso = !new UsuarioDao().hayUsuarios();
        } catch (Exception ex) {
            Ui.error(null, "No se pudo conectar a la base de datos.\n\n" + ex.getMessage()
                    + "\n\nVerifique que PostgreSQL esté activo y los datos de conexión\n"
                    + "(variables MINIMARKET_URL, MINIMARKET_USER, MINIMARKET_PASSWORD).");
            System.exit(1);
            return;
        }

        LoginDialog login = new LoginDialog(primerUso);
        login.setVisible(true);          // modal: espera hasta que se cierre
        if (!login.exitoso()) {
            System.exit(0);
        }
        new MenuPrincipal().setVisible(true);
    }

    private static void aplicarTema() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignorada) {
            // si falla, se usa el aspecto por defecto
        }
        UIManager.put("nimbusFocus", Ui.PRIMARIO);
        UIManager.put("nimbusSelectionBackground", Ui.PRIMARIO);
        UIManager.put("OptionPane.messageFont", Ui.fuente(12f, Font.PLAIN));
        UIManager.put("OptionPane.buttonFont", Ui.fuente(11f, Font.PLAIN));
    }
}
