import java.util.HashMap;
import java.util.Map;

public class Ejercicio4 {

    // @Entity
    static class Cuenta {
        // @Id
        int id;
        double saldo;

        Cuenta(int id, double saldo) {
            this.id = id;
            this.saldo = saldo;
        }

        @Override
        public String toString() {
            return "Cuenta{id=" + id + ", saldo=" + saldo + "}";
        }
    }

    static class SaldoInsuficienteException extends RuntimeException {
        SaldoInsuficienteException(String msg) {
            super(msg);
        }
    }

    static class Transaction {
        private Map<Integer, Double> backup;
        private final Map<Integer, Cuenta> store;

        Transaction(Map<Integer, Cuenta> store) {
            this.store = store;
        }

        void begin() {
            backup = new HashMap<>();
            for (Map.Entry<Integer, Cuenta> e : store.entrySet()) {
                backup.put(e.getKey(), e.getValue().saldo);
            }
            System.out.println("  [TX] begin");
        }

        void commit() {
            backup = null;
            System.out.println("  [TX] commit");
        }

        void rollback() {
            if (backup != null) {
                for (Map.Entry<Integer, Double> e : backup.entrySet()) {
                    store.get(e.getKey()).saldo = e.getValue();
                }
                backup = null;
                System.out.println("  [TX] rollback");
            }
        }
    }

    static class ServicioBancario {
        private final Map<Integer, Cuenta> store = new HashMap<>();
        private final Transaction tx;

        ServicioBancario() {
            store.put(1, new Cuenta(1, 1000.0));
            store.put(2, new Cuenta(2, 500.0));
            tx = new Transaction(store);
        }

        void transferir(int origenId, int destinoId, double importe) {
            tx.begin();
            try {
                Cuenta origen  = store.get(origenId);
                Cuenta destino = store.get(destinoId);

                if (origen.saldo < importe) {
                    throw new SaldoInsuficienteException(
                        "Saldo insuficiente en cuenta " + origenId +
                        ": disponible=" + origen.saldo + ", solicitado=" + importe
                    );
                }
                origen.saldo  -= importe;
                destino.saldo += importe;
                tx.commit();
            } catch (SaldoInsuficienteException e) {
                System.out.println("  ERROR: " + e.getMessage());
                tx.rollback();
            }
        }

        void printEstado() {
            store.values().forEach(System.out::println);
        }
    }

    public static void main(String[] args) {

        ServicioBancario servicio = new ServicioBancario();

        System.out.println("=== Estado inicial ===");
        servicio.printEstado();

        System.out.println("\n=== Transferencia válida: 300 de cuenta 1 a cuenta 2 ===");
        servicio.transferir(1, 2, 300.0);
        servicio.printEstado();

        System.out.println("\n=== Transferencia inválida: 900 de cuenta 1 a cuenta 2 ===");
        servicio.transferir(1, 2, 900.0);
        servicio.printEstado();
    }
}
