import java.util.*;

/**
 * Ejercicio 3 — Cache hit/miss
 * Demuestra cuántas llamadas reales a DB se ahorran con caché.
 */
public class Ejercicio3 {

    static class Cache {
        private final Map<String, String> store = new HashMap<>();
        private int dbCalls = 0;

        String get(String key) {
            if (store.containsKey(key)) {
                System.out.println("  HIT: " + key);
                return store.get(key);
            }
            // Miss: simula llamada a DB
            dbCalls++;
            String value = "valor_de_" + key;
            System.out.println("  MISS: " + key + " → DB call #" + dbCalls);
            store.put(key, value);
            return value;
        }

        int getDbCalls() { return dbCalls; }
    }

    public static void main(String[] args) {
        Cache cache = new Cache();

        // 5 llamadas, solo 2 keys distintas
        cache.get("producto:P01");  // miss
        cache.get("producto:P01");  // hit
        cache.get("producto:P02");  // miss
        cache.get("producto:P01");  // hit
        cache.get("producto:P02");  // hit

        System.out.println("\nLlamadas al servicio: 5");
        System.out.println("Llamadas reales a DB: " + cache.getDbCalls());
        System.out.println("Ahorradas por caché: " + (5 - cache.getDbCalls()));
    }
}
