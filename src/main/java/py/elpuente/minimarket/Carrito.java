package py.elpuente.minimarket;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Logica de la venta (sin interfaz): agregar, quitar, vaciar y total. */
public final class Carrito {
    private final List<LineaVenta> lineas = new ArrayList<>();

    /** Si el codigo ya esta en el carrito suma la cantidad; si no, agrega una linea nueva. */
    public void agregar(String codigo, String nombre, BigDecimal precio, double cantidad) {
        for (LineaVenta l : lineas) {
            if (l.codigo().equals(codigo)) {
                l.sumar(cantidad);
                return;
            }
        }
        lineas.add(new LineaVenta(codigo, nombre, precio, cantidad));
    }

    public void quitar(int indice) { lineas.remove(indice); }

    public void vaciar() { lineas.clear(); }

    public BigDecimal total() {
        return lineas.stream().map(LineaVenta::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<LineaVenta> lineas() { return Collections.unmodifiableList(lineas); }
}
