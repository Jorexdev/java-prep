import java.util.function.Supplier;

// Ejercicio 6 (Fácil) — Lazy logging
// debug(Supplier<String>) solo evalúa el Supplier si DEBUG está activo
public class Ejercicio6 {

    enum Level { TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);
        final int v; Level(int v) { this.v = v; }
    }

    static class Logger {
        private final String name;
        private Level minLevel;

        Logger(String name, Level minLevel) {
            this.name = name;
            this.minLevel = minLevel;
        }

        public void setLevel(Level l) { this.minLevel = l; }
        public boolean isDebugEnabled() { return Level.DEBUG.v >= minLevel.v; }

        // --- Versión NO lazy (siempre evalúa el mensaje) ---
        public void debug(String message) {
            if (Level.DEBUG.v >= minLevel.v) {
                System.out.println("[DEBUG] " + name + " - " + message);
            }
        }

        // --- Versión LAZY (solo evalúa el Supplier si DEBUG activo) ---
        public void debug(Supplier<String> messageSupplier) {
            if (Level.DEBUG.v >= minLevel.v) {
                System.out.println("[DEBUG] " + name + " - " + messageSupplier.get());
            }
        }

        public void info(String msg) {
            if (Level.INFO.v >= minLevel.v) System.out.println("[INFO ] " + name + " - " + msg);
        }
    }

    // Operación costosa que NO queremos llamar si no es necesario
    static String computeExpensive() {
        System.out.println("  *** computeExpensive() EVALUANDO... (esto es costoso) ***");
        // Simula trabajo costoso
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append(i);
        return "resultado-de-" + sb.length() + "-chars";
    }

    public static void main(String[] args) {
        System.out.println("=== Lazy logging ===");
        System.out.println();

        Logger logger = new Logger("com.app.DataProcessor", Level.INFO);

        // --- Sin lazy: siempre evalúa, aunque no loguee ---
        System.out.println("=== Sin lazy (nivel=INFO, DEBUG inactivo) ===");
        System.out.println("Usando: logger.debug(\"prefijo \" + computeExpensive())");
        System.out.println();
        logger.debug("prefijo " + computeExpensive()); // computeExpensive() SE LLAMA aunque no loguee
        System.out.println("Resultado: mensaje DEBUG no apareció (filtrado), pero computeExpensive() SÍ se llamó");

        System.out.println();

        // --- Con lazy: solo evalúa si DEBUG activo ---
        System.out.println("=== Con lazy (nivel=INFO, DEBUG inactivo) ===");
        System.out.println("Usando: logger.debug(() -> \"prefijo \" + computeExpensive())");
        System.out.println();
        logger.debug(() -> "prefijo " + computeExpensive()); // computeExpensive() NO se llama
        System.out.println("Resultado: computeExpensive() NO fue llamado (correcto)");

        System.out.println();

        // --- Con lazy cuando DEBUG sí está activo ---
        System.out.println("=== Con lazy (nivel=DEBUG, DEBUG activo) ===");
        logger.setLevel(Level.DEBUG);
        System.out.println("Usando: logger.debug(() -> \"prefijo \" + computeExpensive())");
        System.out.println();
        logger.debug(() -> "prefijo " + computeExpensive()); // ahora SÍ se llama
        System.out.println("Resultado: computeExpensive() SÍ fue llamado porque DEBUG está activo");

        System.out.println();

        // --- Comparación lado a lado ---
        System.out.println("=== Comparación de evaluaciones (nivel=INFO) ===");
        logger.setLevel(Level.INFO);

        System.out.println("Llamada 1 (no lazy):");
        logger.debug("msg " + computeExpensive());
        System.out.println("  → Supplier evaluado: SÍ (innecesario)");

        System.out.println();
        System.out.println("Llamada 2 (lazy):");
        logger.debug(() -> "msg " + computeExpensive());
        System.out.println("  → Supplier evaluado: NO (correcto)");

        System.out.println();
        System.out.println("Conclusión: con lazy logging evitamos el coste de construir");
        System.out.println("mensajes de debug en producción donde DEBUG está desactivado.");
    }
}
