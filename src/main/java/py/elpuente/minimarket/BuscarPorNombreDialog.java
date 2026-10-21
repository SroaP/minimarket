package py.elpuente.minimarket;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

/** Busqueda por nombre desde la venta, con cantidad (por kg o por unidad). Equivale a buscar_por_nombre. */
final class BuscarPorNombreDialog extends BaseDialog {
    private final ProductoDao dao = new ProductoDao();
    private final VentaDialog venta;
    private final JTextField txtProd = Ui.campo(34, 14f);
    private final JTextField txtCantidad = Ui.campo(10, 14f);
    private final ProductoTableModel modelo = new ProductoTableModel();
    private final JTable tabla = Ui.tabla(modelo);

    BuscarPorNombreDialog(Window padre, VentaDialog venta) {
        super(padre, "Buscar producto por nombre", "Volver");
        this.venta = venta;

        JButton agregar = Ui.boton("Agregar a la venta", 13f);
        txtCantidad.setText("1");

        JPanel form = Ui.tarjeta(new GridBagLayout());
        fila(form, 0, "Nombre del producto", txtProd);
        fila(form, 1, "Cantidad (ej. 0,5 para medio kilo)", txtCantidad);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 2; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(10, 0, 0, 4);
        form.add(agregar, c);

        JPanel arriba = Ui.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        arriba.add(form);
        contenido.add(arriba, BorderLayout.NORTH);
        contenido.add(Ui.tablaEnTarjeta(tabla), BorderLayout.CENTER);

        Ui.alCambiar(txtProd, this::cargarGrilla);
        agregar.addActionListener(e -> agregar());
        txtProd.addActionListener(e -> { if (modelo.getRowCount() > 0) tabla.setRowSelectionInterval(0, 0); });

        setBounds(0, 0, 1000, 720);
        setLocationRelativeTo(padre);
        SwingUtilities.invokeLater(txtProd::requestFocusInWindow);
    }

    private void cargarGrilla() {
        if (txtProd.getText().trim().isEmpty()) return;
        try {
            modelo.setFilas(dao.buscarPorNombre(txtProd.getText()));
        } catch (SQLException ex) {
            Ui.error(this, "Error al consultar la base de datos:\n" + ex.getMessage());
        }
    }

    private void agregar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.info(this, "Seleccione un producto de la lista");
            return;
        }
        Producto p = modelo.get(tabla.convertRowIndexToModel(fila));

        // Cantidad invalida (o no positiva) => 1, como el original cuando no se puede convertir
        double cantidad = 1;
        BigDecimal q = Ui.parseDecimal(txtCantidad.getText());
        if (q != null && q.signum() > 0) cantidad = q.doubleValue();

        venta.agregarDesdeBusqueda(p.codigo(), p.nombre(), p.precio(), cantidad);
        dispose();
    }
}
