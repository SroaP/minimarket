package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/** Ventana modal maximizada: cabecera blanca con "Volver", titulo y logo, y area de contenido gris claro. */
abstract class BaseDialog extends JDialog {
    /** Area donde cada pantalla coloca sus tarjetas. */
    protected final JPanel contenido = new JPanel(new BorderLayout(20, 16));

    BaseDialog(Window padre, String titulo, String textoVolver) {
        super(padre, titulo, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Ui.FONDO);
        setLayout(new BorderLayout());

        JPanel cabecera = new JPanel(new BorderLayout(16, 0));
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, Ui.BORDE), new EmptyBorder(12, 24, 12, 24)));
        JButton volver = Ui.boton("←  " + textoVolver, 12f, Ui.Estilo.NEUTRO);
        volver.addActionListener(e -> dispose());
        JPanel izquierda = Ui.transparente(new FlowLayout(FlowLayout.LEFT, 18, 0));
        izquierda.add(volver);
        izquierda.add(Ui.etiqueta(titulo, 18f, Font.BOLD, Ui.PRIMARIO_OSC));
        cabecera.add(izquierda, BorderLayout.WEST);
        cabecera.add(Ui.logo(1.3), BorderLayout.EAST);

        contenido.setBackground(Ui.FONDO);
        contenido.setBorder(new EmptyBorder(22, 30, 26, 30));
        add(cabecera, BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);

        setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
    }

    /** Fila "etiqueta + campo" para los formularios (GridBagLayout). */
    protected static void fila(JPanel panel, int y, String texto, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.insets = new Insets(8, 4, 8, 14);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(Ui.etiqueta(texto, 13f, Font.PLAIN, Ui.GRIS), c);
        c.gridx = 1;
        c.insets = new Insets(8, 0, 8, 4);
        panel.add(campo, c);
    }
}
