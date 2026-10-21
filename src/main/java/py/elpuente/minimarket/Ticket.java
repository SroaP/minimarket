package py.elpuente.minimarket;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Copia temporal de la ultima venta cobrada. Solo vive en memoria: se sobreescribe con la
 * siguiente venta cobrada y se descarta al empezar una venta nueva o al cerrar la pantalla de venta.
 */
final class Ticket {
    // Datos del local: cambielos aqui
    static final String LOCAL = "MINIMARKET EL PUENTE";
    static final String DIRECCION = "Dirección: (completar)";

    private static final int ANCHO = 40;
    private static final Locale PY = Locale.forLanguageTag("es-PY");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    record Item(String nombre, double cantidad, BigDecimal subtotal) { }

    private final long numero;
    private final LocalDateTime fecha;
    private final String usuario;
    private final List<Item> items;
    private final BigDecimal total;
    private final List<Pago> pagos;

    private Ticket(long numero, LocalDateTime fecha, String usuario, List<Item> items, BigDecimal total, List<Pago> pagos) {
        this.numero = numero;
        this.fecha = fecha;
        this.usuario = usuario;
        this.items = items;
        this.total = total;
        this.pagos = pagos;
    }

    /** Toma una copia de las lineas (el carrito se vacia despues de cobrar). */
    static Ticket crear(long numero, LocalDateTime fecha, String usuario, List<LineaVenta> lineas, BigDecimal total,
                        List<Pago> pagos) {
        List<Item> copia = new ArrayList<>();
        for (LineaVenta l : lineas) copia.add(new Item(l.nombre(), l.cantidad(), l.subtotal()));
        return new Ticket(numero, fecha, usuario, copia, total, new ArrayList<>(pagos));
    }

    long numero() { return numero; }

    /** Texto del ticket en columnas de ancho fijo (para fuente monoespaciada). */
    String texto() {
        DecimalFormat cant = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(PY));
        String raya = "-".repeat(ANCHO);
        StringBuilder sb = new StringBuilder();
        sb.append(centrar(LOCAL)).append('\n');
        sb.append(centrar(DIRECCION)).append('\n');
        sb.append(raya).append('\n');
        sb.append("Fecha: ").append(fecha.format(FECHA)).append('\n');
        sb.append("Ticket N° ").append(numero).append('\n');
        sb.append("Cajero: ").append(usuario).append('\n');
        sb.append(raya).append('\n');
        sb.append(String.format("%-6s%-23s%11s%n", "CANT", "DESCRIPCIÓN", "SUBTOTAL"));
        for (Item i : items) {
            String desc = i.nombre().length() > 22 ? i.nombre().substring(0, 22) : i.nombre();
            sb.append(String.format("%-6s%-23s%11s%n", cant.format(i.cantidad()), desc, Ui.formatoEntero(i.subtotal())));
        }
        sb.append(raya).append('\n');
        String totalTxt = Ui.guaranies(total);
        sb.append(String.format("%-" + (ANCHO - totalTxt.length()) + "s%s%n", "TOTAL", totalTxt));
        for (Pago p : pagos) {
            String monto = Ui.guaranies(p.recibido());
            sb.append(String.format("%-" + (ANCHO - monto.length()) + "s%s%n", p.metodo(), monto));
        }
        BigDecimal vuelto = PlanPago.vueltoDe(pagos);
        if (vuelto.signum() > 0) {
            String v = Ui.guaranies(vuelto);
            sb.append(String.format("%-" + (ANCHO - v.length()) + "s%s%n", "VUELTO", v));
        }
        sb.append(raya).append('\n');
        sb.append(centrar("¡Gracias por su compra!")).append('\n');
        return sb.toString();
    }

    private static String centrar(String s) {
        int margen = Math.max(0, (ANCHO - s.length()) / 2);
        return " ".repeat(margen) + s;
    }
}
