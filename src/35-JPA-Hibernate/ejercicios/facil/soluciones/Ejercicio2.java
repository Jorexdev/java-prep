import java.util.HashMap;
import java.util.Map;

public class Ejercicio2 {

    // @Entity
    static class Producto {
        // @Id @GeneratedValue
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

    static class EntityManager {
        private final Map<Integer, Producto> store = new HashMap<>();
        private int nextId = 1;

        void persist(Producto p) {
            p.id = nextId++;
            store.put(p.id, new Producto(p.id, p.nombre, p.precio));
            System.out.println("INSERT: " + p);
        }

        Producto find(int id) {
            return store.get(id);
        }

        void merge(Producto p) {
            if (!store.containsKey(p.id)) {
                throw new IllegalArgumentException("Entidad no encontrada: id=" + p.id);
            }
            store.put(p.id, new Producto(p.id, p.nombre, p.precio));
            System.out.println("UPDATE: " + p);
        }

        void remove(int id) {
            Producto removed = store.remove(id);
            if (removed != null) {
                System.out.println("DELETE: " + removed);
            }
        }
    }

    public static void main(String[] args) {

        EntityManager em = new EntityManager();

        Producto p = new Producto(0, "Teclado", 49.99);
        em.persist(p);

        Producto encontrado = em.find(p.id);
        System.out.println("FIND:   " + encontrado);

        p.nombre = "Teclado Mecánico";
        p.precio = 89.99;
        em.merge(p);

        System.out.println("FIND tras merge: " + em.find(p.id));

        em.remove(p.id);
        System.out.println("FIND tras remove: " + em.find(p.id));
    }
}
