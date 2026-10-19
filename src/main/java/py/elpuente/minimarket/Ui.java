package py.elpuente.minimarket;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

/** Utilidades de interfaz compartidas por todas las pantallas. */
final class Ui {
    static final Color TEAL = new Color(0, 128, 128);
    static final String TITULO = "Minimarket El Puente";

    private static final NumberFormat ENTERO = NumberFormat.getIntegerInstance(new Locale("es", "PY"));
    static { ENTERO.setRoundingMode(RoundingMode.HALF_UP); }

    private Ui() { }

    static Font fuente(float puntos, int estilo) {
        return new Font(Font.SANS_SERIF, estilo, Math.round(puntos * 1.15f));
    }

    static JLabel etiqueta(String texto, float puntos) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(puntos, Font.PLAIN));
        return l;
    }

    static JButton boton(String texto, float puntos) {
        JButton b = new JButton(texto);
        b.setFont(fuente(puntos, Font.PLAIN));
        b.setFocusPainted(false);
        return b;
    }

    static JTextField campo(int columnas, float puntos) {
        JTextField t = new JTextField(columnas);
        t.setFont(fuente(puntos, Font.PLAIN));
        return t;
    }

    static JPanel transparente(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    static JLabel logo(double escala) {
        URL url = Ui.class.getResource("/img/logo.png");
        if (url == null) return new JLabel();
        ImageIcon ic = new ImageIcon(url);
        Image img = ic.getImage().getScaledInstance(
                (int) (ic.getIconWidth() * escala), (int) (ic.getIconHeight() * escala), Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(img));
    }

    static JTable tabla(TableModel modelo) {
        JTable t = new JTable(modelo);
        t.setFont(fuente(12f, Font.PLAIN));
        t.getTableHeader().setFont(fuente(12f, Font.BOLD));
        t.setRowHeight(30);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        return t;
    }

    /** Ejecuta la accion cada vez que cambia el texto (equivale a TextChanged). */
    static void alCambiar(JTextComponent c, Runnable accion) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { accion.run(); }
            public void removeUpdate(DocumentEvent e) { accion.run(); }
            public void changedUpdate(DocumentEvent e) { accion.run(); }
        });
    }

    // ---- mensajes (equivalen a MsgBox) ----------------------------------------------------

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

    // ---- numeros -------------------------------------------------------------------------

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
}
