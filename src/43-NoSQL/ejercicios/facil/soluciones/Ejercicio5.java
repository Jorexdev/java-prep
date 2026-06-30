import java.util.*;

/**
 * Ejercicio 5 — Redis Hash como sesión de usuario
 * Simula HSET/HGET/HGETALL/HDEL de Redis.
 */
public class Ejercicio5 {

    static class RedisHash {
        private final Map<String, Map<String, String>> store = new HashMap<>();

        void hset(String key, String field, String value) {
            store.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(field, value);
        }

        Optional<String> hget(String key, String field) {
            return Optional.ofNullable(store.getOrDefault(key, Collections.emptyMap()).get(field));
        }

        Map<String, String> hgetall(String key) {
            return Collections.unmodifiableMap(store.getOrDefault(key, Collections.emptyMap()));
        }

        boolean hdel(String key, String field) {
            Map<String, String> hash = store.get(key);
            return hash != null && hash.remove(field) != null;
        }
    }

    public static void main(String[] args) {
        RedisHash redis = new RedisHash();

        String sessionKey = "session:user:42";
        redis.hset(sessionKey, "userId",   "42");
        redis.hset(sessionKey, "role",     "ADMIN");
        redis.hset(sessionKey, "lastSeen", "2024-01-15T10:30:00Z");

        System.out.println("HGETALL: " + redis.hgetall(sessionKey));
        System.out.println("HGET role: " + redis.hget(sessionKey, "role").orElse("no existe"));

        boolean eliminado = redis.hdel(sessionKey, "lastSeen");
        System.out.println("HDEL lastSeen: " + eliminado);

        System.out.println("Estado final: " + redis.hgetall(sessionKey));
        // Espera: {userId=42, role=ADMIN}
    }
}
