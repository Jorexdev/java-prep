import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Simulación de Spring Cache con Redis como backend.
 *
 * Conceptos demostrados:
 *  - @Cacheable: cache-aside transparente (miss → ejecuta método → guarda en Redis)
 *  - @CacheEvict: invalida la entry al actualizar el dato en DB
 *  - @CachePut: siempre ejecuta el método Y actualiza Redis (sin consultar caché)
 *  - Cache key generation: método + parámetros como key por defecto
 *  - Contadores de "database calls" para demostrar el ahorro
 */
public class ExpSpringCacheRedis {

    // ─────────────────────────────────────────────
    // CACHE: simula Redis como backend de Spring Cache
    // ─────────────────────────────────────────────

    static class Cache {
        private final String nombre;
        private final Map<String, Object> store = new HashMap<>();
        private int hits = 0;
        private int misses = 0;

        Cache(String nombre) {
            this.nombre = nombre;
        }

        Optional<Object> get(String key) {
            if (store.containsKey(key)) {
                hits++;
                System.out.printf("    [CACHE HIT]  %s::%s%n", nombre, key);
                return Optional.of(store.get(key));
            }
            misses++;
            System.out.printf("    [CACHE MISS] %s::%s → ejecutando método...%n", nombre, key);
            return Optional.empty();
        }

        void put(String key, Object value) {
            store.put(key, value);
            System.out.printf("    [CACHE PUT]  %s::%s = %s%n", nombre, key, value);
        }

        void evict(String key) {
            if (store.remove(key) != null) {
                System.out.printf("    [CACHE EVICT] %s::%s eliminada%n", nombre, key);
            }
        }

        void evictAll() {
            store.clear();
            System.out.printf("    [CACHE CLEAR] %s — todas las entries eliminadas%n", nombre);
        }

        boolean contains(String key) { return store.containsKey(key); }
        int hits()   { return hits; }
        int misses() { return misses; }

        void printStats() {
            System.out.printf("  Cache '%s': hits=%d, misses=%d, entries=%d%n",
                    nombre, hits, misses, store.size());
        }
    }

    // ─────────────────────────────────────────────
    // CACHE MANAGER: gestiona múltiples caches nombrados
    // En Spring: RedisCacheManager, con TTL y serialización configurables
    // ─────────────────────────────────────────────

    static class CacheManager {
        private final Map<String, Cache> caches = new HashMap<>();

        Cache getCache(String nombre) {
            return caches.computeIfAbsent(nombre, Cache::new);
        }
    }

    // ─────────────────────────────────────────────
    // ENTIDAD Y REPOSITORIO (simulan DB)
    // ─────────────────────────────────────────────

    record Producto(String id, String nombre, double precio) {}

    static class ProductoRepository {
        private final Map<String, Producto> db = new HashMap<>();
        private final AtomicInteger dbCalls = new AtomicInteger(0);

        ProductoRepository() {
            db.put("P01", new Producto("P01", "Laptop",    1200.0));
            db.put("P02", new Producto("P02", "Monitor",   350.0));
            db.put("P03", new Producto("P03", "Teclado",   80.0));
        }

        Producto findById(String id) {
            int call = dbCalls.incrementAndGet();
            System.out.printf("    [DB CALL #%d] SELECT * FROM productos WHERE id='%s'%n", call, id);
            return db.get(id);
        }

        Producto save(Producto p) {
            int call = dbCalls.incrementAndGet();
            System.out.printf("    [DB CALL #%d] UPDATE productos SET nombre='%s', precio=%.2f WHERE id='%s'%n",
                    call, p.nombre(), p.precio(), p.id());
            db.put(p.id(), p);
            return p;
        }

        int getDbCalls() { return dbCalls.get(); }
    }

    // ─────────────────────────────────────────────
    // PRODUCTO SERVICE: simula @Cacheable, @CacheEvict, @CachePut
    // ─────────────────────────────────────────────

    static class ProductoService {
        private final ProductoRepository repository;
        private final Cache cache;

        ProductoService(ProductoRepository repository, CacheManager cacheManager) {
            this.repository = repository;
            this.cache = cacheManager.getCache("productos");
        }

        // @Cacheable(value = "productos", key = "#id")
        // Spring AOP intercepta la llamada, genera key, busca en Redis.
        // Si hit → devuelve sin ejecutar el método.
        // Si miss → ejecuta, guarda en Redis, devuelve.
        Producto findById(String id) {
            // Spring genera la key: "productos::" + id
            String cacheKey = id;
            return (Producto) cache.get(cacheKey).orElseGet(() -> {
                Producto p = repository.findById(id);
                if (p != null) cache.put(cacheKey, p);
                return p;
            });
        }

        // @CacheEvict(value = "productos", key = "#id")
        // Invalida la entry en Redis. El siguiente findById hará miss y recargará de DB.
        void evictFromCache(String id) {
            System.out.printf("    [@CacheEvict] invalidando key '%s'%n", id);
            cache.evict(id);
        }

