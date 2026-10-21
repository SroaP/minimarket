package py.elpuente.minimarket;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Elimina un producto por codigo. Equivale a Eliminar_productos. */
final class EliminarProductosDialog extends BusquedaDialog {

    EliminarProductosDialog(Window padre) {
        super(padre, "Eliminar productos");

        JButton eliminar = Ui.boton("Eliminar producto", 13f, Ui.Estilo.PELIGRO);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 2; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(10, 6, 6, 6);
        formulario.add(eliminar, c);

        Ui.alCambiar(txtCodigo, this::cargarGrilla);
        Ui.alCambiar(txtProd, this::cargarGrilla);
        eliminar.addActionListener(e -> eliminar());

        iniciar();
    }

    private void eliminar() {
        txtCodigo.requestFocusInWindow();
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            Ui.error(this, "Seleccione una opcion");
            return;
        }
        if (!Ui.confirmar(this, "Estas seguro de que desea eliminar el registro?", Ui.TITULO)) return;

        try {
            if (dao.eliminarPorCodigo(codigo)) {
                cargarGrilla();
                Ui.info(this, "Registro eliminado con exito");
                limpiar();
            } else {
                Ui.error(this, "Error al eliminar");
            }
        } catch (SQLException ex) {
            Ui.error(this, "Error al eliminar:\n" + ex.getMessage());
        }
    }
}
