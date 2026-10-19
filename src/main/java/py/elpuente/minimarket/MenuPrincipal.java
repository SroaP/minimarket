package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Menu principal. Equivale a menu_principal. */
public final class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        super(Ui.TITULO);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { salir(); }
        });
        getContentPane().setBackground(Ui.TEAL);
        setLayout(new BorderLayout());

        JButton salir = Ui.boton("Salir", 16f);
        salir.setPreferredSize(new Dimension(200, 60));
        salir.addActionListener(e -> salir());
        JPanel arriba = Ui.transparente(new FlowLayout(FlowLayout.LEFT, 40, 30));
        arriba.add(salir);
        add(arriba, BorderLayout.NORTH);

        JPanel botones = Ui.transparente(new GridLayout(0, 1, 0, 12));
        botones.setBorder(new EmptyBorder(20, 60, 40, 40));
        botones.add(menu("Venta", () -> new VentaDialog(this)));
        botones.add(menu("Escanear Productos", () -> new ScanProductosDialog(this)));
        botones.add(menu("Cargar Productos", () -> new CargarProductosDialog(this)));
        botones.add(menu("Editar Precios", () -> new EditarPreciosDialog(this)));
        botones.add(menu("Eliminar Productos", () -> new EliminarProductosDialog(this)));
        JPanel izquierda = Ui.transparente(new BorderLayout());
        izquierda.add(botones, BorderLayout.NORTH);
        add(izquierda, BorderLayout.WEST);

        JPanel logo = Ui.transparente(new GridBagLayout());
        logo.add(Ui.logo(4));
        add(logo, BorderLayout.CENTER);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
    }

    private JButton menu(String texto, java.util.function.Supplier<JDialog> ventana) {
        JButton b = Ui.boton(texto, 16f);
        b.setPreferredSize(new Dimension(333, 90));
        b.addActionListener(e -> ventana.get().setVisible(true));
        return b;
    }

    private void salir() {
        if (Ui.confirmar(this, "¿Estás seguro de que desea salir del sistema?", "Confirmar salida")) {
            System.exit(0);
        }
    }
}
