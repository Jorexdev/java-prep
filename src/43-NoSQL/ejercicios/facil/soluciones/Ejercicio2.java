import java.util.*;

/**
 * Ejercicio 2 — Redis String con TTL
 * Simula GET/SET con expiración basada en timestamp.
 */
public class Ejercicio2 {

    static class RedisString {
        private final Map<String, String> store = new HashMap<>();
        private final Map<String, Long> expireAt = new HashMap<>();

        void set(String key, String value, long ttlSeconds) {
            store.put(key, value);
            expireAt.put(key, System.currentTimeMillis() + ttlSeconds * 1000);
        }

        String get(String key) {
            Long exp = expireAt.get(key);
            if (exp != null && System.currentTimeMillis() > exp) {
                store.remove(key);
                expireAt.remove(key);
                return null;
            }
            return store.get(key);
        }

        long ttl(String key) {
            Long exp = expireAt.get(key);
            if (exp == null) return -1;
            long remaining = (exp - System.currentTimeMillis()) / 1000;
            return Math.max(remaining, 0);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        RedisString redis = new RedisString();

        redis.set("session:123", "user-data", 2);

        System.out.println("Lectura inmediata: " + redis.get("session:123"));
        System.out.println("TTL restante: ~" + redis.ttl("session:123") + "s");

        System.out.println("Esperando 3 segundos...");
        Thread.sleep(3000);

        System.out.println("Lectura tras expirar: " + redis.get("session:123"));
        // Espera: null (la key expiró)
    }
}
