import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Simulación de estructuras de datos Redis con Java puro.
 *
 * Conceptos demostrados:
 *  - RedisString: GET/SET/INCR/EXPIRE/TTL — contador de visitas con expiración
 *  - RedisHash: HSET/HGET/HGETALL/HDEL — sesión de usuario con múltiples campos
 *  - RedisSet: SADD/SMEMBERS/SISMEMBER/SUNION — tags únicos por artículo
 *  - RedisSortedSet: ZADD/ZRANK/ZRANGE — ranking de jugadores con score
 *  - RedisList: LPUSH/RPOP — cola de tareas FIFO
 */
public class ExpRedis {

    // ─────────────────────────────────────────────
    // REDIS STRING: GET/SET/INCR/EXPIRE/TTL
    // Uso: caché simple, contadores atómicos, flags
    // ─────────────────────────────────────────────

    static class RedisString {
        private final Map<String, String> store = new HashMap<>();
        private final Map<String, Long> expireAt = new HashMap<>();  // ms desde epoch

        // SET key value [EX seconds]
        void set(String key, String value) {
            store.put(key, value);
            expireAt.remove(key);  // sin TTL
        }

        void set(String key, String value, long ttlSeconds) {
            store.put(key, value);
            expireAt.put(key, System.currentTimeMillis() + ttlSeconds * 1000);
        }

        // GET key → null si expirado o no existe
        Optional<String> get(String key) {
            if (isExpired(key)) {
                store.remove(key);
                expireAt.remove(key);
                return Optional.empty();
            }
            return Optional.ofNullable(store.get(key));
        }

        // INCR key → atómico en Redis real (O(1))
        long incr(String key) {
            if (isExpired(key)) { store.remove(key); expireAt.remove(key); }
            String current = store.getOrDefault(key, "0");
            long newVal = Long.parseLong(current) + 1;
            store.put(key, String.valueOf(newVal));
            return newVal;
        }

        // EXPIRE key seconds
        void expire(String key, long ttlSeconds) {
            if (store.containsKey(key)) {
                expireAt.put(key, System.currentTimeMillis() + ttlSeconds * 1000);
            }
        }

        // TTL key → segundos restantes, -1 si sin TTL, -2 si no existe/expirado
        long ttl(String key) {
            if (!store.containsKey(key) || isExpired(key)) return -2;
            if (!expireAt.containsKey(key)) return -1;
            return Math.max(0, (expireAt.get(key) - System.currentTimeMillis()) / 1000);
        }

        private boolean isExpired(String key) {
            Long exp = expireAt.get(key);
            return exp != null && System.currentTimeMillis() > exp;
        }
    }

    // ─────────────────────────────────────────────
    // REDIS HASH: HSET/HGET/HGETALL/HDEL
    // Uso: objetos con múltiples campos, sesiones de usuario
    // ─────────────────────────────────────────────

    static class RedisHash {
        private final Map<String, Map<String, String>> store = new HashMap<>();

        // HSET key field value
        void hset(String key, String field, String value) {
            store.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(field, value);
        }

        // HGET key field
        Optional<String> hget(String key, String field) {
            return Optional.ofNullable(store.getOrDefault(key, Collections.emptyMap()).get(field));
        }

        // HGETALL key → todos los campos del hash
        Map<String, String> hgetall(String key) {
            return Collections.unmodifiableMap(store.getOrDefault(key, Collections.emptyMap()));
        }

        // HDEL key field
        boolean hdel(String key, String field) {
            Map<String, String> hash = store.get(key);
            return hash != null && hash.remove(field) != null;
        }

        // HEXISTS key field
        boolean hexists(String key, String field) {
            return store.getOrDefault(key, Collections.emptyMap()).containsKey(field);
        }
    }

    // ─────────────────────────────────────────────
    // REDIS SET: SADD/SMEMBERS/SISMEMBER/SUNION
    // Uso: tags únicos, membresías, deduplicación
    // ─────────────────────────────────────────────

    static class RedisSet {
        private final Map<String, Set<String>> store = new HashMap<>();

        // SADD key member — devuelve true si fue añadido (no existía)
        boolean sadd(String key, String member) {
            return store.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(member);
        }

