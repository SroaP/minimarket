package py.elpuente.minimarket;

import javax.swing.*;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
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
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}
