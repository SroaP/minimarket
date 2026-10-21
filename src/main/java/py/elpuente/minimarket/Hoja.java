package py.elpuente.minimarket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Una hoja de reporte: encabezado, filas y anchos de columna (en caracteres). */
final class Hoja {
    enum Estilo { NORMAL, ENCABEZADO, TOTAL }

    record Fila(Estilo estilo, List<Object> celdas) { }

    final String nombre;
    final double[] anchos;
    final List<Fila> filas = new ArrayList<>();

    Hoja(String nombre, double... anchos) {
        this.nombre = nombre;
        this.anchos = anchos;
    }

    Hoja fila(Estilo e, Object... celdas) {
        filas.add(new Fila(e, Arrays.asList(celdas)));
        return this;
    }
}
