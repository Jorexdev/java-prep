import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    // @Entity
    static class Usuario {
        // @Id
        int id;
        String nombre;

        Usuario(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        String snapshot() {
            return nombre;
        }

        @Override
        public String toString() {
            return "Usuario{id=" + id + ", nombre='" + nombre + "'}";
        }
    }

    static class UnitOfWork {
        private final Map<Integer, Usuario> identityMap  = new HashMap<>();
        private final Map<Integer, String>  snapshots    = new HashMap<>();
        private final List<Usuario>         newObjects   = new ArrayList<>();
        private final List<Usuario>         deletedQueue = new ArrayList<>();
        private int nextId = 1;

        Usuario getById(int id) {
            if (identityMap.containsKey(id)) {
                System.out.println("  [identity map] devolviendo instancia en memoria: id=" + id);
                return identityMap.get(id);
            }
            System.out.println("  [BD] SELECT * FROM usuario WHERE id=" + id);
            return null;
        }

        void registerNew(Usuario u) {
            u.id = nextId++;
            identityMap.put(u.id, u);
            snapshots.put(u.id, u.snapshot());
            newObjects.add(u);
        }

        void registerDeleted(Usuario u) {
            deletedQueue.add(u);
            identityMap.remove(u.id);
        }

        void commit() {
            System.out.println("\n--- UnitOfWork.commit() ---");

            for (Usuario u : newObjects) {
                System.out.println("  INSERT INTO usuario VALUES (" + u.id + ", '" + u.nombre + "')");
            }
            newObjects.clear();

            for (Usuario u : identityMap.values()) {
                String original = snapshots.get(u.id);
                if (original != null && !u.snapshot().equals(original)) {
                    System.out.println("  UPDATE usuario SET nombre='" + u.nombre + "' WHERE id=" + u.id);
                    snapshots.put(u.id, u.snapshot());
                }
            }

            for (Usuario u : deletedQueue) {
                System.out.println("  DELETE FROM usuario WHERE id=" + u.id);
            }
            deletedQueue.clear();

            System.out.println("--- fin commit ---");
        }
    }

    public static void main(String[] args) {

        UnitOfWork uow = new UnitOfWork();

        Usuario u1 = new Usuario(0, "Ana");
        Usuario u2 = new Usuario(0, "Luis");
        Usuario u3 = new Usuario(0, "Marta");

        uow.registerNew(u1);
        uow.registerNew(u2);
        uow.registerNew(u3);

        System.out.println("=== Identity map: doble acceso al mismo id ===");
        Usuario ref1 = uow.getById(u1.id);
        Usuario ref2 = uow.getById(u1.id);
        System.out.println("Misma instancia: " + (ref1 == ref2));

        System.out.println("\n=== Dirty tracking ===");
        u1.nombre = "Ana García";
        uow.registerDeleted(u3);

        uow.commit();
    }
}
