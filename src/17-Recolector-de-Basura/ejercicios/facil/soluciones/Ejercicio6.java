import java.util.ArrayList;
import java.util.List;

public class Ejercicio6 {

    static class Leak {
        // Lista estática: nunca es elegible para GC mientras la clase esté cargada
        static final List<byte[]> leakedData = new ArrayList<>();

        static void leak() {
            // Añadir 10KB sin eliminarlo nunca
            leakedData.add(new byte[10 * 1024]);
        }

        static int size() {
            return leakedData.size();
        }
    }

    static String humanReadable(long bytes) {
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    public static void main(String[] args) {
        System.out.println("=== Memory Leak Simulation ===");
        System.out.println("Añadiendo 10KB por llamada, 20 llamadas en total.");
        System.out.println();
        System.out.printf("%-10s %-15s %-15s %-15s%n",
            "Llamada", "Lista size", "Usado (MB)", "Libre (MB)");
        System.out.println("-".repeat(55));

        Runtime rt = Runtime.getRuntime();

        for (int i = 1; i <= 20; i++) {
            Leak.leak();

            long total = rt.totalMemory();
            long free = rt.freeMemory();
            long used = total - free;

            System.out.printf("%-10d %-15d %-15s %-15s%n",
                i,
                Leak.size(),
                humanReadable(used),
                humanReadable(free));
        }

        System.out.println();
        System.out.println("=== Análisis ===");
        System.out.println("La lista estática retiene TODAS las referencias.");
        System.out.println("El GC no puede recolectar nada porque todo sigue siendo alcanzable.");
        System.out.println("Tamaño final de la lista: " + Leak.size() + " arrays");
        long leaked = (long) Leak.size() * 10 * 1024;
        System.out.println("Memoria retenida: " + humanReadable(leaked));
        System.out.println();
        System.out.println("Solución: usar WeakHashMap, límite de tamaño, o evitar campos estáticos.");
    }
}
