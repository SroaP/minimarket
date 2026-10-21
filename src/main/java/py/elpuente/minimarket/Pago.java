package py.elpuente.minimarket;

import java.math.BigDecimal;

/**
 * Un pago parcial o total de una venta.
 * @param monto    parte de la venta que cubre este pago
 * @param recibido lo que entrego el cliente (en efectivo puede ser mas que monto: la diferencia es el vuelto)
 */
record Pago(MetodoPago metodo, BigDecimal monto, BigDecimal recibido) { }
