package py.elpuente.minimarket;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

/** Alta de productos. Equivale a Cargar_Productos. */
final class CargarProductosDialog extends BaseDialog {
    private final ProductoDao dao = new ProductoDao();
    private final JTextField txtCodigo = Ui.campo(14, 14f);
    private final JTextField txtProducto = Ui.campo(24, 14f);
    private final JTextField txtPrecio = Ui.campo(12, 14f);
    private final JButton btnNuevo = Ui.boton("Nuevo", 13f, Ui.Estilo.NEUTRO);
    private final JButton btnGuardar = Ui.boton("Guardar", 13f);
    private final ProductoTableModel modelo = new ProductoTableModel();

    CargarProductosDialog(Window padre) {
        super(padre, "Cargar productos", "Volver al menú");

        JPanel formulario = Ui.tarjeta(new GridBagLayout());
        agregar(formulario, 0, "Código", txtCodigo);
        agregar(formulario, 1, "Nombre del producto", txtProducto);
        agregar(formulario, 2, "Precio", txtPrecio);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 3; c.gridy = 1; c.anchor = GridBagConstraints.SOUTH; c.insets = new Insets(0, 10, 0, 0);
        JPanel botones = Ui.transparente(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        formulario.add(botones, c);

        JPanel arriba = Ui.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        arriba.add(formulario);
        contenido.add(arriba, BorderLayout.NORTH);
        contenido.add(Ui.tablaEnTarjeta(Ui.tabla(modelo)), BorderLayout.CENTER);

        // Enter pasa al siguiente campo; en el precio, guarda
        txtCodigo.addActionListener(e -> txtProducto.requestFocusInWindow());
        txtProducto.addActionListener(e -> txtPrecio.requestFocusInWindow());
        txtPrecio.addActionListener(e -> btnGuardar.doClick());

        btnNuevo.addActionListener(e -> {
            txtCodigo.setText("");
            txtProducto.setText("");
            txtPrecio.setText("");
            txtCodigo.requestFocusInWindow();
        });
        btnGuardar.addActionListener(e -> guardar());

        cargarGrilla();
        SwingUtilities.invokeLater(txtCodigo::requestFocusInWindow);
    }

    private static void agregar(JPanel p, int x, String titulo, JTextField campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0, 0, 6, 12);
        p.add(Ui.etiqueta(titulo, 13f, Font.PLAIN, Ui.GRIS), c);
        c.gridy = 1; c.insets = new Insets(0, 0, 0, 12);
        p.add(campo, c);
    }

    private void cargarGrilla() {
        try {
            modelo.setFilas(dao.listar());
        } catch (SQLException ex) {
            Ui.error(this, "Error al consultar la base de datos:\n" + ex.getMessage());
        }
    }

    private void guardar() {
        if (txtCodigo.getText().trim().isEmpty()) {
            Ui.info(this, "Cargue el codigo");
        } else if (txtProducto.getText().trim().isEmpty()) {
            Ui.info(this, "Cargue el nombre del producto");
        } else if (txtPrecio.getText().trim().isEmpty()) {
            Ui.info(this, "Cargue el precio");
        } else {
            BigDecimal precio = Ui.parseDecimal(txtPrecio.getText());
            if (precio == null) {
                Ui.aviso(this, "Ingrese un precio válido.");
                return;
            }
            try {
                if (dao.insertar(txtCodigo.getText(), txtProducto.getText(), precio)) {
                    Ui.info(this, "registro guardado con exito");
                    cargarGrilla();
                    btnNuevo.requestFocusInWindow();
                } else {
                    Ui.info(this, "registro no guardado");
                }
            } catch (SQLException ex) {
                // p. ej. codigo duplicado o codigo no numerico
                Ui.error(this, "registro no guardado:\n" + ex.getMessage());
            }
        }
    }
}
