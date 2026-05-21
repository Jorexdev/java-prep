import java.util.HashMap;
import java.util.Map;

public class Ejercicio1 {

    // @Entity
    static class Usuario {
        // @Id
        int id;
        String nombre;
        String estado;

        Usuario(String nombre) {
            this.id = 0;
            this.nombre = nombre;
            this.estado = "TRANSIENT";
        }

        @Override
        public String toString() {
            return "Usuario{id=" + id + ", nombre='" + nombre + "', estado=" + estado + "}";
        }
    }

    static class EntityManager {
        private final Map<Integer, Usuario> store = new HashMap<>();
        private int nextId = 1;

        void persist(Usuario u) {
            u.id = nextId++;
            u.estado = "MANAGED";
            store.put(u.id, u);
        }

        void detach(Usuario u) {
            u.estado = "DETACHED";
            store.remove(u.id);
        }

        void remove(Usuario u) {
            u.estado = "REMOVED";
            store.remove(u.id);
        }
    }

    public static void main(String[] args) {

        EntityManager em = new EntityManager();

        Usuario u = new Usuario("Ana");
        System.out.println("Después de new:     " + u);

        em.persist(u);
        System.out.println("Después de persist: " + u);

        em.detach(u);
        System.out.println("Después de detach:  " + u);

        em.persist(u);
        System.out.println("Re-persist:         " + u);

        em.remove(u);
        System.out.println("Después de remove:  " + u);
    }
}