        // SMEMBERS key
        Set<String> smembers(String key) {
            return Collections.unmodifiableSet(store.getOrDefault(key, Collections.emptySet()));
        }

        // SISMEMBER key member
        boolean sismember(String key, String member) {
            return store.getOrDefault(key, Collections.emptySet()).contains(member);
        }

        // SUNION key1 key2 ... → unión de todos los sets
        Set<String> sunion(String... keys) {
            Set<String> resultado = new LinkedHashSet<>();
            for (String key : keys) {
                resultado.addAll(store.getOrDefault(key, Collections.emptySet()));
            }
            return resultado;
        }

        // SINTER key1 key2 → intersección
        Set<String> sinter(String key1, String key2) {
            Set<String> s1 = store.getOrDefault(key1, Collections.emptySet());
            Set<String> s2 = store.getOrDefault(key2, Collections.emptySet());
            return s1.stream().filter(s2::contains).collect(Collectors.toCollection(LinkedHashSet::new));
        }

        int scard(String key) {
            return store.getOrDefault(key, Collections.emptySet()).size();
        }
    }

    // ─────────────────────────────────────────────
    // REDIS SORTED SET: ZADD/ZRANK/ZRANGE
    // Uso: rankings, leaderboards, colas de prioridad
    // ─────────────────────────────────────────────

    static class RedisSortedSet {
        // TreeMap<score, member> — mantenemos ordenación por score
        // En Redis real, el score puede repetirse (varios miembros con el mismo score)
        private final Map<String, Double> scores = new LinkedHashMap<>();

        // ZADD key score member
        void zadd(String member, double score) {
            scores.put(member, score);
        }

        // ZRANGE key 0 -1 → lista de menor a mayor score
        List<String> zrange() {
            return scores.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        // ZRANGE key 0 -1 REV → lista de mayor a menor score
        List<String> zrangeRev() {
            return scores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        // ZRANK key member → posición 0-indexed (de menor a mayor score)
        Optional<Integer> zrank(String member) {
            if (!scores.containsKey(member)) return Optional.empty();
            List<String> ranked = zrange();
            return Optional.of(ranked.indexOf(member));
        }

        // ZSCORE key member
        Optional<Double> zscore(String member) {
            return Optional.ofNullable(scores.get(member));
        }

        // ZRANGEBYSCORE min max (top N por score mínimo)
        List<Map.Entry<String, Double>> zrangebyscore(double min, double max) {
            return scores.entrySet().stream()
                    .filter(e -> e.getValue() >= min && e.getValue() <= max)
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());
        }

        int zcard() { return scores.size(); }
    }

    // ─────────────────────────────────────────────
    // REDIS LIST: LPUSH/RPOP (cola FIFO)
    // Uso: colas de tareas, historial reciente
    // ─────────────────────────────────────────────

    static class RedisList {
        private final Map<String, ArrayDeque<String>> store = new HashMap<>();

        // LPUSH key value — inserta al inicio (head)
        void lpush(String key, String value) {
            store.computeIfAbsent(key, k -> new ArrayDeque<>()).addFirst(value);
        }

        // RPUSH key value — inserta al final (tail)
        void rpush(String key, String value) {
            store.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(value);
        }

        // RPOP key — extrae del final (tail) → cola FIFO con LPUSH+RPOP
        Optional<String> rpop(String key) {
            ArrayDeque<String> list = store.get(key);
            return list != null && !list.isEmpty() ? Optional.of(list.removeLast()) : Optional.empty();
        }

        // LPOP key — extrae del inicio (head)
        Optional<String> lpop(String key) {
            ArrayDeque<String> list = store.get(key);
            return list != null && !list.isEmpty() ? Optional.of(list.removeFirst()) : Optional.empty();
        }

        // LRANGE key 0 -1 → snapshot sin extraer
        List<String> lrange(String key) {
            return new ArrayList<>(store.getOrDefault(key, new ArrayDeque<>()));
        }

        // LLEN key
        int llen(String key) {
            return store.getOrDefault(key, new ArrayDeque<>()).size();
        }
    }

    // ─────────────────────────────────────────────
    // MAIN: demo de cada estructura
    // ─────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ExpRedis: Estructuras de datos Redis simuladas ===\n");

