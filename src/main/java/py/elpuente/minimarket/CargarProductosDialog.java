package py.elpuente.minimarket;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

/** Alta de productos. Equivale a Cargar_Productos. */
final class CargarProductosDialog extends BaseDialog {
    private final ProductoDao dao = new ProductoDao();
    private final JTextField txtCodigo = Ui.campo(14, 14f);
    private final JTextField txtProducto = Ui.campo(18, 14f);
    private final JTextField txtPrecio = Ui.campo(12, 14f);
    private final JButton btnNuevo = Ui.boton("Nuevo", 14f);
    private final JButton btnGuardar = Ui.boton("Guardar", 14f);
    private final ProductoTableModel modelo = new ProductoTableModel();

    CargarProductosDialog(Window padre) {
        super(padre, "Cargar productos", "Volver al menú");

        JPanel formulario = Ui.transparente(new FlowLayout(FlowLayout.CENTER, 20, 5));
        formulario.add(columna("Ingrese el código", txtCodigo));
        formulario.add(columna("Ingrese el nombre del producto", txtProducto));
        formulario.add(columna("Ingrese el precio", txtPrecio));

        JPanel botones = Ui.transparente(new FlowLayout(FlowLayout.CENTER, 20, 5));
        botones.add(btnNuevo);
        botones.add(btnGuardar);

        JPanel arriba = Ui.transparente(new BorderLayout());
        arriba.add(formulario, BorderLayout.NORTH);
        arriba.add(botones, BorderLayout.SOUTH);

        JPanel contenedor = Ui.transparente(new BorderLayout());
        contenedor.add(arriba, BorderLayout.NORTH);
        contenedor.add(tablaConMargen(Ui.tabla(modelo)), BorderLayout.CENTER);
        add(contenedor, BorderLayout.CENTER);

        // Enter pasa al siguiente campo; en el precio, al boton Guardar
        txtCodigo.addActionListener(e -> txtProducto.requestFocusInWindow());
        txtProducto.addActionListener(e -> txtPrecio.requestFocusInWindow());
        txtPrecio.addActionListener(e -> btnGuardar.requestFocusInWindow());

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

    private static JPanel columna(String titulo, JTextField campo) {
        JPanel p = Ui.transparente(new BorderLayout(0, 6));
        p.add(Ui.etiqueta(titulo, 14f), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
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
