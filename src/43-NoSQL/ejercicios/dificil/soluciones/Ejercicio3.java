import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ejercicio 3 (Difícil) — Write-through vs Write-back cache
 *
 * Write-through: cada escritura va a cache Y DB de forma síncrona.
 *   → Consistencia fuerte. DB siempre actualizada. Mayor latencia de escritura.
 *   → Cuándo: datos críticos donde la consistencia es prioritaria (inventario, saldos).
 *
 * Write-back (write-behind): escribe solo en cache. DB se actualiza en batch diferido.
 *   → Mayor throughput. Menor latencia. Riesgo de pérdida si cache cae antes del flush.
 *   → Cuándo: contadores de visitas, logs, métricas donde perder algún dato es tolerable.
 */
public class Ejercicio3 {

    // ─── Interfaz común ───
    interface CacheStrategy {
        void write(String key, String value);
        void flush();  // fuerza escritura a DB (relevante en write-back)
        int getDbWrites();
        Map<String, String> getCacheSnapshot();
    }

    // ─── Simulador de DB ───
    static class Database {
        private final Map<String, String> store = new HashMap<>();
        private final AtomicInteger writeCount = new AtomicInteger(0);

        void write(String key, String value) {
            writeCount.incrementAndGet();
            store.put(key, value);
        }

        int getWriteCount() { return writeCount.get(); }
        Map<String, String> getStore() { return Collections.unmodifiableMap(store); }
    }

    // ─────────────────────────────────────────────
    // WRITE-THROUGH: escribe en cache Y DB de forma síncrona
    // Cada write → 1 DB write inmediato
    // ─────────────────────────────────────────────
    static class WriteThroughCache implements CacheStrategy {
        private final Map<String, String> cache = new HashMap<>();
        private final Database db;

        WriteThroughCache(Database db) { this.db = db; }

        @Override
        public void write(String key, String value) {
            cache.put(key, value);   // 1. escribe en cache
            db.write(key, value);    // 2. escribe en DB sincrónamente
        }

        @Override
        public void flush() {
            // No-op: write-through ya escribe en DB en cada write
        }

        @Override
        public int getDbWrites() { return db.getWriteCount(); }

        @Override
        public Map<String, String> getCacheSnapshot() { return Collections.unmodifiableMap(cache); }
    }

    // ─────────────────────────────────────────────
    // WRITE-BACK: escribe solo en cache, acumula cambios, flush diferido
    // N writes → 1 DB write por key única al hacer flush
    // ─────────────────────────────────────────────
    static class WriteBackCache implements CacheStrategy {
        private final Map<String, String> cache = new HashMap<>();
        private final Set<String> dirtyKeys = new LinkedHashSet<>();  // keys pendientes de flush
        private final Database db;

        WriteBackCache(Database db) { this.db = db; }

        @Override
        public void write(String key, String value) {
            cache.put(key, value);
            dirtyKeys.add(key);  // marca como "sucia" (pendiente de DB write)
            // NO escribe en DB todavía
        }

        @Override
        public void flush() {
            System.out.printf("  [FLUSH] %d keys sucias → DB%n", dirtyKeys.size());
            for (String key : dirtyKeys) {
                db.write(key, cache.get(key));
            }
            dirtyKeys.clear();
        }

        @Override
        public int getDbWrites() { return db.getWriteCount(); }

        @Override
        public Map<String, String> getCacheSnapshot() { return Collections.unmodifiableMap(cache); }

        public int getDirtyCount() { return dirtyKeys.size(); }
    }

    public static void main(String[] args) {
        System.out.println("=== Write-through vs Write-back ===\n");

        // ── Write-through ──
        System.out.println("── Write-through (10 escrituras) ──");
        Database dbWT = new Database();
        WriteThroughCache wt = new WriteThroughCache(dbWT);

        for (int i = 1; i <= 10; i++) {
            String key = "key:" + (i % 3);  // 3 keys distintas, algunos updates repetidos
            wt.write(key, "valor-" + i);
            System.out.printf("  write(%s, valor-%d) → DB write inmediato%n", key, i);
        }
        System.out.printf("  Total DB writes: %d (1 por cada write)%n%n", wt.getDbWrites());

        // ── Write-back ──
        System.out.println("── Write-back (10 escrituras) ──");
        Database dbWB = new Database();
        WriteBackCache wb = new WriteBackCache(dbWB);

        for (int i = 1; i <= 10; i++) {
            String key = "key:" + (i % 3);
            wb.write(key, "valor-" + i);
            System.out.printf("  write(%s, valor-%d) → solo en caché (dirty keys: %d)%n",
                    key, i, wb.getDirtyCount());
        }
        System.out.printf("  DB writes ANTES del flush: %d%n", wb.getDbWrites());
        wb.flush();
        System.out.printf("  DB writes DESPUÉS del flush: %d (1 por key única)%n%n", wb.getDbWrites());

        // ── Comparativa ──
        System.out.println("── Comparativa ──");
        System.out.printf("  Write-through: %d DB writes para 10 operaciones%n", dbWT.getWriteCount());
        System.out.printf("  Write-back:    %d DB writes para 10 operaciones (flush agrupa)%n", dbWB.getWriteCount());

        System.out.println("""

  Cuándo usar cada estrategia:
    Write-through → consistencia fuerte, datos críticos (saldos, inventario).
                    DB y caché siempre sincronizados. Mayor latencia de escritura.

    Write-back    → máximo throughput, datos donde la eventual consistencia es ok.
                    Riesgo: si el nodo cae antes del flush, se pierden los writes sucios.
                    Ideal para: contadores de visitas, métricas, logs de actividad.""");
    }
}
