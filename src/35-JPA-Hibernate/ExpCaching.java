import java.util.HashMap;
import java.util.Map;

// Simula la caché de primer nivel (L1, por sesión/EntityManager)
// y la de segundo nivel (L2, compartida entre sesiones).
// Cada "SELECT a BD" se imprime y cuenta para mostrar el impacto real.

// ── Entidad ───────────────────────────────────────────────────────────────────

// @Entity @Cacheable (L2 requiere esta anotación + config de proveedor, ej. EhCache)
class Producto {
    private final Long id;
    private final String nombre;
    private final double precio;

    Producto(Long id, String nombre, double precio) {
        this.id     = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public Long   getId()     { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + "}";
    }
}

// ── Base de datos simulada ────────────────────────────────────────────────────

class Database {
    private static final Map<Long, Producto> tabla = Map.of(
        1L, new Producto(1L, "Laptop Pro",    1299.99),
        2L, new Producto(2L, "Teclado Mec.",   89.99),
        3L, new Producto(3L, "Monitor 4K",    349.00)
    );

    private int queryCount = 0;

    public Producto select(Long id) {
        queryCount++;
        System.out.println("    [DB SQL " + queryCount + "] SELECT * FROM producto WHERE id = " + id);
        return tabla.get(id);
    }

    public int  getQueryCount()   { return queryCount; }
    public void resetQueryCount() { queryCount = 0; }
}

// ── L2 Cache (compartida entre sessions) ─────────────────────────────────────

// Equivale a la región de caché configurada con @Cacheable + EhCache/Caffeine
class L2Cache {
    private final Map<Long, Producto> cache = new HashMap<>();

    public Producto get(Long id) {
        Producto p = cache.get(id);
        if (p != null) System.out.println("    [L2  ] HIT  id=" + id + " → " + p);
        else           System.out.println("    [L2  ] MISS id=" + id);
        return p;
    }

    public void put(Long id, Producto producto) {
        cache.put(id, producto);
        System.out.println("    [L2  ] PUT  id=" + id);
    }

    public boolean isEnabled() { return true; }
}

// ── EntityManager (L1 cache por sesión) ──────────────────────────────────────

// Equivale a javax.persistence.EntityManager (una instancia por sesión/request)
class EntityManager {

    private final String sessionId;
    private final Database db;
    private final L2Cache  l2;
    private final boolean  useL2;
    // Mapa de identidad (identity map) — esto es la caché L1
    private final Map<Long, Producto> l1Cache = new HashMap<>();

    EntityManager(String sessionId, Database db, L2Cache l2, boolean useL2) {
        this.sessionId = sessionId;
        this.db        = db;
        this.l2        = l2;
        this.useL2     = useL2;
    }

    // Equivale a em.find(Producto.class, id)
    public Producto find(Long id) {
        System.out.println("  [EM " + sessionId + "] find(id=" + id + ")");

        // 1. L1: busca en el mapa de identidad de esta sesión
        if (l1Cache.containsKey(id)) {
            System.out.println("    [L1  ] HIT  id=" + id + " (misma sesión, no va a BD)");
            return l1Cache.get(id);
        }

        // 2. L2: si está habilitada, consulta la caché compartida
        if (useL2) {
            Producto fromL2 = l2.get(id);
            if (fromL2 != null) {
                l1Cache.put(id, fromL2);   // pobla también L1
                return fromL2;
            }
        }

        // 3. BD: carga real
        Producto p = db.select(id);
        if (p != null) {
            l1Cache.put(id, p);
            if (useL2) l2.put(id, p);
        }
        return p;
    }

    // @Transactional — al cerrar la sesión se invalida L1 (L2 persiste)
    public void close() {
        l1Cache.clear();
        System.out.println("  [EM " + sessionId + "] closed — L1 cache descartada");
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpCaching {
    public static void main(String[] args) {

        Database db = new Database();
        L2Cache  l2 = new L2Cache();

        System.out.println("=== Simulación JPA L1 y L2 Cache ===\n");

        // ─── DEMO 1: L1 — dos find en la misma sesión → 1 sola query ─────────
        System.out.println("[ DEMO 1: L1 Cache — misma sesión ]");
        System.out.println("  Cargamos el mismo id=1 dos veces en la misma sesión.");
        System.out.println("  Segunda llamada debe venir de L1 sin ir a BD.\n");

        db.resetQueryCount();
        EntityManager em1 = new EntityManager("S1", db, l2, false);
        em1.find(1L);
        em1.find(1L);  // L1 hit — sin SQL
        em1.close();
        System.out.println("\n  Queries a BD: " + db.getQueryCount() + " (esperado: 1)");

        System.out.println("\n" + "─".repeat(60) + "\n");

        // ─── DEMO 2: Sin L2 — dos sesiones distintas → 2 queries ────────────
        System.out.println("[ DEMO 2: Sin L2 — dos sesiones distintas ]");
        System.out.println("  Dos EntityManagers independientes cargan el mismo id=2.");
        System.out.println("  Cada uno va a BD porque L2 está deshabilitada.\n");

        db.resetQueryCount();
        EntityManager em2a = new EntityManager("S2a", db, l2, false);
        EntityManager em2b = new EntityManager("S2b", db, l2, false);
        em2a.find(2L);
        em2a.close();
        em2b.find(2L);   // nueva sesión, L1 vacía, sin L2 → BD
        em2b.close();
        System.out.println("\n  Queries a BD: " + db.getQueryCount() + " (esperado: 2)");

        System.out.println("\n" + "─".repeat(60) + "\n");

        // ─── DEMO 3: Con L2 — segunda sesión hit en L2 ───────────────────────
        System.out.println("[ DEMO 3: Con L2 — segunda sesión beneficia de la caché compartida ]");
        System.out.println("  Sesión A carga id=3 → BD + pone en L2.");
        System.out.println("  Sesión B carga id=3 → L2 hit, sin ir a BD.\n");

        db.resetQueryCount();
        L2Cache l2enabled = new L2Cache();
        EntityManager em3a = new EntityManager("S3a", db, l2enabled, true);
        EntityManager em3b = new EntityManager("S3b", db, l2enabled, true);
        em3a.find(3L);   // BD + L2 put
        em3a.close();
        em3b.find(3L);   // L2 hit — sin BD
        em3b.close();
        System.out.println("\n  Queries a BD: " + db.getQueryCount() + " (esperado: 1)");

        System.out.println("\n" + "─".repeat(60) + "\n");

        // ─── Resumen ──────────────────────────────────────────────────────────
        System.out.println("[ RESUMEN ]");
        System.out.printf("  %-40s %s%n", "Escenario", "Queries a BD");
        System.out.printf("  %-40s %s%n", "-".repeat(40), "-".repeat(12));
        System.out.printf("  %-40s %s%n", "L1: mismo id, misma sesión",       "1");
        System.out.printf("  %-40s %s%n", "Sin L2: mismo id, 2 sesiones",     "2");
        System.out.printf("  %-40s %s%n", "Con L2: mismo id, 2 sesiones",     "1");
        System.out.println();
        System.out.println("  L1 es automática en JPA (identityMap por EntityManager).");
        System.out.println("  L2 requiere configuración explícita: @Cacheable + proveedor (EhCache/Caffeine).");
        System.out.println("  L2 invalida entradas si la entidad se actualiza (@CacheEvict).");
    }
}
