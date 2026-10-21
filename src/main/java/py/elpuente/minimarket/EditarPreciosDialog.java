package py.elpuente.minimarket;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

/** Cambia el precio por codigo o por nombre. Equivale a editar_precios. */
final class EditarPreciosDialog extends BusquedaDialog {
    private final JTextField txtPrecio = Ui.campo(24, 14f);

    EditarPreciosDialog(Window padre) {
        super(padre, "Editar precios");

        fila(formulario, 2, "Nuevo precio", txtPrecio);
        JButton editar = Ui.boton("Guardar nuevo precio", 13f);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 3; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(10, 6, 6, 6);
        formulario.add(editar, c);

        Ui.alCambiar(txtCodigo, this::cargarGrilla);
        Ui.alCambiar(txtProd, this::cargarGrilla);
        editar.addActionListener(e -> editar());

        iniciar();
    }

    private void editar() {
        txtCodigo.requestFocusInWindow();
        String codigo = txtCodigo.getText().trim();
        String nombre = txtProd.getText().trim();

        if (codigo.isEmpty() && nombre.isEmpty()) {
            Ui.error(this, "Seleccione una opcion");
            return;
        }
        BigDecimal precio = Ui.parseDecimal(txtPrecio.getText());
        if (precio == null) {
            Ui.aviso(this, "Ingrese un precio válido.");
            return;
        }
        if (!Ui.confirmar(this, "Estas seguro de que desea cambiar el precio?", Ui.TITULO)) return;

        try {
            boolean ok = !codigo.isEmpty()
                    ? dao.actualizarPrecioPorCodigo(codigo, precio)
                    : dao.actualizarPrecioPorNombre(nombre, precio);
            if (ok) {
                cargarGrilla();
                Ui.info(this, "Registro cambiado con exito");
                limpiar();
                txtPrecio.setText("");
            } else {
                Ui.error(this, "Error al cambiar");
            }
        } catch (SQLException ex) {
            Ui.error(this, "Error al cambiar:\n" + ex.getMessage());
        }
    }
}
