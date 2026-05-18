import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Ejercicio5 {

    // --- Versión SIN hashCode (solo equals) ---
    static class PuntoSinHash {
        int x, y;
        PuntoSinHash(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PuntoSinHash)) return false;
            PuntoSinHash p = (PuntoSinHash) o;
            return x == p.x && y == p.y;
        }
        // hashCode() NO sobreescrito -> usa identidad de objeto (dirección en memoria)
    }

    // --- Versión CON hashCode ---
    static class PuntoConHash {
        int x, y;
        PuntoConHash(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PuntoConHash)) return false;
            PuntoConHash p = (PuntoConHash) o;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public static void main(String[] args) {
        // --- Sin hashCode: el HashMap NO puede encontrar la clave ---
        System.out.println("=== PuntoSinHash (sin hashCode) ===");
        Map<PuntoSinHash, String> mapaRoto = new HashMap<>();
        PuntoSinHash p1 = new PuntoSinHash(1, 2);
        mapaRoto.put(p1, "origen");

        PuntoSinHash busqueda1 = new PuntoSinHash(1, 2);
        System.out.println("equals? " + p1.equals(busqueda1));          // true
        System.out.println("get(nuevo Punto(1,2)): " + mapaRoto.get(busqueda1)); // null (falla!)
        System.out.println("Razón: hashCode diferente -> HashMap busca en bucket equivocado");

        // --- Con hashCode: el HashMap funciona correctamente ---
        System.out.println("\n=== PuntoConHash (con hashCode) ===");
        Map<PuntoConHash, String> mapaOk = new HashMap<>();
        PuntoConHash p2 = new PuntoConHash(1, 2);
        mapaOk.put(p2, "origen");

        PuntoConHash busqueda2 = new PuntoConHash(1, 2);
        System.out.println("equals? " + p2.equals(busqueda2));          // true
        System.out.println("get(nuevo Punto(1,2)): " + mapaOk.get(busqueda2)); // "origen"
    }
}
