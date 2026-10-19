package py.elpuente.minimarket;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Modelo de tabla para las grillas codigo / producto / precio. */
final class ProductoTableModel extends AbstractTableModel {
    private static final String[] COLUMNAS = {"codigo", "producto", "precio"};
    private List<Producto> filas = new ArrayList<>();

    void setFilas(List<Producto> nuevas) {
        filas = new ArrayList<>(nuevas);
        fireTableDataChanged();
    }

    Producto get(int fila) { return filas.get(fila); }

    @Override public int getRowCount() { return filas.size(); }
    @Override public int getColumnCount() { return COLUMNAS.length; }
    @Override public String getColumnName(int c) { return COLUMNAS[c]; }

    @Override public Object getValueAt(int f, int c) {
        Producto p = filas.get(f);
        return switch (c) {
            case 0 -> p.codigo();
            case 1 -> p.nombre();
            default -> p.precio();
        };
    }
}
