import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.*;

public class Ejercicio5 {

    // Detector de memory leaks manual usando WeakReference + ReferenceQueue
    // Rastrea objetos candidatos a GC y detecta cuando NO son recogidos
    static class LeakDetector<T> {
        // Asocia cada referencia debil con el nombre del objeto que rastrean
        private final Map<WeakReference<T>, String> trackedRefs = new IdentityHashMap<>();
        private final ReferenceQueue<T> queue = new ReferenceQueue<>();
        private int totalRegistered = 0;
        private int totalCollected = 0;

        // Registra un objeto para rastreo
        void track(T obj, String name) {
            WeakReference<T> ref = new WeakReference<>(obj, queue);
            trackedRefs.put(ref, name);
            totalRegistered++;
            System.out.printf("  [track] '%s' registrado (total rastreados: %d)%n",
                    name, trackedRefs.size());
        }

        // Procesa la ReferenceQueue: los objetos encolados han sido recogidos por el GC
        int processQueue() {
            int collected = 0;
            Reference<? extends T> ref;
            while ((ref = queue.poll()) != null) {
                String name = trackedRefs.remove(ref);
                if (name != null) {
                    totalCollected++;
                    collected++;
                    System.out.printf("  [GC] '%s' fue recogido por el GC%n", name);
                }
            }
            return collected;
        }

        // Objetos aun vivos (no recogidos)
        List<String> getLiveObjects() {
            List<String> live = new ArrayList<>();
            for (Map.Entry<WeakReference<T>, String> e : trackedRefs.entrySet()) {
                if (e.getKey().get() != null) live.add(e.getValue());
            }
            return live;
        }

        // Posibles leaks: objetos registrados cuya ref ya fue encolada pero quedaron en el mapa
        List<String> getPotentialLeaks() {
            List<String> leaks = new ArrayList<>();
            for (Map.Entry<WeakReference<T>, String> e : trackedRefs.entrySet()) {
                if (e.getKey().get() == null) leaks.add(e.getValue() + " (ref nula, no encolada aun)");
            }
            return leaks;
        }

        void printReport() {
            System.out.println();
            System.out.println("  --- Informe del LeakDetector ---");
            System.out.printf("  Total registrados  : %d%n", totalRegistered);
            System.out.printf("  Recogidos por GC   : %d%n", totalCollected);
            System.out.printf("  Aun vivos          : %d%n", getLiveObjects().size());
            List<String> leaks = getPotentialLeaks();
            System.out.printf("  Posibles leaks     : %d%n", leaks.size());
            if (!leaks.isEmpty()) {
                System.out.println("  Detalle leaks:");
                leaks.forEach(l -> System.out.println("    - " + l));
            }
        }
    }

    // Objeto simulado que representa un recurso (ej. resultado de query)
    static class CachedResult {
        private final String key;
        private final byte[] payload;

        CachedResult(String key, int sizeKb) {
            this.key = key;
            this.payload = new byte[sizeKb * 1024];
        }

        @Override public String toString() { return "CachedResult[" + key + "]"; }
    }

    // Cache que puede ser fuente de memory leaks si no controla sus entradas
    static class CacheSinControl {
        private final Map<String, CachedResult> map = new HashMap<>();

        void put(String key, CachedResult val) { map.put(key, val); }
        CachedResult get(String key) { return map.get(key); }
        int size() { return map.size(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Memory Leak Detector con WeakReference + ReferenceQueue ===");
        System.out.println();

        LeakDetector<CachedResult> detector = new LeakDetector<>();

        // --- Escenario 1: objetos que se liberan correctamente ---
        System.out.println("[ Escenario 1 ] Objetos sin referencia fuerte (deben ser recogidos)");
        System.out.println();

        for (int i = 1; i <= 5; i++) {
            CachedResult res = new CachedResult("query-" + i, 100);
            detector.track(res, "query-" + i);
            // res sale de scope aqui: no hay referencia fuerte
        }

        System.out.println("  Eliminando referencias fuertes y llamando GC...");
        System.gc();
        Thread.sleep(200); // dar tiempo al GC para encolar referencias
        System.gc();
        Thread.sleep(100);

        int collected = detector.processQueue();
        System.out.printf("  Objetos recogidos en este GC: %d%n", collected);
        detector.printReport();

        // --- Escenario 2: leak simulado con cache sin control ---
        System.out.println();
        System.out.println("[ Escenario 2 ] Cache sin control: retiene referencias fuertes (LEAK)");
        System.out.println();

        CacheSinControl cache = new CacheSinControl();
        LeakDetector<CachedResult> detectorLeak = new LeakDetector<>();

        for (int i = 1; i <= 5; i++) {
            CachedResult res = new CachedResult("leak-" + i, 50);
            cache.put("leak-" + i, res);        // referencia fuerte en el cache
            detectorLeak.track(res, "leak-" + i); // solo referencia debil en el detector
        }

        System.out.println("  Llamando GC... (el cache aun retiene referencias fuertes)");
        System.gc();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(100);

        int collectedLeak = detectorLeak.processQueue();
        System.out.printf("  Objetos recogidos (deberian ser 0, el cache los retiene): %d%n",
                collectedLeak);
        System.out.printf("  Cache size: %d (las entradas NO se liberaron)%n", cache.size());
        detectorLeak.printReport();

        // --- Escenario 3: liberar el cache y verificar recogida ---
        System.out.println();
        System.out.println("[ Escenario 3 ] Vaciando el cache -> objetos deben ser recogidos");
        System.out.println();

        cache = null; // eliminar referencia fuerte al cache completo
        System.gc();
        Thread.sleep(300);
        System.gc();
        Thread.sleep(100);

        int collectedAfterClear = detectorLeak.processQueue();
        System.out.printf("  Objetos recogidos tras vaciar cache: %d%n", collectedAfterClear);
        detectorLeak.printReport();

        System.out.println();
        System.out.println("=== Conclusion ===");
        System.out.println("WeakReference + ReferenceQueue permite detectar objetos que el GC recogio.");
        System.out.println("Si un objeto rastreado nunca aparece en la queue, hay un leak:");
        System.out.println("  alguien mantiene una referencia fuerte inesperada.");
        System.out.println("Util en frameworks de cache, pools y profilers ligeros.");
    }
}
