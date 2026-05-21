import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio5 {

    // @Entity
    static class Empleado {
        // @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        int id;
        String nombre;

        Empleado(String nombre) {
            this.id = 0;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return "Empleado{id=" + id + ", nombre='" + nombre + "'}";
        }
    }

    static class AutoIncrementIdGenerator {
        private final AtomicInteger counter = new AtomicInteger(1);

        int next() {
            return counter.getAndIncrement();
        }
    }

    static class EntityManager {
        private final Map<Integer, Empleado> store = new HashMap<>();
        private final AutoIncrementIdGenerator generator = new AutoIncrementIdGenerator();

        void persist(Empleado e) {
            e.id = generator.next();
            store.put(e.id, e);
            System.out.println("Persisted: " + e);
        }

        Empleado find(int id) {
            return store.get(id);
        }
    }

    public static void main(String[] args) {

        EntityManager em = new EntityManager();

        Empleado e1 = new Empleado("Lucía");
        Empleado e2 = new Empleado("Marcos");
        Empleado e3 = new Empleado("Elena");

        System.out.println("Antes de persist: " + e1);
        System.out.println("Antes de persist: " + e2);
        System.out.println("Antes de persist: " + e3);
        System.out.println();

        em.persist(e1);
        em.persist(e2);
        em.persist(e3);

        System.out.println();
        System.out.println("IDs asignados: " + e1.id + ", " + e2.id + ", " + e3.id);
        System.out.println("IDs consecutivos: " + (e2.id == e1.id + 1 && e3.id == e2.id + 1));
    }
}
