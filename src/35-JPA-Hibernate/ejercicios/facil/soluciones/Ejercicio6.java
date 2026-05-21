import java.util.HashMap;
import java.util.Map;

public class Ejercicio6 {

    // @Entity
    static class Usuario {
        // @Id
        int id;
        String nombre;

        Usuario(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    static class EntityManager {
        private final Map<Integer, Usuario> managed = new HashMap<>();
        private final Map<Integer, String> snapshots = new HashMap<>();
        private int nextId = 1;

        void persist(Usuario u) {
            u.id = nextId++;
            managed.put(u.id, u);
            snapshots.put(u.id, u.nombre);
        }

        void flush() {
            System.out.println("--- flush() ---");
            for (Usuario u : managed.values()) {
                String original = snapshots.get(u.id);
                if (!u.nombre.equals(original)) {
                    System.out.println("UPDATE Usuario SET nombre='" + u.nombre + "' WHERE id=" + u.id);
                    snapshots.put(u.id, u.nombre);
                }
            }
            System.out.println("--- fin flush ---");
        }
    }

    public static void main(String[] args) {

        EntityManager em = new EntityManager();

        Usuario u1 = new Usuario(0, "Ana");
        Usuario u2 = new Usuario(0, "Luis");
        Usuario u3 = new Usuario(0, "Marta");

        em.persist(u1);
        em.persist(u2);
        em.persist(u3);

        u1.nombre = "Ana García";
        u3.nombre = "Marta López";

        em.flush();

        System.out.println("\nSegundo flush sin cambios:");
        em.flush();
    }
}