        // ── String: contador de visitas con expiración ──
        System.out.println("── 1. RedisString: contador de visitas + TTL ──");
        RedisString redis = new RedisString();
        redis.set("visitas:home", "0");
        for (int i = 0; i < 5; i++) redis.incr("visitas:home");
        redis.expire("visitas:home", 2);
        System.out.printf("  visitas:home = %s (TTL ~%ds)%n",
                redis.get("visitas:home").orElse("null"), redis.ttl("visitas:home"));

        System.out.println("  Esperando 2.5s para que expire...");
        Thread.sleep(2500);
        System.out.printf("  visitas:home tras expirar = %s%n",
                redis.get("visitas:home").orElse("null (expirado)"));

        // ── Hash: sesión de usuario ──
        System.out.println("\n── 2. RedisHash: sesión de usuario ──");
        RedisHash hash = new RedisHash();
        hash.hset("session:user:42", "userId", "42");
        hash.hset("session:user:42", "role", "ADMIN");
        hash.hset("session:user:42", "lastSeen", "2024-01-15T10:30:00Z");
        hash.hset("session:user:42", "locale", "es-ES");
        System.out.println("  HGETALL session:user:42 = " + hash.hgetall("session:user:42"));
        hash.hdel("session:user:42", "lastSeen");
        System.out.println("  Tras HDEL lastSeen: " + hash.hgetall("session:user:42"));

        // ── Set: tags únicos por artículo ──
        System.out.println("\n── 3. RedisSet: tags únicos ──");
        RedisSet set = new RedisSet();
        set.sadd("tags:articulo:1", "java");
        set.sadd("tags:articulo:1", "spring");
        set.sadd("tags:articulo:1", "backend");
        boolean añadido = set.sadd("tags:articulo:1", "java");  // duplicado → false
        System.out.println("  tags articulo 1: " + set.smembers("tags:articulo:1"));
        System.out.println("  'java' añadido de nuevo (duplicado): " + añadido);

        set.sadd("tags:articulo:2", "java");
        set.sadd("tags:articulo:2", "kafka");
        set.sadd("tags:articulo:2", "streaming");

        Set<String> union = set.sunion("tags:articulo:1", "tags:articulo:2");
        Set<String> inter = set.sinter("tags:articulo:1", "tags:articulo:2");
        System.out.println("  SUNION (todos los tags): " + union);
        System.out.println("  SINTER (tags comunes): " + inter);

        // ── Sorted Set: ranking de jugadores ──
        System.out.println("\n── 4. RedisSortedSet: leaderboard ──");
        RedisSortedSet ss = new RedisSortedSet();
        ss.zadd("ana",    1500.0);
        ss.zadd("carlos", 2300.0);
        ss.zadd("bea",    1800.0);
        ss.zadd("david",  2100.0);
        ss.zadd("elena",  900.0);

        System.out.println("  Ranking (menor a mayor score): " + ss.zrange());
        System.out.println("  Top 3 (mayor a menor):         " + ss.zrangeRev().subList(0, 3));
        System.out.printf("  ZRANK 'bea' = %d (0-indexed asc)%n", ss.zrank("bea").orElse(-1));
        System.out.printf("  ZSCORE 'carlos' = %.0f%n", ss.zscore("carlos").orElse(0.0));

        // ── List: cola de tareas FIFO ──
        System.out.println("\n── 5. RedisList: cola de tareas FIFO ──");
        RedisList list = new RedisList();
        // LPUSH → el último añadido queda al frente, RPOP → saca el primero añadido (FIFO)
        list.rpush("cola:emails", "email:welcome:user1");
        list.rpush("cola:emails", "email:reset:user2");
        list.rpush("cola:emails", "email:notification:user3");
        System.out.println("  Cola inicial: " + list.lrange("cola:emails"));
        System.out.printf("  Procesando: %s (quedan %d)%n",
                list.lpop("cola:emails").orElse("vacía"), list.llen("cola:emails"));
        System.out.printf("  Procesando: %s (quedan %d)%n",
                list.lpop("cola:emails").orElse("vacía"), list.llen("cola:emails"));
    }
}
