import java.util.*;

/**
 * Ejercicio 2 (Medio) — Cache Aside pattern
 * Demuestra: miss→load, hit, update+evict, miss→reload.
 */
public class Ejercicio2 {

    static class CacheAsideService {
        private final Map<String, String> cache = new HashMap<>();
        private final Map<String, String> database;
        private int dbLoads = 0;

        CacheAsideService(Map<String, String> database) {
            this.database = database;
        }

        String get(String id) {
            if (cache.containsKey(id)) {
                System.out.println("  [HIT]  " + id + " → " + cache.get(id));
                return cache.get(id);
            }
            // Cache miss: cargar de DB y guardar en caché
            dbLoads++;
            String value = database.get(id);
            System.out.println("  [MISS] " + id + " → DB load #" + dbLoads + ": " + value);
            if (value != null) cache.put(id, value);
            return value;
        }

        void update(String id, String newValue) {
            database.put(id, newValue);
            cache.remove(id);  // evict → el siguiente get recargará desde DB
            System.out.println("  [UPDATE+EVICT] " + id + " actualizado en DB, eliminado del caché");
        }

        int getDbLoads() { return dbLoads; }
    }

    public static void main(String[] args) {
        Map<String, String> db = new HashMap<>();
        db.put("producto:P01", "Laptop 1200€");
        db.put("producto:P02", "Monitor 350€");

        CacheAsideService service = new CacheAsideService(db);

        System.out.println("1. get (miss → load desde DB):");
        service.get("producto:P01");

        System.out.println("\n2. get (hit desde caché):");
        service.get("producto:P01");

        System.out.println("\n3. update → evict del caché:");
        service.update("producto:P01", "Laptop Pro 1500€");

        System.out.println("\n4. get tras update (miss → recarga desde DB con valor nuevo):");
        service.get("producto:P01");

        System.out.println("\n5. get (hit con valor actualizado):");
        service.get("producto:P01");

        System.out.println("\nDB loads totales: " + service.getDbLoads() + " (de 4 get calls)");
    }
}
