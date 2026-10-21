package py.elpuente.minimarket;

import javax.swing.table.AbstractTableModel;

/** Muestra el Carrito en una JTable: codigo, producto, precio, cantidad, subtotal. */
final class VentaTableModel extends AbstractTableModel {
    private static final String[] COLUMNAS = {"codigo", "producto", "precio", "cantidad", "subtotal"};
    private final Carrito carrito;

    VentaTableModel(Carrito carrito) { this.carrito = carrito; }

    void refrescar() { fireTableDataChanged(); }

    @Override public int getRowCount() { return carrito.lineas().size(); }
    @Override public int getColumnCount() { return COLUMNAS.length; }
    @Override public String getColumnName(int c) { return COLUMNAS[c]; }
    @Override public Class<?> getColumnClass(int c) {
        return switch (c) { case 2, 4 -> java.math.BigDecimal.class; case 3 -> Double.class; default -> String.class; };
    }

    @Override public Object getValueAt(int f, int c) {
        LineaVenta l = carrito.lineas().get(f);
        return switch (c) {
            case 0 -> l.codigo();
            case 1 -> l.nombre();
            case 2 -> l.precio();
            case 3 -> l.cantidad();
            default -> l.subtotal();
        };
    }
}
