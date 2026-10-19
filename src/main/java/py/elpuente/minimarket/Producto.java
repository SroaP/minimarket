package py.elpuente.minimarket;

import java.math.BigDecimal;

/** Fila de la tabla productos(codigo, producto, precio). */
public record Producto(String codigo, String nombre, BigDecimal precio) { }
