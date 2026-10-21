package py.elpuente.minimarket;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Logica del cobro: pagos combinados (efectivo + tarjeta + QR), lo que falta y el vuelto. */
final class PlanPago {
    private final BigDecimal total;
    private final List<Pago> pagos = new ArrayList<>();

    PlanPago(BigDecimal total) { this.total = total; }

    BigDecimal total() { return total; }
    List<Pago> pagos() { return Collections.unmodifiableList(pagos); }

    BigDecimal pagado() {
        return pagos.stream().map(Pago::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal restante() { return total.subtract(pagado()).max(BigDecimal.ZERO); }

    boolean completo() { return restante().signum() == 0; }

    BigDecimal vuelto() { return vueltoDe(pagos); }

    static BigDecimal vueltoDe(List<Pago> lista) {
        return lista.stream().map(p -> p.recibido().subtract(p.monto())).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Vuelto que habria si ahora se recibe esta cantidad en efectivo (para mostrarlo mientras se escribe). */
    BigDecimal vueltoSiEfectivo(BigDecimal recibido) {
        return vuelto().add(recibido.subtract(restante()).max(BigDecimal.ZERO));
    }

    /**
     * Agrega un pago. Si "recibido" es null se toma lo que falta (pago exacto).
     * En efectivo se puede entregar de mas (genera vuelto); con tarjeta o QR no.
     */
    Pago agregar(MetodoPago metodo, BigDecimal recibido) {
        if (completo()) throw new IllegalStateException("La venta ya esta pagada por completo.");
        BigDecimal falta = restante();
        BigDecimal r = recibido == null ? falta : recibido;
        if (r.signum() <= 0) throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        BigDecimal aplicado;
        if (metodo == MetodoPago.EFECTIVO) {
            aplicado = r.min(falta);
        } else {
            if (r.compareTo(falta) > 0) {
                throw new IllegalArgumentException("Con " + metodo + " no se puede cobrar más de lo que falta ("
                        + Ui.guaranies(falta) + ").");
            }
            aplicado = r;
        }
        Pago p = new Pago(metodo, aplicado, r);
        pagos.add(p);
        return p;
    }

    void quitar(int indice) { pagos.remove(indice); }
}
