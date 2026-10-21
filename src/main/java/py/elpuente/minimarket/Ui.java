package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Tema visual y utilidades de interfaz compartidas por todas las pantallas. */
final class Ui {
    // ---- paleta ----
    static final Color PRIMARIO = new Color(15, 118, 110);
    static final Color PRIMARIO_OSC = new Color(19, 78, 74);
    static final Color PRIMARIO_CLARO = new Color(204, 240, 235);
    static final Color FONDO = new Color(241, 245, 249);
    static final Color BORDE = new Color(215, 222, 232);
    static final Color TEXTO = new Color(15, 23, 42);
    static final Color GRIS = new Color(100, 116, 139);
    static final Color PELIGRO = new Color(220, 38, 38);
    static final Color FILA_ALT = new Color(246, 249, 252);

    static final String TITULO = "Minimarket El Puente";
    private static final Locale PY = Locale.forLanguageTag("es-PY");
    private static final NumberFormat ENTERO = NumberFormat.getIntegerInstance(PY);
    private static final DecimalFormat CANTIDAD = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(PY));
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    static { ENTERO.setRoundingMode(RoundingMode.HALF_UP); }

    private Ui() { }

    enum Estilo { PRIMARIO, PELIGRO, NEUTRO }

    // ---- fuentes y componentes basicos ----

    static Font fuente(float puntos, int estilo) {
        return new Font(Font.SANS_SERIF, estilo, Math.round(puntos * 1.15f));
    }

    static JLabel etiqueta(String texto, float puntos) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(puntos, Font.PLAIN));
        l.setForeground(TEXTO);
        return l;
    }

    static JLabel etiqueta(String texto, float puntos, int estilo, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(puntos, estilo));
        l.setForeground(color);
        return l;
    }

    static JPanel transparente(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    static JTextField campo(int columnas, float puntos) {
        JTextField t = new JTextField(columnas);
        estilizarCampo(t, puntos);
        return t;
    }

    static JPasswordField campoClave(int columnas, float puntos) {
        JPasswordField t = new JPasswordField(columnas);
        estilizarCampo(t, puntos);
        return t;
    }

    private static void estilizarCampo(JTextField t, float puntos) {
        t.setFont(fuente(puntos, Font.PLAIN));
        t.setForeground(TEXTO);
        t.setBackground(Color.WHITE);
        t.setCaretColor(PRIMARIO);
        Runnable normal = () -> t.setBorder(bordeCampo(BORDE));
        Runnable foco = () -> t.setBorder(bordeCampo(PRIMARIO));
        normal.run();
        t.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { foco.run(); }
            @Override public void focusLost(FocusEvent e) { normal.run(); }
        });
    }

    private static javax.swing.border.Border bordeCampo(Color c) {
        return BorderFactory.createCompoundBorder(new LineBorder(c, 1, true), new EmptyBorder(7, 10, 7, 10));
    }

    static JLabel logo(double escala) {
        URL url = Ui.class.getResource("/img/logo.png");
        if (url == null) return new JLabel();
        ImageIcon ic = new ImageIcon(url);
        Image img = ic.getImage().getScaledInstance(
                (int) (ic.getIconWidth() * escala), (int) (ic.getIconHeight() * escala), Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(img));
    }

    // ---- botones ----

    static JButton boton(String texto, float puntos) { return boton(texto, puntos, Estilo.PRIMARIO); }

    static JButton boton(String texto, float puntos, Estilo estilo) {
        Boton b = new Boton(texto, estilo);
        b.setFont(fuente(puntos, Font.BOLD));
        return b;
    }

    /** Boton plano con esquinas redondeadas que se dibuja a si mismo (no depende del Look&Feel). */
    private static final class Boton extends JButton {
        private final Estilo estilo;

        Boton(String texto, Estilo estilo) {
            super(texto);
            this.estilo = estilo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(11, 22, 11, 22));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean sobre = getModel().isRollover(), presionado = getModel().isPressed();
            Color fondo, texto, borde = null;
            switch (estilo) {
                case PELIGRO -> { fondo = presionado ? new Color(153, 27, 27) : sobre ? new Color(185, 28, 28) : PELIGRO; texto = Color.WHITE; }
                case NEUTRO -> { fondo = presionado ? new Color(226, 232, 240) : sobre ? new Color(241, 245, 249) : Color.WHITE; texto = TEXTO; borde = BORDE; }
                default -> { fondo = presionado ? PRIMARIO_OSC : sobre ? new Color(17, 94, 89) : PRIMARIO; texto = Color.WHITE; }
            }
            if (!isEnabled()) { fondo = new Color(203, 213, 225); texto = new Color(100, 116, 139); }
            g.setColor(fondo);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            if (borde != null && isEnabled()) {
                g.setColor(borde);
                g.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            }
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.setColor(texto);
            g.drawString(getText(), x, y);
            if (hasFocus() && isFocusPainted()) { /* sin anillo de foco: el hover y el color bastan */ }
            g.dispose();
        }
    }

    /** Boton tipo interruptor (para elegir una opcion entre varias). */
    static final class Interruptor extends JToggleButton {
        Interruptor(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(fuente(12f, Font.BOLD));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 22, 10, 22));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean sel = isSelected(), sobre = getModel().isRollover();
            RoundRectangle2D forma = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.setColor(sel ? PRIMARIO : sobre ? new Color(241, 245, 249) : Color.WHITE);
            g.fill(forma);
            if (!sel) { g.setColor(BORDE); g.draw(forma); }
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.setColor(sel ? Color.WHITE : TEXTO);
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Mosaico del menu principal: icono con inicial, titulo y descripcion. */
    static final class Mosaico extends JButton {
        private final String titulo, detalle, icono;
        private final Color acento;

        Mosaico(String icono, String titulo, String detalle, Color acento) {
            this.icono = icono;
            this.titulo = titulo;
            this.detalle = detalle;
            this.acento = acento;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(330, 130));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean sobre = getModel().isRollover(), presionado = getModel().isPressed();
            int w = getWidth() - 1, h = getHeight() - 1;
            if (sobre) { g.setColor(new Color(0, 0, 0, 18)); g.fill(new RoundRectangle2D.Float(2, 5, w - 2, h - 2, 20, 20)); }
            g.setColor(presionado ? new Color(241, 245, 249) : Color.WHITE);
            g.fill(new RoundRectangle2D.Float(0, 0, w, h - 3, 20, 20));
            g.setColor(sobre ? acento : BORDE);
            g.setStroke(new BasicStroke(sobre ? 2f : 1f));
            g.draw(new RoundRectangle2D.Float(0, 0, w, h - 3, 20, 20));

            int d = 56, ix = 22, iy = (h - 3 - d) / 2;
            g.setColor(new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 28));
            g.fillOval(ix, iy, d, d);
            g.setColor(acento);
            g.setFont(fuente(24f, Font.BOLD));
            FontMetrics fi = g.getFontMetrics();
            String letra = icono;
            g.drawString(letra, ix + (d - fi.stringWidth(letra)) / 2, iy + (d - fi.getHeight()) / 2 + fi.getAscent());

            int tx = ix + d + 18;
            g.setColor(TEXTO);
            g.setFont(fuente(15f, Font.BOLD));
            FontMetrics ft = g.getFontMetrics();
            g.drawString(titulo, tx, (h - 3) / 2 - 2);
            g.setColor(GRIS);
            g.setFont(fuente(11f, Font.PLAIN));
            FontMetrics fd = g.getFontMetrics();
            g.drawString(recortar(detalle, fd, getWidth() - tx - 14), tx, (h - 3) / 2 + fd.getHeight() + 2);
            g.dispose();
        }

        private static String recortar(String s, FontMetrics fm, int ancho) {
            if (fm.stringWidth(s) <= ancho) return s;
            while (s.length() > 1 && fm.stringWidth(s + "…") > ancho) s = s.substring(0, s.length() - 1);
            return s + "…";
        }
    }

    /** Panel blanco con esquinas redondeadas y borde suave. */
    static final class Tarjeta extends JPanel {
        Tarjeta(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(18, 20, 18, 20));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
            g.setColor(BORDE);
            g.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static Tarjeta tarjeta(LayoutManager layout) { return new Tarjeta(layout); }

    // ---- tablas ----

    static JTable tabla(TableModel modelo) {
        JTable t = new JTable(modelo) {
            @Override public Component prepareRenderer(TableCellRenderer r, int fila, int col) {
                Component c = super.prepareRenderer(r, fila, col);
                c.setBackground(isRowSelected(fila) ? PRIMARIO_CLARO : (fila % 2 == 0 ? Color.WHITE : FILA_ALT));
                c.setForeground(TEXTO);
                return c;
            }
        };
        t.setFont(fuente(12f, Font.PLAIN));
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        t.setSelectionBackground(PRIMARIO_CLARO);
        t.setSelectionForeground(TEXTO);

        t.setDefaultRenderer(Object.class, new Celda(false, v -> String.valueOf(v)));
        t.setDefaultRenderer(String.class, new Celda(false, v -> String.valueOf(v)));
        t.setDefaultRenderer(BigDecimal.class, new Celda(true, v -> formatoEntero((BigDecimal) v)));
        t.setDefaultRenderer(Double.class, new Celda(true, v -> { synchronized (CANTIDAD) { return CANTIDAD.format(v); } }));
        t.setDefaultRenderer(Long.class, new Celda(true, String::valueOf));
        t.setDefaultRenderer(Integer.class, new Celda(true, String::valueOf));
        t.setDefaultRenderer(LocalDateTime.class, new Celda(false, v -> ((LocalDateTime) v).format(FECHA_HORA)));

        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setDefaultRenderer((tbl, valor, sel, foco, f, c) -> {
            JLabel l = new JLabel(String.valueOf(valor));
            l.setOpaque(true);
            l.setBackground(PRIMARIO);
            l.setForeground(Color.WHITE);
            l.setFont(fuente(12f, Font.BOLD));
            Class<?> k = tbl.getModel().getColumnClass(c);
            boolean derecha = k == BigDecimal.class || k == Double.class || k == Long.class || k == Integer.class;
            l.setHorizontalAlignment(derecha ? SwingConstants.RIGHT : SwingConstants.LEFT);
            l.setBorder(new EmptyBorder(0, 12, 0, 12));
            l.setPreferredSize(new Dimension(l.getPreferredSize().width, 40));
            return l;
        });
        for (int i = 0; i < t.getColumnCount(); i++) {
            String n = String.valueOf(t.getColumnName(i)).toLowerCase();
            t.getColumnModel().getColumn(i).setPreferredWidth(n.equals("producto") ? 420 : n.startsWith("fecha") ? 190 : 130);
        }
        return t;
    }

    /** Tabla dentro de una tarjeta con scroll. */
    static JComponent tablaEnTarjeta(JTable tabla) {
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(new LineBorder(BORDE));
        sp.getViewport().setBackground(Color.WHITE);
        Tarjeta c = tarjeta(new BorderLayout());
        c.setBorder(new EmptyBorder(14, 14, 14, 14));
        c.add(sp, BorderLayout.CENTER);
        return c;
    }

    private static final class Celda extends DefaultTableCellRenderer {
        private final java.util.function.Function<Object, String> formato;

        Celda(boolean derecha, java.util.function.Function<Object, String> formato) {
            this.formato = formato;
            setHorizontalAlignment(derecha ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 12, 0, 12));
        }

        @Override protected void setValue(Object v) { setText(v == null ? "" : formato.apply(v)); }
    }

    // ---- campos ----

    /** Ejecuta la accion cada vez que cambia el texto (equivale a TextChanged). */
    static void alCambiar(JTextComponent c, Runnable accion) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { accion.run(); }
            public void removeUpdate(DocumentEvent e) { accion.run(); }
            public void changedUpdate(DocumentEvent e) { accion.run(); }
        });
    }

    /** Solo permite escribir (o pegar) digitos, hasta un maximo de caracteres. */
    static void soloDigitos(JTextComponent c, int max) {
        ((AbstractDocument) c.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void insertString(FilterBypass fb, int off, String s, AttributeSet a) throws BadLocationException {
                replace(fb, off, 0, s, a);
            }

            @Override public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a) throws BadLocationException {
                if (s == null) { super.replace(fb, off, len, s, a); return; }
                String d = s.replaceAll("\\D", "");
                int libre = max - (fb.getDocument().getLength() - len);
                if (libre <= 0) return;
                if (d.length() > libre) d = d.substring(0, libre);
                super.replace(fb, off, len, d, a);
            }
        });
    }

    // ---- mensajes (equivalen a MsgBox) ----

    static void info(Component padre, String msg) {
        JOptionPane.showMessageDialog(padre, msg, TITULO, JOptionPane.INFORMATION_MESSAGE);
    }

    static void aviso(Component padre, String msg) {
        JOptionPane.showMessageDialog(padre, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    static void error(Component padre, String msg) {
        JOptionPane.showMessageDialog(padre, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static boolean confirmar(Component padre, String msg, String titulo) {
        return JOptionPane.showConfirmDialog(padre, msg, titulo, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ---- numeros ----

    /** Acepta "1500", "1500.50" o "1500,50". Devuelve null si no es un numero valido. */
    static BigDecimal parseDecimal(String s) {
        if (s == null) return null;
        String t = s.trim().replace(',', '.');
        if (t.isEmpty()) return null;
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static boolean esEntero(String s) {
        try {
            Integer.parseInt(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Equivale a ToString("N0"): sin decimales y con separador de miles (guaranies). */
    static String formatoEntero(BigDecimal v) {
        synchronized (ENTERO) {
            return ENTERO.format(v);
        }
    }

    static String guaranies(BigDecimal v) { return "Gs. " + formatoEntero(v); }
}
