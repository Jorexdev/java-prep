import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Comparator chain con tiebreakers y sort estable demostrado

public class Ejercicio5 {

    record Producto(String nombre, String categoria, double precio, int stock) {
        @Override
        public String toString() {
            return String.format("%-18s cat=%-12s precio=%6.2f stock=%3d",
                                 nombre, categoria, precio, stock);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Comparator chain con tiebreakers y sort estable ===\n");

        // Comparator: 1º categoría asc, 2º precio desc, 3º nombre asc (tiebreaker)
        Comparator<Producto> comparator = Comparator
            .comparing(Producto::categoria)
            .thenComparingDouble((Producto p) -> -p.precio())   // precio desc → negamos
            .thenComparing(Producto::nombre);

        // 12 productos con colisiones deliberadas en varios niveles del chain
        List<Producto> productos = new ArrayList<>(List.of(
            new Producto("Teclado",     "Perifericos",  89.99, 10),
            new Producto("Raton",       "Perifericos",  45.00, 25),
            new Producto("Monitor",     "Pantallas",   299.00,  5),
            new Producto("Monitor Pro", "Pantallas",   299.00,  3),  // mismo precio → tiebreaker nombre
            new Producto("Auriculares", "Audio",        59.90, 12),
            new Producto("Altavoces",   "Audio",        59.90,  8),  // mismo precio → tiebreaker nombre
            new Producto("Cable HDMI",  "Perifericos",  12.50, 50),
            new Producto("Hub USB",     "Perifericos",  29.99, 18),
            new Producto("Webcam",      "Perifericos",  45.00,  7),  // mismo precio que Raton
            new Producto("Microfono",   "Audio",        89.99, 15),
            new Producto("Subwoofer",   "Audio",       150.00,  4),
            new Producto("SSD 1TB",     "Almacenamiento", 79.00, 20)
        ));

        // ── Estabilidad: dos productos idénticos en los 3 criterios ──────────
        // "Raton" y "Webcam" tienen misma categoría (Perifericos) y mismo precio (45.00)
        // pero nombre distinto → resuelto por tiebreaker.
        // Para demostrar estabilidad real, añadimos dos ítems con todo igual menos stock:
        Producto duplicadoA = new Producto("Widget", "Accesorios", 19.99, 100); // posición 12
        Producto duplicadoB = new Producto("Widget", "Accesorios", 19.99,  50); // posición 13
        productos.add(duplicadoA);
        productos.add(duplicadoB);

        System.out.println("Lista original (14 productos, orden de inserción):");
        for (int i = 0; i < productos.size(); i++) {
            System.out.printf("  [%2d] %s%n", i, productos.get(i));
        }

        // ── Collections.sort (merge sort → estable) ─────────────────────────
        List<Producto> porCollections = new ArrayList<>(productos);
        Collections.sort(porCollections, comparator);

        System.out.println("\nOrdenado con Collections.sort (estable):");
        for (Producto p : porCollections) {
            System.out.println("  " + p);
        }

        // ── List.sort (también estable en Java) ──────────────────────────────
        List<Producto> porListSort = new ArrayList<>(productos);
        porListSort.sort(comparator);

        System.out.println("\nOrdenado con List.sort (también estable):");
        for (Producto p : porListSort) {
            System.out.println("  " + p);
        }

        // ── Verificar estabilidad de los duplicados ──────────────────────────
        int posA = porCollections.indexOf(duplicadoA);
        int posB = porCollections.indexOf(duplicadoB);
        System.out.println("\n=== Verificacion de estabilidad ===");
        System.out.printf("  duplicadoA (stock=100) en posición %d%n", posA);
        System.out.printf("  duplicadoB (stock=50)  en posición %d%n", posB);
        System.out.println("  Orden relativo preservado: " + (posA < posB ? "SI ✓" : "NO ✗"));

        System.out.println("\n=== Criterios determinantes ===");
        String catAnterior = "";
        double precioAnterior = Double.NaN;
        for (Producto p : porCollections) {
            String criterio;
            if (!p.categoria().equals(catAnterior)) {
                criterio = "CATEGORIA";
                catAnterior = p.categoria();
                precioAnterior = p.precio();
            } else if (Double.compare(p.precio(), precioAnterior) != 0) {
                criterio = "PRECIO";
                precioAnterior = p.precio();
            } else {
                criterio = "NOMBRE (tiebreaker)";
            }
            System.out.printf("  %-18s → ordenado por %s%n", p.nombre(), criterio);
        }

        System.out.println("\nNota: Java garantiza sort estable desde Java 1.2 (TimSort).");
        System.out.println("Comparator.thenComparing() evalúa criterios en orden de izquierda a derecha.");
        System.out.println("El último thenComparing es el tiebreaker de desempate definitivo.");
    }
}
