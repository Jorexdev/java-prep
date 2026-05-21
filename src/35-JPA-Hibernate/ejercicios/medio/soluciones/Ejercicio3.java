import java.util.HashMap;
import java.util.Map;

public class Ejercicio3 {

    // @Entity
    static class Producto {
        // @Id
        int id;
        String nombre;
        double precio;

        Producto(int id, String nombre, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }

        @Override
        public String toString() {
            return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + "}";
        }
    }

    static class CacheL2 {
        private final Map<Integer, Producto> cache = new HashMap<>();
        int hits = 0;
        int misses = 0;

        Producto get(int id) {
            Producto p = cache.get(id);
            if (p != null) {
                hits++;
                return p;
            }
            misses++;
            return null;
        }

        void put(int id, Producto p) {
            cache.put(id, p);
        }

        void printStats() {
            System.out.println("CacheL2 — hits: " + hits + ", misses: " + misses +
                ", ratio: " + String.format("%.0f%%", hits * 100.0 / (hits + misses)));
        }
    }

    static class EntityManager {
        private final Map<Integer, Producto> store = new HashMap<>();
        private final CacheL2 cache = new CacheL2();

        void persist(Producto p) {
            store.put(p.id, p);
            cache.put(p.id, p);
        }

        Producto findById(int id) {
            Producto cached = cache.get(id);
            if (cached != null) {
                System.out.println("  [L2 HIT]  id=" + id);
                return cached;
            }
            System.out.println("  [L2 MISS] id=" + id + " → consulta a BD");
            Producto fromStore = store.get(id);
            if (fromStore != null) {
                cache.put(id, fromStore);
            }
            return fromStore;
        }

        CacheL2 getCache() {
            return cache;
        }
    }

    public static void main(String[] args) {

        EntityManager em = new EntityManager();
        em.persist(new Producto(1, "Monitor", 299.99));
        em.persist(new Producto(2, "Teclado", 49.99));
        em.persist(new Producto(3, "Ratón",   29.99));

        // Vaciamos caché para simular arranque frío
        EntityManager em2 = new EntityManager();
        em2.store.putAll(em.store);

        System.out.println("Consultas:");
        em2.findById(1);
        em2.findById(2);
        em2.findById(1);
        em2.findById(1);
        em2.findById(3);
        em2.findById(2);
        em2.findById(3);

        System.out.println();
        em2.getCache().printStats();
    }
}
