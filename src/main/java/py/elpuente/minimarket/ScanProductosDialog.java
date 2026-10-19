package py.elpuente.minimarket;

import javax.swing.*;
import java.awt.*;

/** Consulta de precios por codigo (escaner) o por nombre. Equivale a Scan_productos. */
final class ScanProductosDialog extends BusquedaDialog {

    ScanProductosDialog(Window padre) {
        super(padre, "Escanear productos");

        // Con cada cambio en el codigo solo busca si es un numero valido
        Ui.alCambiar(txtCodigo, () -> {
            if (Ui.esEntero(txtCodigo.getText())) cargarGrilla();
        });
        txtCodigo.addActionListener(e -> {
            if (!Ui.esEntero(txtCodigo.getText())) {
                Ui.error(this, "Debes ingresar únicamente números válidos en el campo ID.");
                return;
            }
            cargarGrilla();
            txtCodigo.selectAll();
        });

        Ui.alCambiar(txtProd, this::cargarGrilla);
        txtProd.addActionListener(e -> {
            cargarGrilla();
            txtProd.selectAll();
        });

        iniciar();
    }
}
