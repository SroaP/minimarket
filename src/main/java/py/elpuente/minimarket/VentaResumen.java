package py.elpuente.minimarket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResumen(long id, LocalDateTime fecha, String usuario, BigDecimal total) { }
