package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/** Pantalla de venta: escanea codigos, suma cantidades y calcula el total. Equivale a sumar_productos. */
final class VentaDialog extends BaseDialog {
    private final ProductoDao dao = new ProductoDao();
    private final Carrito carrito = new Carrito();
    private final VentaTableModel modelo = new VentaTableModel(carrito);
    private final JTable tabla = Ui.tabla(modelo);
    private final JTextField txtCodigo = Ui.campo(22, 14f);
    private final JTextField txtTotal = Ui.campo(12, 18f);

    VentaDialog(Window padre) {
        super(padre, "Venta", "Volver al menu");

        txtCodigo.setFont(Ui.fuente(14f, Font.BOLD));
        txtTotal.setFont(Ui.fuente(18f, Font.BOLD));
        txtTotal.setEditable(false);
        txtTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        txtTotal.setBackground(new Color(185, 209, 234));

        // Arriba: codigo
        JPanel codigo = Ui.transparente(new FlowLayout(FlowLayout.CENTER, 12, 8));
        codigo.add(Ui.etiqueta("Ingrese el codigo:", 14f));
        codigo.add(txtCodigo);

        // Derecha: total y cobrar
        JButton cobrar = Ui.boton("COBRAR", 12f);
        cobrar.setPreferredSize(new Dimension(180, 70));
        JPanel derecha = Ui.transparente(new GridBagLayout());
        derecha.setBorder(new EmptyBorder(10, 10, 10, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.insets = new Insets(8, 8, 8, 8);
        c.gridy = 0; derecha.add(Ui.etiqueta("TOTAL", 16f), c);
        c.gridy = 1; derecha.add(txtTotal, c);
        c.gridy = 2; c.insets = new Insets(40, 8, 8, 8); derecha.add(cobrar, c);

        // Abajo: botones de la grilla
        JButton buscar = Ui.boton("Buscar por nombre o agregar por kg", 11f);
        JButton quitar = Ui.boton("Eliminar producto", 11f);
        JPanel abajo = Ui.transparente(new FlowLayout(FlowLayout.LEFT, 12, 8));
        abajo.setBorder(new EmptyBorder(0, 30, 15, 0));
        abajo.add(buscar);
        abajo.add(quitar);

        JPanel centro = Ui.transparente(new BorderLayout());
        centro.add(codigo, BorderLayout.NORTH);
        JPanel grilla = Ui.transparente(new BorderLayout());
        grilla.setBorder(new EmptyBorder(5, 40, 5, 10));
        grilla.add(new JScrollPane(tabla), BorderLayout.CENTER);
        centro.add(grilla, BorderLayout.CENTER);
        centro.add(abajo, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
        add(derecha, BorderLayout.EAST);

        // Enter en el codigo agrega el producto (el JTextField ya evita el pitido)
        txtCodigo.addActionListener(e -> {
            String cod = txtCodigo.getText().trim();
            if (!cod.isEmpty()) {
                agregarProducto(cod);
                txtCodigo.setText("");
                txtCodigo.requestFocusInWindow();
            }
        });
        buscar.addActionListener(e -> new BuscarPorNombreDialog(this, this).setVisible(true));
        quitar.addActionListener(e -> quitarSeleccionado());
        cobrar.addActionListener(e -> {
            carrito.vaciar();
            modelo.refrescar();
            txtTotal.setText("");
        });

        SwingUtilities.invokeLater(txtCodigo::requestFocusInWindow);
    }

    private void agregarProducto(String codigo) {
        try {
            List<Producto> r = dao.buscarPorCodigo(codigo);
            if (r.isEmpty()) {
                Ui.aviso(this, "Producto no encontrado en el sistema.");
                return;
            }
            Producto p = r.get(0);
            carrito.agregar(codigo, p.nombre(), p.precio(), 1);
            actualizar();
        } catch (Exception ex) {
            Ui.error(this, "Error al consultar el producto: " + ex.getMessage());
        }
    }

    /** Lo llama la ventana de busqueda por nombre. */
    void agregarDesdeBusqueda(String codigo, String nombre, BigDecimal precio, double cantidad) {
        carrito.agregar(codigo, nombre, precio, cantidad);
        actualizar();
    }

    private void quitarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.info(this, "Seleccione un producto para eliminar");
            return;
        }
        carrito.quitar(tabla.convertRowIndexToModel(fila));
        actualizar();
    }

    private void actualizar() {
        modelo.refrescar();
        txtTotal.setText("Total: " + Ui.formatoEntero(carrito.total()));
    }
}
