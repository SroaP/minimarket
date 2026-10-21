package py.elpuente.minimarket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaDetalle(long ventaId, LocalDateTime fecha, String usuario, String codigo, String producto,
                           BigDecimal precio, BigDecimal cantidad, BigDecimal subtotal) { }
