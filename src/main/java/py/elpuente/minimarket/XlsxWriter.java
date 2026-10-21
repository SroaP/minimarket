package py.elpuente.minimarket;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Escritor minimo de .xlsx (Office Open XML) sin dependencias externas. */
final class XlsxWriter {
    // indices de cellXfs en styles.xml
    private static final int GENERAL = 0, NEGRITA = 1, FECHA_HORA = 2, DINERO = 3, DINERO_NEGRITA = 4, FECHA = 5,
            CANTIDAD = 6, ENCABEZADO = 7;

    private XlsxWriter() { }

    static void escribir(Path destino, List<Hoja> hojas) throws IOException {
        try (OutputStream os = Files.newOutputStream(destino); ZipOutputStream z = new ZipOutputStream(os)) {
            entrada(z, "[Content_Types].xml", contentTypes(hojas.size()));
            entrada(z, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            entrada(z, "xl/workbook.xml", workbook(hojas));
            entrada(z, "xl/_rels/workbook.xml.rels", workbookRels(hojas.size()));
            entrada(z, "xl/styles.xml", STYLES);
            for (int i = 0; i < hojas.size(); i++) {
                entrada(z, "xl/worksheets/sheet" + (i + 1) + ".xml", hoja(hojas.get(i)));
            }
        }
    }

    private static void entrada(ZipOutputStream z, String nombre, String contenido) throws IOException {
        z.putNextEntry(new ZipEntry(nombre));
        z.write(contenido.getBytes(StandardCharsets.UTF_8));
        z.closeEntry();
    }

    private static String contentTypes(int n) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 1; i <= n; i++) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
              .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        return sb.append("</Types>").toString();
    }

    private static String workbook(List<Hoja> hojas) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>");
        for (int i = 0; i < hojas.size(); i++) {
            sb.append("<sheet name=\"").append(esc(hojas.get(i).nombre)).append("\" sheetId=\"").append(i + 1)
              .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        return sb.append("</sheets></workbook>").toString();
    }

    private static String workbookRels(int n) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= n; i++) {
            sb.append("<Relationship Id=\"rId").append(i)
              .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
              .append(i).append(".xml\"/>");
        }
        sb.append("<Relationship Id=\"rId").append(n + 1)
          .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        return sb.append("</Relationships>").toString();
    }

    private static String hoja(Hoja h) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>"
                + "<cols>");
        for (int i = 0; i < h.anchos.length; i++) {
            sb.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1).append("\" width=\"")
              .append(h.anchos[i]).append("\" customWidth=\"1\"/>");
        }
        sb.append("</cols><sheetData>");
        int r = 1;
        for (Hoja.Fila f : h.filas) {
            sb.append("<row r=\"").append(r).append("\">");
            for (int c = 0; c < f.celdas().size(); c++) {
                celda(sb, columna(c) + r, f.celdas().get(c), f.estilo());
            }
            sb.append("</row>");
            r++;
        }
        return sb.append("</sheetData></worksheet>").toString();
    }

    private static void celda(StringBuilder sb, String ref, Object v, Hoja.Estilo estilo) {
        if (v == null) return;
        int s;
        String valor = null;
        String texto = null;
        if (v instanceof BigDecimal b) {
            valor = b.toPlainString();
            s = estilo == Hoja.Estilo.TOTAL ? DINERO_NEGRITA : DINERO;
        } else if (v instanceof Double d) {
            valor = BigDecimal.valueOf(d).toPlainString();
            s = CANTIDAD;
        } else if (v instanceof Number n) {
            valor = n.toString();
            s = GENERAL;
        } else if (v instanceof LocalDateTime dt) {
            valor = BigDecimal.valueOf(Duration.between(LocalDateTime.of(1899, 12, 30, 0, 0), dt).getSeconds())
                    .divide(BigDecimal.valueOf(86400), 8, java.math.RoundingMode.HALF_UP).toPlainString();
            s = FECHA_HORA;
        } else if (v instanceof LocalDate d) {
            valor = String.valueOf(java.time.temporal.ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), d));
            s = FECHA;
        } else {
            texto = v.toString();
            s = estilo == Hoja.Estilo.NORMAL ? GENERAL : NEGRITA;
        }
        if (estilo == Hoja.Estilo.ENCABEZADO) s = ENCABEZADO;
        else if (estilo == Hoja.Estilo.TOTAL && (s == GENERAL || s == CANTIDAD)) s = NEGRITA;

        sb.append("<c r=\"").append(ref).append("\" s=\"").append(s).append('"');
        if (texto != null) {
            sb.append(" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(esc(texto)).append("</t></is></c>");
        } else {
            sb.append("><v>").append(valor).append("</v></c>");
        }
    }

    private static String columna(int i) {
        StringBuilder sb = new StringBuilder();
        for (i++; i > 0; i = (i - 1) / 26) sb.insert(0, (char) ('A' + (i - 1) % 26));
        return sb.toString();
    }

    private static String esc(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                default -> { if (c >= 0x20 || c == '\t' || c == '\n' || c == '\r') sb.append(c); }
            }
        }
        return sb.toString();
    }

    private static final String STYLES = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        + "<numFmts count=\"3\"><numFmt numFmtId=\"164\" formatCode=\"dd/mm/yyyy\\ hh:mm\"/>"
        + "<numFmt numFmtId=\"165\" formatCode=\"#,##0.###\"/><numFmt numFmtId=\"166\" formatCode=\"dd/mm/yyyy\"/></numFmts>"
        + "<fonts count=\"3\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
        + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
        + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font></fonts>"
        + "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill>"
        + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF0F766E\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"
        + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
        + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
        + "<cellXfs count=\"8\">"
        + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
        + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
        + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
        + "<xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
        + "<xf numFmtId=\"3\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\"/>"
        + "<xf numFmtId=\"166\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
        + "<xf numFmtId=\"165\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
        + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
        + "</cellXfs>"
        + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
        + "</styleSheet>";
}
