package py.elpuente.minimarket;

/** Formas de pago. El nombre del enum se guarda en la base de datos (venta_pagos.metodo). */
enum MetodoPago {
    EFECTIVO("Efectivo"), TARJETA("Tarjeta"), QR("QR");

    private final String etiqueta;
    MetodoPago(String etiqueta) { this.etiqueta = etiqueta; }
    @Override public String toString() { return etiqueta; }
}
