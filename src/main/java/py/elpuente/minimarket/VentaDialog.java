package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Pantalla de venta: escanea codigos, suma cantidades, calcula el total y registra la venta al cobrar. */
final class VentaDialog extends BaseDialog {
    private final ProductoDao dao = new ProductoDao();
    private final VentaDao ventaDao = new VentaDao();
    private final Carrito carrito = new Carrito();
    private final VentaTableModel modelo = new VentaTableModel(carrito);
    private final JTable tabla = Ui.tabla(modelo);
    private final JTextField txtCodigo = Ui.campo(22, 17f);
    private final JLabel lblTotal = Ui.etiqueta("Gs. 0", 30f, Font.BOLD, Ui.PRIMARIO);
    /** Ultima venta cobrada, solo en memoria. null = no hay ticket disponible. */
    private Ticket ultimoTicket;
    private final JButton btnImprimir = Ui.boton("Imprimir ticket", 13f, Ui.Estilo.NEUTRO);
    private final JLabel lblItems = Ui.etiqueta("Sin productos", 12f, Font.PLAIN, Ui.GRIS);

    VentaDialog(Window padre) {
        super(padre, "Venta", "Volver al menú");

        // Izquierda: codigo + grilla
        Ui.Tarjeta codigo = Ui.tarjeta(new BorderLayout(16, 0));
        codigo.add(Ui.etiqueta("Escanee o ingrese el código", 13f, Font.PLAIN, Ui.GRIS), BorderLayout.WEST);
        codigo.add(txtCodigo, BorderLayout.CENTER);
        JPanel izquierda = Ui.transparente(new BorderLayout(0, 16));
        izquierda.add(codigo, BorderLayout.NORTH);
        izquierda.add(Ui.tablaEnTarjeta(tabla), BorderLayout.CENTER);

        // Derecha: total y acciones
        JButton cobrar = Ui.boton("COBRAR", 16f);
        JButton buscar = Ui.boton("Buscar por nombre / agregar por kg", 11f, Ui.Estilo.NEUTRO);
        JButton quitar = Ui.boton("Quitar producto seleccionado", 11f, Ui.Estilo.PELIGRO);
        Ui.Tarjeta derecha = Ui.tarjeta(new GridBagLayout());
        derecha.setPreferredSize(new Dimension(360, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;
        c.gridy = 0; derecha.add(Ui.etiqueta("TOTAL A COBRAR", 12f, Font.BOLD, Ui.GRIS), c);
        c.gridy = 1; c.insets = new Insets(6, 0, 0, 0); derecha.add(lblTotal, c);
        c.gridy = 2; derecha.add(lblItems, c);
        c.gridy = 3; c.weighty = 1; derecha.add(Box.createVerticalGlue(), c);
        c.weighty = 0;
        btnImprimir.setEnabled(false);
        c.gridy = 4; c.insets = new Insets(0, 0, 10, 0); derecha.add(btnImprimir, c);
        c.gridy = 5; cobrar.setBorder(new EmptyBorder(18, 10, 18, 10)); derecha.add(cobrar, c);
        c.gridy = 6; derecha.add(buscar, c);
        c.gridy = 7; c.insets = new Insets(0, 0, 0, 0); derecha.add(quitar, c);

        contenido.add(izquierda, BorderLayout.CENTER);
        contenido.add(derecha, BorderLayout.EAST);

        // Enter en el codigo agrega el producto
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
        cobrar.addActionListener(e -> cobrar());
        btnImprimir.addActionListener(e -> {
            if (ultimoTicket != null) new TicketDialog(this, ultimoTicket).setVisible(true);
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
            descartarTicketSiVentaNueva();
            carrito.agregar(codigo, p.nombre(), p.precio(), 1);
            actualizar();
        } catch (Exception ex) {
            Ui.error(this, "Error al consultar el producto: " + ex.getMessage());
        }
    }

    /** Lo llama la ventana de busqueda por nombre. */
    void agregarDesdeBusqueda(String codigo, String nombre, BigDecimal precio, double cantidad) {
        descartarTicketSiVentaNueva();
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

    /** Registra la venta en la base de datos (para los reportes) y limpia la pantalla. */
    private void cobrar() {
        if (carrito.lineas().isEmpty()) {
            Ui.aviso(this, "No hay productos en la venta.");
            return;
        }
        BigDecimal total = carrito.total();
        if (!Ui.confirmar(this, "¿Cobrar " + Ui.guaranies(total) + "?", "Confirmar cobro")) return;
        try {
            long id = ventaDao.guardar(new ArrayList<>(carrito.lineas()), total, Sesion.usuario());
            // Guarda el ticket en memoria (sobreescribe el anterior); solo se muestra al pulsar "Imprimir ticket"
            ultimoTicket = Ticket.crear(id, LocalDateTime.now(), Sesion.usuario(), carrito.lineas(), total);
            btnImprimir.setEnabled(true);
            carrito.vaciar();
            actualizar();
            Ui.info(this, "Venta N° " + id + " registrada.\nTotal cobrado: " + Ui.guaranies(total)
                    + "\n\nPulse \"Imprimir ticket\" para ver el ticket.");
            txtCodigo.requestFocusInWindow();
        } catch (SQLException ex) {
            Ui.error(this, "No se pudo registrar la venta:\n" + ex.getMessage());
        }
    }

    /** Al empezar una venta nueva (carrito vacio) el ticket anterior sin imprimir se descarta. */
    private void descartarTicketSiVentaNueva() {
        if (carrito.lineas().isEmpty() && ultimoTicket != null) {
            ultimoTicket = null;
            btnImprimir.setEnabled(false);
        }
    }

    private void actualizar() {
        modelo.refrescar();
        lblTotal.setText(Ui.guaranies(carrito.total()));
        int n = carrito.lineas().size();
        lblItems.setText(n == 0 ? "Sin productos" : n + (n == 1 ? " producto" : " productos"));
    }
}
