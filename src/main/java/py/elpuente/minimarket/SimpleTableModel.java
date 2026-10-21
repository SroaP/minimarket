package py.elpuente.minimarket;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Modelo generico de solo lectura: nombres de columna, tipos y filas. */
final class SimpleTableModel extends AbstractTableModel {
    private final String[] columnas;
    private final Class<?>[] tipos;
    private List<Object[]> filas = new ArrayList<>();

    SimpleTableModel(String[] columnas, Class<?>... tipos) {
        this.columnas = columnas;
        this.tipos = tipos;
    }

    void setFilas(List<Object[]> nuevas) {
        filas = new ArrayList<>(nuevas);
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return filas.size(); }
    @Override public int getColumnCount() { return columnas.length; }
    @Override public String getColumnName(int c) { return columnas[c]; }
    @Override public Class<?> getColumnClass(int c) { return tipos[c]; }
    @Override public Object getValueAt(int f, int c) { return filas.get(f)[c]; }
}
