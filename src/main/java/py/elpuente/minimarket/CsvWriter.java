package py.elpuente.minimarket;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** CSV para Excel en español: UTF-8 con BOM, separador ';' y coma decimal. */
final class CsvWriter {
    private static final DateTimeFormatter FH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private CsvWriter() { }

    static void escribir(Path destino, Hoja hoja) throws IOException {
        try (Writer w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            for (Hoja.Fila f : hoja.filas) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < f.celdas().size(); i++) {
                    if (i > 0) sb.append(';');
                    sb.append(texto(f.celdas().get(i)));
                }
                w.write(sb.append("\r\n").toString());
            }
        }
    }

    private static String texto(Object v) {
        if (v == null) return "";
        if (v instanceof BigDecimal b) return b.toPlainString().replace('.', ',');
        if (v instanceof Double d) return BigDecimal.valueOf(d).toPlainString().replace('.', ',');
        if (v instanceof LocalDateTime dt) return dt.format(FH);
        if (v instanceof LocalDate d) return d.format(F);
        String s = v.toString();
        if (s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
