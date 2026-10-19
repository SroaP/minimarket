package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Ventana modal maximizada con fondo teal, boton "Volver" y logo (como las ventanas del original). */
abstract class BaseDialog extends JDialog {

    BaseDialog(Window padre, String titulo, String textoVolver) {
        super(padre, titulo, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Ui.TEAL);
        setLayout(new BorderLayout(10, 10));

        JPanel cabecera = Ui.transparente(new BorderLayout());
        cabecera.setBorder(new EmptyBorder(15, 20, 5, 20));
        JButton volver = Ui.boton(textoVolver, 13f);
        volver.addActionListener(e -> dispose());
        cabecera.add(volver, BorderLayout.WEST);
        cabecera.add(Ui.logo(1.5), BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
    }

    /** Fila "etiqueta + campo" para los formularios. */
    protected static void fila(JPanel panel, int y, String texto, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
        panel.add(Ui.etiqueta(texto, 14f), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        panel.add(campo, c);
    }

    /** Envuelve la tabla en un scroll con margenes (no la agrega a la ventana). */
    protected static JComponent tablaConMargen(JTable tabla) {
        JScrollPane sp = new JScrollPane(tabla);
        JPanel envoltorio = Ui.transparente(new BorderLayout());
        envoltorio.setBorder(new EmptyBorder(10, 40, 30, 40));
        envoltorio.add(sp, BorderLayout.CENTER);
        return envoltorio;
    }
}
