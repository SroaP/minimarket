package py.elpuente.minimarket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Arma las hojas del reporte (detalle, resumen por dia y por producto) a partir de los datos de la BD. */
final class ReporteVentas {
    private ReporteVentas() { }

    static List<Hoja> armar(Periodo periodo, LocalDate ref, List<VentaDetalle> detalle, List<VentaResumen> ventas) {
        // ---- Detalle ----
        Hoja det = new Hoja("Detalle", 9, 17, 12, 14, 36, 14, 10, 14);
        det.fila(Hoja.Estilo.ENCABEZADO, "Venta", "Fecha", "Usuario", "Código", "Producto", "Precio", "Cantidad", "Subtotal");
        BigDecimal totalDet = BigDecimal.ZERO;
        for (VentaDetalle d : detalle) {
            det.fila(Hoja.Estilo.NORMAL, d.ventaId(), d.fecha(), d.usuario(), d.codigo(), d.producto(),
                    d.precio(), d.cantidad().doubleValue(), d.subtotal());
            totalDet = totalDet.add(d.subtotal());
        }
        det.fila(Hoja.Estilo.TOTAL, "TOTAL", null, null, null, null, null, null, totalDet);

        // ---- Resumen por dia (todos los dias del periodo, aunque no haya ventas) ----
        Map<LocalDate, BigDecimal> totalDia = new TreeMap<>();
        Map<LocalDate, Long> cantDia = new TreeMap<>();
        for (LocalDate d = periodo.inicio(ref); !d.isAfter(periodo.fin(ref)); d = d.plusDays(1)) {
            totalDia.put(d, BigDecimal.ZERO);
            cantDia.put(d, 0L);
        }
        for (VentaResumen v : ventas) {
            LocalDate d = v.fecha().toLocalDate();
            totalDia.merge(d, v.total(), BigDecimal::add);
            cantDia.merge(d, 1L, Long::sum);
        }
        Hoja res = new Hoja("Resumen por día", 14, 18, 18);
        res.fila(Hoja.Estilo.ENCABEZADO, "Fecha", "Cantidad de ventas", "Total vendido");
        long n = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, BigDecimal> e : totalDia.entrySet()) {
            long c = cantDia.get(e.getKey());
            res.fila(Hoja.Estilo.NORMAL, e.getKey(), c, e.getValue());
            n += c;
            total = total.add(e.getValue());
        }
        res.fila(Hoja.Estilo.TOTAL, "TOTAL", n, total);

        // ---- Por producto ----
        record Acum(String nombre, double cantidad, BigDecimal total) { }
        Map<String, Acum> prod = new LinkedHashMap<>();
        for (VentaDetalle d : detalle) {
            prod.merge(d.codigo(), new Acum(d.producto(), d.cantidad().doubleValue(), d.subtotal()),
                    (a, b) -> new Acum(b.nombre(), a.cantidad() + b.cantidad(), a.total().add(b.total())));
        }
        Hoja pp = new Hoja("Por producto", 14, 36, 14, 16);
        pp.fila(Hoja.Estilo.ENCABEZADO, "Código", "Producto", "Cantidad", "Total vendido");
        prod.entrySet().stream()
                .sorted((a, b) -> b.getValue().total().compareTo(a.getValue().total()))
                .forEach(e -> pp.fila(Hoja.Estilo.NORMAL, e.getKey(), e.getValue().nombre(),
                        e.getValue().cantidad(), e.getValue().total()));
        pp.fila(Hoja.Estilo.TOTAL, "TOTAL", null, null, totalDet);

        return List.of(det, res, pp);
    }

    static String nombreArchivo(Periodo p, LocalDate ref, String extension) {
        return "ventas_" + p.name().toLowerCase() + "_" + p.inicio(ref)
                + (p == Periodo.DIARIO ? "" : "_a_" + p.fin(ref)) + "." + extension;
    }
}
