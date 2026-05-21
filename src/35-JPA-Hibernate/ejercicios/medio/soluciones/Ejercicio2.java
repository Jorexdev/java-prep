import java.util.HashMap;
import java.util.Map;

public class Ejercicio2 {

    static class OptimisticLockException extends RuntimeException {
        OptimisticLockException(String msg) {
            super(msg);
        }
    }

    // @Entity
    static class Cuenta {
        // @Id
        int id;
        double saldo;
        // @Version
        int version;

        Cuenta(int id, double saldo) {
            this.id = id;
            this.saldo = saldo;
            this.version = 0;
        }

        Cuenta copia() {
            return new Cuenta(id, saldo) {{ version = Cuenta.this.version; }};
        }

        @Override
        public String toString() {
            return "Cuenta{id=" + id + ", saldo=" + saldo + ", version=" + version + "}";
        }
    }

    static class EntityManager {
        private final Map<Integer, Cuenta> store = new HashMap<>();

        void persist(Cuenta c) {
            store.put(c.id, c.copia());
        }

        Cuenta find(int id) {
            Cuenta stored = store.get(id);
            if (stored == null) return null;
            return stored.copia();
        }

        void merge(Cuenta c) {
            Cuenta stored = store.get(c.id);
            if (stored == null) {
                throw new IllegalArgumentException("Entidad no encontrada: id=" + c.id);
            }
            if (stored.version != c.version) {
                throw new OptimisticLockException(
                    "Conflicto en Cuenta id=" + c.id +
                    ": versión esperada=" + c.version +
                    ", versión actual=" + stored.version
                );
            }
            c.version++;
            store.put(c.id, c.copia());
            System.out.println("Merge OK: " + c);
        }
    }

    public static void main(String[] args) {

        EntityManager em = new EntityManager();
        em.persist(new Cuenta(1, 1000.0));

        System.out.println("--- Transacción A (éxito) ---");
        Cuenta cuentaA = em.find(1);
        Cuenta cuentaB = em.find(1);

        cuentaA.saldo -= 200;
        em.merge(cuentaA);
        System.out.println("Estado en BD: " + em.find(1));

        System.out.println("\n--- Transacción B (fallo: versión obsoleta) ---");
        try {
            cuentaB.saldo -= 300;
            em.merge(cuentaB);
        } catch (OptimisticLockException e) {
            System.out.println("OptimisticLockException: " + e.getMessage());
        }

        System.out.println("\nEstado final en BD: " + em.find(1));
    }
}
