import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Ejercicio2 {

    // Punto ROTO: tiene equals pero NO hashCode
    static class PuntoRoto {
        final int x, y;

        PuntoRoto(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PuntoRoto)) return false;
            PuntoRoto p = (PuntoRoto) o;
            return x == p.x && y == p.y;
        }
        // hashCode heredado de Object → devuelve referencia del objeto → siempre diferente

        @Override
        public String toString() { return "(" + x + "," + y + ")"; }
    }

    // Punto CORRECTO: equals y hashCode consistentes
    static class PuntoCorrecto {
        final int x, y;

        PuntoCorrecto(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PuntoCorrecto)) return false;
            PuntoCorrecto p = (PuntoCorrecto) o;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() { return "(" + x + "," + y + ")"; }
    }

    public static void main(String[] args) {
        // --- Comportamiento ROTO ---
        Set<PuntoRoto> setRoto = new HashSet<>();
        setRoto.add(new PuntoRoto(1, 2));
        setRoto.add(new PuntoRoto(1, 2)); // equals dice iguales, pero hashCode diferente → duplicado!
        System.out.println("PuntoRoto  — equals sin hashCode:");
        System.out.println("  size esperado 1, real: " + setRoto.size()); // 2 — BUG

        // --- Comportamiento CORRECTO ---
        Set<PuntoCorrecto> setCorrecto = new HashSet<>();
        setCorrecto.add(new PuntoCorrecto(1, 2));
        setCorrecto.add(new PuntoCorrecto(1, 2)); // mismo hash → misma cubeta → equals → detecta duplicado
        System.out.println("\nPuntoCorrecto — equals con hashCode:");
        System.out.println("  size esperado 1, real: " + setCorrecto.size()); // 1 — OK
    }
}
