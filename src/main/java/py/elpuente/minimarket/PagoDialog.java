package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Cobro: elegir metodo (F1 efectivo, F2 tarjeta, F3 QR), escribir lo que entrega el cliente y ver el vuelto.
 * Enter con el campo vacio paga exactamente lo que falta. Permite combinar metodos (ej. 5.000 con tarjeta
 * y el resto en efectivo). Con la venta completa, Enter confirma el cobro.
 */
final class PagoDialog extends JDialog {
    private final PlanPago plan;
    private MetodoPago metodo = MetodoPago.EFECTIVO;
    private boolean confirmado;

    private final JTextField txtMonto = Ui.campo(12, 22f);
    private final JLabel lblFalta = Ui.etiqueta(" ", 18f, Font.BOLD, Ui.TEXTO);
    private final JLabel lblVuelto = Ui.etiqueta(" ", 26f, Font.BOLD, new Color(22, 163, 74));
    private final JLabel lblAyuda = Ui.etiqueta(" ", 11f, Font.PLAIN, Ui.GRIS);
    private final SimpleTableModel modelo = new SimpleTableModel(
            new String[]{"método", "monto aplicado", "entregó"}, String.class, BigDecimal.class, BigDecimal.class);
    private final JTable tabla = Ui.tabla(modelo);
    private final JButton btnConfirmar = Ui.boton("Confirmar cobro", 14f);
    private final Map<MetodoPago, JToggleButton> botones = new EnumMap<>(MetodoPago.class);

    PagoDialog(Window padre, BigDecimal total) {
        super(padre, "Cobrar", ModalityType.APPLICATION_MODAL);
        this.plan = new PlanPago(total);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Ui.FONDO);
        setLayout(new BorderLayout());

