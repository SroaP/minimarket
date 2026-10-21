package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.function.Supplier;

/** Menu principal con mosaicos. Equivale a menu_principal. */
public final class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        super(Ui.TITULO);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { salir(); }
        });
        getContentPane().setBackground(Ui.FONDO);
        setLayout(new BorderLayout());

        // Cabecera
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, Ui.BORDE), new EmptyBorder(14, 32, 14, 32)));
        cabecera.add(Ui.logo(1.6), BorderLayout.WEST);
        JButton salir = Ui.boton("Salir", 12f, Ui.Estilo.NEUTRO);
        salir.addActionListener(e -> salir());
        JPanel derecha = Ui.transparente(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        derecha.add(Ui.etiqueta("Usuario: " + Sesion.usuario(), 13f, Font.BOLD, Ui.PRIMARIO_OSC));
        derecha.add(salir);
        cabecera.add(derecha, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        // Mosaicos
        JPanel rejilla = Ui.transparente(new GridLayout(0, 3, 22, 22));
        rejilla.add(mosaico("$", "Venta", "Registrar y cobrar una venta", new Color(15, 118, 110), () -> new VentaDialog(this)));
        rejilla.add(mosaico("#", "Escanear productos", "Consultar precios", new Color(37, 99, 235), () -> new ScanProductosDialog(this)));
        rejilla.add(mosaico("+", "Cargar productos", "Agregar productos nuevos", new Color(22, 163, 74), () -> new CargarProductosDialog(this)));
        rejilla.add(mosaico("%", "Editar precios", "Cambiar el precio de un producto", new Color(217, 119, 6), () -> new EditarPreciosDialog(this)));
        rejilla.add(mosaico("×", "Eliminar productos", "Quitar productos del sistema", new Color(220, 38, 38), () -> new EliminarProductosDialog(this)));
        rejilla.add(mosaico("↓", "Exportar ventas", "Reportes por período", new Color(124, 58, 237), () -> new ExportarVentasDialog(this)));
        rejilla.add(mosaico("@", "Usuarios", "Crear usuarios de acceso", new Color(71, 85, 105), () -> new UsuarioDialog(this)));

        JPanel centro = Ui.transparente(new GridBagLayout());
        JPanel bloque = Ui.transparente(new BorderLayout(0, 24));
        bloque.add(Ui.etiqueta("¿Qué desea hacer?", 22f, Font.BOLD, Ui.TEXTO), BorderLayout.NORTH);
        bloque.add(rejilla, BorderLayout.CENTER);
        centro.add(bloque);
        add(centro, BorderLayout.CENTER);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 640));
        setLocationRelativeTo(null);
    }

    private JButton mosaico(String icono, String titulo, String detalle, Color acento, Supplier<JDialog> ventana) {
        JButton b = new Ui.Mosaico(icono, titulo, detalle, acento);
        b.addActionListener(e -> ventana.get().setVisible(true));
        return b;
    }

    private void salir() {
        if (Ui.confirmar(this, "¿Estás seguro de que desea salir del sistema?", "Confirmar salida")) {
            System.exit(0);
        }
    }
}
