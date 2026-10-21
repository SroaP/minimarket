package py.elpuente.minimarket;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/** Periodo de un reporte: dia, semana (lunes a domingo) o mes calendario. */
enum Periodo {
    DIARIO("Diario"), SEMANAL("Semanal"), MENSUAL("Mensual");

    private final String etiqueta;
    Periodo(String etiqueta) { this.etiqueta = etiqueta; }
    @Override public String toString() { return etiqueta; }

    /** Primer dia incluido. */
    LocalDate inicio(LocalDate ref) {
        return switch (this) {
            case DIARIO -> ref;
            case SEMANAL -> ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MENSUAL -> ref.withDayOfMonth(1);
        };
    }

    /** Ultimo dia incluido. */
    LocalDate fin(LocalDate ref) {
        return switch (this) {
            case DIARIO -> ref;
            case SEMANAL -> inicio(ref).plusDays(6);
            case MENSUAL -> ref.with(TemporalAdjusters.lastDayOfMonth());
        };
    }

    LocalDateTime desde(LocalDate ref) { return inicio(ref).atStartOfDay(); }

    /** Limite superior exclusivo. */
    LocalDateTime hasta(LocalDate ref) { return fin(ref).plusDays(1).atStartOfDay(); }
}