        JPanel cuerpo = Ui.transparente(new GridBagLayout());
        cuerpo.setBorder(new EmptyBorder(20, 24, 12, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;

        // Total
        Ui.Tarjeta tTotal = Ui.tarjeta(new BorderLayout());
        tTotal.add(Ui.etiqueta("TOTAL A COBRAR", 12f, Font.BOLD, Ui.GRIS), BorderLayout.NORTH);
        tTotal.add(Ui.etiqueta(Ui.guaranies(total), 30f, Font.BOLD, Ui.PRIMARIO), BorderLayout.CENTER);
        c.gridy = 0; c.insets = new Insets(0, 0, 14, 0); cuerpo.add(tTotal, c);

        // Metodo
        JPanel metodos = Ui.transparente(new GridLayout(1, 3, 10, 0));
        ButtonGroup grupo = new ButtonGroup();
        for (MetodoPago m : MetodoPago.values()) {
            JToggleButton b = new Ui.Interruptor("F" + (m.ordinal() + 1) + "  " + m);
            b.setSelected(m == metodo);
            grupo.add(b);
            botones.put(m, b);
            metodos.add(b);
            b.addActionListener(e -> seleccionar(m));
        }
        c.gridy = 1; c.insets = new Insets(0, 0, 14, 0); cuerpo.add(metodos, c);

        // Monto
        Ui.Tarjeta tMonto = Ui.tarjeta(new BorderLayout(0, 8));
        tMonto.add(Ui.etiqueta("Monto que entrega el cliente", 12f, Font.PLAIN, Ui.GRIS), BorderLayout.NORTH);
        tMonto.add(txtMonto, BorderLayout.CENTER);
        tMonto.add(lblAyuda, BorderLayout.SOUTH);
        c.gridy = 2; cuerpo.add(tMonto, c);

        // Pagos cargados
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(new LineBorder(Ui.BORDE));
        sp.setPreferredSize(new Dimension(0, 130));
        JButton quitar = Ui.boton("Quitar pago seleccionado", 10f, Ui.Estilo.NEUTRO);
        quitar.addActionListener(e -> quitarSeleccionado());
        Ui.Tarjeta tPagos = Ui.tarjeta(new BorderLayout(0, 8));
        tPagos.add(sp, BorderLayout.CENTER);
        tPagos.add(quitar, BorderLayout.SOUTH);
        c.gridy = 3; c.insets = new Insets(0, 0, 14, 0); cuerpo.add(tPagos, c);

        // Falta / vuelto
        JPanel resumen = Ui.transparente(new GridLayout(2, 1, 0, 2));
        resumen.add(lblFalta);
        resumen.add(lblVuelto);
        c.gridy = 4; c.insets = new Insets(0, 4, 0, 0); cuerpo.add(resumen, c);

        // Botones
        JButton cancelar = Ui.boton("Cancelar", 13f, Ui.Estilo.NEUTRO);
        cancelar.addActionListener(e -> dispose());
        btnConfirmar.addActionListener(e -> confirmar());
        JPanel pie = Ui.transparente(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pie.setBorder(new EmptyBorder(0, 24, 20, 24));
        pie.add(cancelar);
        pie.add(btnConfirmar);

        add(cuerpo, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);

        // Enter: paga (campo vacio = exacto) o, si ya esta completo, confirma
        txtMonto.addActionListener(e -> {
            if (plan.completo()) confirmar(); else agregarPago();
        });
        Ui.alCambiar(txtMonto, this::vistaPrevia);
        atajo("F1", () -> botones.get(MetodoPago.EFECTIVO).doClick());
        atajo("F2", () -> botones.get(MetodoPago.TARJETA).doClick());
        atajo("F3", () -> botones.get(MetodoPago.QR).doClick());
        atajo("ESCAPE", this::dispose);

        actualizar();
        setSize(560, 800);
        setLocationRelativeTo(padre);
        SwingUtilities.invokeLater(txtMonto::requestFocusInWindow);
    }

    boolean confirmado() { return confirmado; }
    List<Pago> pagos() { return new ArrayList<>(plan.pagos()); }

    private void atajo(String tecla, Runnable accion) {
        JRootPane rp = getRootPane();
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), tecla);
        rp.getActionMap().put(tecla, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { accion.run(); }
        });
    }

    private void seleccionar(MetodoPago m) {
        metodo = m;
        botones.get(m).setSelected(true);
        txtMonto.requestFocusInWindow();
        vistaPrevia();
    }

    private void agregarPago() {
        String texto = txtMonto.getText().trim();
        BigDecimal recibido = null;                       // vacio => monto exacto
        if (!texto.isEmpty()) {
            recibido = Ui.parseDecimal(texto);
            if (recibido == null || recibido.signum() <= 0) {
                Ui.aviso(this, "Ingrese un monto válido.");
                return;
            }
        }
        try {
            plan.agregar(metodo, recibido);
        } catch (IllegalArgumentException ex) {
            Ui.aviso(this, ex.getMessage());
            return;
        }
        txtMonto.setText("");
        actualizar();
    }

    private void quitarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        plan.quitar(tabla.convertRowIndexToModel(fila));
        actualizar();
        txtMonto.requestFocusInWindow();
    }

    private void confirmar() {
        if (!plan.completo()) return;
        confirmado = true;
        dispose();
    }

    /** Muestra el vuelto mientras se escribe el efectivo recibido. */
    private void vistaPrevia() {
        BigDecimal v = plan.vuelto();
        BigDecimal r = Ui.parseDecimal(txtMonto.getText());
        if (metodo == MetodoPago.EFECTIVO && r != null && r.signum() > 0 && !plan.completo()) {
            v = plan.vueltoSiEfectivo(r);
        }
        lblVuelto.setText(v.signum() > 0 ? "VUELTO: " + Ui.guaranies(v) : " ");
    }

    private void actualizar() {
        List<Object[]> filas = new ArrayList<>();
        for (Pago p : plan.pagos()) filas.add(new Object[]{p.metodo().toString(), p.monto(), p.recibido()});
        modelo.setFilas(filas);

        if (plan.completo()) {
            lblFalta.setText("Pago completo");
            lblFalta.setForeground(new Color(22, 163, 74));
            lblAyuda.setText("Presione Enter para confirmar el cobro");
        } else {
            lblFalta.setText("Falta: " + Ui.guaranies(plan.restante()));
            lblFalta.setForeground(Ui.TEXTO);
            lblAyuda.setText("Enter sin escribir nada = pagar lo que falta (" + Ui.guaranies(plan.restante()) + ")");
        }
        btnConfirmar.setEnabled(plan.completo());
        vistaPrevia();
    }
}