        // @CachePut(value = "productos", key = "#result.id")
        // Siempre ejecuta el método (guarda en DB) Y actualiza Redis.
        // Sin consultar caché: garantiza que Redis queda actualizado tras cada escritura.
        Producto actualizar(Producto producto) {
            Producto saved = repository.save(producto);
            // @CachePut: actualiza independientemente de si había hit o miss
            cache.put(saved.id(), saved);
            System.out.printf("    [@CachePut] Redis actualizado con nueva versión de '%s'%n", saved.id());
            return saved;
        }

        // @CacheEvict(value = "productos", allEntries = true)
        // Limpia todo el caché de "productos". Útil tras migraciones masivas.
        void evictAll() {
            cache.evictAll();
        }
    }

    // ─────────────────────────────────────────────
    // DEMO: @Cacheable vs @CachePut vs @CacheEvict
    // ─────────────────────────────────────────────

    static void demoCacheable() {
        System.out.println("══ Demo @Cacheable ══");
        CacheManager cacheManager = new CacheManager();
        ProductoRepository repo = new ProductoRepository();
        ProductoService service = new ProductoService(repo, cacheManager);

        System.out.println("\n  Llamada 1 → MISS (primera vez):");
        Producto p1 = service.findById("P01");
        System.out.printf("  Resultado: %s%n", p1);

        System.out.println("\n  Llamada 2 → HIT (mismo id, ya en caché):");
        Producto p1cached = service.findById("P01");
        System.out.printf("  Resultado: %s%n", p1cached);

        System.out.println("\n  Llamada 3 → HIT (tercera llamada, sigue en caché):");
        service.findById("P01");

        System.out.println("\n  Llamada 4 → MISS (primer acceso a P02):");
        service.findById("P02");

        System.out.printf("%n  DB calls reales: %d (de 4 llamadas al servicio)%n", repo.getDbCalls());
        cacheManager.getCache("productos").printStats();
    }

    static void demoCacheEvict() {
        System.out.println("\n══ Demo @CacheEvict ══");
        CacheManager cacheManager = new CacheManager();
        ProductoRepository repo = new ProductoRepository();
        ProductoService service = new ProductoService(repo, cacheManager);

        System.out.println("\n  Cargando P01 en caché (MISS):");
        service.findById("P01");

        System.out.println("\n  Cargando P01 de nuevo (HIT):");
        service.findById("P01");

        System.out.println("\n  Simulando actualización → @CacheEvict:");
        service.evictFromCache("P01");

        System.out.println("\n  Siguiente acceso → MISS (caché invalidado, recarga de DB):");
        service.findById("P01");

        System.out.printf("%n  DB calls totales: %d%n", repo.getDbCalls());
    }

    static void demoCachePut() {
        System.out.println("\n══ Demo @CachePut ══");
        CacheManager cacheManager = new CacheManager();
        ProductoRepository repo = new ProductoRepository();
        ProductoService service = new ProductoService(repo, cacheManager);

        System.out.println("\n  Actualizando P02 con @CachePut:");
        Producto actualizado = service.actualizar(new Producto("P02", "Monitor 4K", 420.0));
        System.out.printf("  Producto actualizado: %s%n", actualizado);

        System.out.println("\n  Leyendo P02 → HIT (caché caliente gracias a @CachePut):");
        Producto leído = service.findById("P02");
        System.out.printf("  Leído desde caché: %s%n", leído);

        System.out.printf("%n  Nota: @CachePut evita que el siguiente caller haga miss.%n");
        System.out.printf("  DB calls totales: %d (1 update + 0 selects)%n", repo.getDbCalls());
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== ExpSpringCacheRedis: Spring Cache + Redis simulado ===\n");

        demoCacheable();
        demoCacheEvict();
        demoCachePut();

        System.out.println("""

══ Resumen de anotaciones Spring Cache ══
  @Cacheable  → Consulta caché. Miss → ejecuta + guarda. Hit → devuelve sin ejecutar.
               Ideal para: findById, getProducto, getUserProfile (operaciones de lectura).

  @CacheEvict → Invalida una o varias keys. No ejecuta la query de caché.
               Ideal para: delete, cuando los datos cambian desde fuera del servicio.
               allEntries=true para limpiar todo el caché de ese nombre.

  @CachePut   → Siempre ejecuta el método Y actualiza Redis. Sin hit/miss check.
               Ideal para: update (garantiza que Redis queda con el valor nuevo).
               Sin @CachePut en el update, el caché quedaría obsoleto hasta que expire.

  Key generation por defecto: cacheName + "::" + parámetros del método.
  Custom key: @Cacheable(key = "#usuario.id + ':' + #tipo")
  Condition:  @Cacheable(condition = "#id.length() > 3") — solo cachea si se cumple
  Unless:     @Cacheable(unless = "#result == null") — no cachea resultados nulos""");
    }
}
