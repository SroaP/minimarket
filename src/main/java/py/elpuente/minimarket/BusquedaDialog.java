package py.elpuente.minimarket;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Base de Escanear / Editar precios / Eliminar: dos campos (codigo y nombre) y una grilla.
 * Replica cargar_grilla del original: si hay codigo busca por codigo; si hay nombre busca por
 * nombre (y este segundo resultado pisa al primero, igual que antes).
 */
abstract class BusquedaDialog extends BaseDialog {
    protected final ProductoDao dao = new ProductoDao();
    protected final JTextField txtCodigo = Ui.campo(18, 14f);
    protected final JTextField txtProd = Ui.campo(18, 14f);
    protected final ProductoTableModel modelo = new ProductoTableModel();
    protected final JTable tabla = Ui.tabla(modelo);
    protected final JPanel formulario = Ui.transparente(new GridBagLayout());

    BusquedaDialog(Window padre, String titulo) {
        super(padre, titulo, "Volver al menú");
        fila(formulario, 0, "Ingrese el código", txtCodigo);
        fila(formulario, 1, "Ingrese el nombre del producto", txtProd);
        JPanel arriba = Ui.transparente(new FlowLayout(FlowLayout.CENTER));
        arriba.add(formulario);
        JPanel contenedor = Ui.transparente(new BorderLayout());
        contenedor.add(arriba, BorderLayout.NORTH);
        contenedor.add(tablaConMargen(tabla), BorderLayout.CENTER);
        add(contenedor, BorderLayout.CENTER);
    }

    /** Debe llamarse al final del constructor de la subclase (ya con todos los campos creados). */
    protected void iniciar() {
        cargarGrilla();
        SwingUtilities.invokeLater(txtCodigo::requestFocusInWindow);
    }

    protected void cargarGrilla() {
        try {
            List<Producto> resultado = null;
            if (!txtCodigo.getText().trim().isEmpty()) {
                resultado = dao.buscarPorCodigo(txtCodigo.getText());
            }
            if (!txtProd.getText().trim().isEmpty()) {
                resultado = dao.buscarPorNombre(txtProd.getText());
            }
            if (resultado != null) modelo.setFilas(resultado);
        } catch (SQLException ex) {
            Ui.error(this, "Error al consultar la base de datos:\n" + ex.getMessage());
        }
    }

    protected void limpiar() {
        txtCodigo.setText("");
        txtProd.setText("");
    }
}
