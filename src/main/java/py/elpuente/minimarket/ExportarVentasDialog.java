package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Consulta y exporta las ventas diarias, semanales o mensuales a Excel (.xlsx) o CSV. */
final class ExportarVentasDialog extends BaseDialog {
    private static final DateTimeFormatter DMA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VentaDao dao = new VentaDao();
    private final ButtonGroup grupo = new ButtonGroup();
    private final java.util.Map<Periodo, JToggleButton> botones = new java.util.EnumMap<>(Periodo.class);
    private final JSpinner spnFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JLabel lblRango = Ui.etiqueta(" ", 13f, Font.BOLD, Ui.PRIMARIO_OSC);
    private final JLabel lblResumen = Ui.etiqueta(" ", 14f, Font.BOLD, Ui.TEXTO);
    private final SimpleTableModel modelo = new SimpleTableModel(
            new String[]{"N° venta", "Fecha y hora", "Usuario", "Total"},
            Long.class, LocalDateTime.class, String.class, BigDecimal.class);

    ExportarVentasDialog(Window padre) {
        super(padre, "Exportar ventas", "Volver al menú");

        spnFecha.setFont(Ui.fuente(13f, Font.PLAIN));
        spnFecha.setEditor(new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy"));
        spnFecha.setPreferredSize(new Dimension(150, 34));
        JPanel segmentos = Ui.transparente(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (Periodo p : Periodo.values()) {
            JToggleButton b = new Ui.Interruptor(p.toString());
            b.setSelected(p == Periodo.DIARIO);
            grupo.add(b);
            botones.put(p, b);
            segmentos.add(b);
            b.addActionListener(e -> consultar());
        }

        JButton hoy = Ui.boton("Hoy", 11f, Ui.Estilo.NEUTRO);
        JButton xlsx = Ui.boton("Exportar a Excel (.xlsx)", 12f);
        JButton csv = Ui.boton("Exportar a CSV", 12f, Ui.Estilo.NEUTRO);

        Ui.Tarjeta filtros = Ui.tarjeta(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0; c.gridx = 0; c.insets = new Insets(0, 0, 6, 14);
        filtros.add(Ui.etiqueta("Período", 12f, Font.PLAIN, Ui.GRIS), c);
        c.gridx = 1; filtros.add(Ui.etiqueta("Fecha de referencia", 12f, Font.PLAIN, Ui.GRIS), c);
        c.gridy = 1; c.gridx = 0; c.insets = new Insets(0, 0, 0, 14); filtros.add(segmentos, c);
        c.gridx = 1; c.insets = new Insets(0, 0, 0, 8); filtros.add(spnFecha, c);
        c.gridx = 2; c.insets = new Insets(0, 0, 0, 30); filtros.add(hoy, c);
        c.gridx = 3; c.insets = new Insets(0, 0, 0, 12); filtros.add(xlsx, c);
        c.gridx = 4; c.insets = new Insets(0, 0, 0, 0); filtros.add(csv, c);
        c.gridy = 2; c.gridx = 0; c.gridwidth = 5; c.insets = new Insets(14, 0, 0, 0);
        filtros.add(lblRango, c);

        JPanel arriba = Ui.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        arriba.add(filtros);

        Ui.Tarjeta resumen = Ui.tarjeta(new BorderLayout());
        resumen.add(lblResumen, BorderLayout.CENTER);

        JPanel centro = Ui.transparente(new BorderLayout(0, 16));
        centro.add(Ui.tablaEnTarjeta(Ui.tabla(modelo)), BorderLayout.CENTER);
        centro.add(resumen, BorderLayout.SOUTH);
        contenido.add(arriba, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);

        spnFecha.addChangeListener(e -> consultar());
        hoy.addActionListener(e -> spnFecha.setValue(new Date()));
        xlsx.addActionListener(e -> exportar(true));
        csv.addActionListener(e -> exportar(false));
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { consultar(); }
        });
    }

    private Periodo periodo() {
        for (var e : botones.entrySet()) if (e.getValue().isSelected()) return e.getKey();
        return Periodo.DIARIO;
    }

    private LocalDate fecha() {
        return ((Date) spnFecha.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void consultar() {
        Periodo p = periodo();
        LocalDate ref = fecha();
        lblRango.setText(p == Periodo.DIARIO
                ? "Día: " + ref.format(DMA)
                : "Del " + p.inicio(ref).format(DMA) + " al " + p.fin(ref).format(DMA));
        try {
            List<VentaResumen> ventas = dao.ventas(p.desde(ref), p.hasta(ref));
            List<Object[]> filas = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            for (VentaResumen v : ventas) {
                filas.add(new Object[]{v.id(), v.fecha(), v.usuario(), v.total()});
                total = total.add(v.total());
            }
            modelo.setFilas(filas);
            lblResumen.setText(ventas.size() + (ventas.size() == 1 ? " venta" : " ventas")
                    + "   ·   Total vendido: " + Ui.guaranies(total));
        } catch (SQLException ex) {
            Ui.error(this, "Error al consultar las ventas:\n" + ex.getMessage());
        }
    }

    private void exportar(boolean excel) {
        Periodo p = periodo();
        LocalDate ref = fecha();
        String ext = excel ? "xlsx" : "csv";
        try {
            List<VentaResumen> ventas = dao.ventas(p.desde(ref), p.hasta(ref));
            if (ventas.isEmpty()) {
                Ui.info(this, "No hay ventas registradas en este período.");
                return;
            }
            List<VentaDetalle> detalle = dao.detalle(p.desde(ref), p.hasta(ref));

            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Guardar reporte de ventas");
            fc.setFileFilter(new FileNameExtensionFilter(excel ? "Excel (*.xlsx)" : "CSV (*.csv)", ext));
            fc.setSelectedFile(new File(ReporteVentas.nombreArchivo(p, ref, ext)));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File destino = fc.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith("." + ext)) {
                destino = new File(destino.getParentFile(), destino.getName() + "." + ext);
            }
            if (destino.exists() && !Ui.confirmar(this, "El archivo ya existe. ¿Reemplazarlo?", "Confirmar")) return;

            List<Hoja> hojas = ReporteVentas.armar(p, ref, detalle, ventas);
            if (excel) XlsxWriter.escribir(destino.toPath(), hojas);
            else CsvWriter.escribir(destino.toPath(), hojas.get(0));
            Ui.info(this, "Reporte exportado correctamente:\n" + destino.getAbsolutePath());
        } catch (SQLException | IOException ex) {
            Ui.error(this, "No se pudo exportar:\n" + ex.getMessage());
        }
    }
}
