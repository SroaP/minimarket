package py.elpuente.minimarket;

import java.math.BigDecimal;

/** Una fila del carrito de venta. */
public final class LineaVenta {
    private final String codigo;
    private final String nombre;
    private final BigDecimal precio;
    private double cantidad;

    public LineaVenta(String codigo, String nombre, BigDecimal precio, double cantidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public String codigo() { return codigo; }
    public String nombre() { return nombre; }
    public BigDecimal precio() { return precio; }
    public double cantidad() { return cantidad; }
    public void sumar(double extra) { cantidad += extra; }

    public BigDecimal subtotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}
