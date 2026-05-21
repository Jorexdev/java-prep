import java.util.*;

public class Ejercicio4 {

    enum TxState { NONE, PREPARED, COMMITTED, ROLLED_BACK }

    static class ResourceManager {
        final String name;
        private final boolean failOnPrepare;
        private final boolean failOnCommit;
        private final Map<String, TxState> transactions = new HashMap<>();

        ResourceManager(String name, boolean failOnPrepare, boolean failOnCommit) {
            this.name = name;
            this.failOnPrepare = failOnPrepare;
            this.failOnCommit = failOnCommit;
        }

        boolean prepare(String txId) {
            if (failOnPrepare) {
                System.out.println("  [" + name + "] prepare(" + txId + ") → NO (fallo simulado)");
                return false;
            }
            transactions.put(txId, TxState.PREPARED);
            System.out.println("  [" + name + "] prepare(" + txId + ") → YES");
            return true;
        }

        void commit(String txId) {
            if (failOnCommit) {
                System.out.println("  [" + name + "] commit(" + txId + ") → FALLO (necesita recovery)");
                return;
            }
            transactions.put(txId, TxState.COMMITTED);
            System.out.println("  [" + name + "] commit(" + txId + ") → OK");
        }

        void rollback(String txId) {
            transactions.put(txId, TxState.ROLLED_BACK);
            System.out.println("  [" + name + "] rollback(" + txId + ") → OK");
        }

        TxState getState(String txId) {
            return transactions.getOrDefault(txId, TxState.NONE);
        }
    }

    static class TransactionCoordinator {
        private final List<ResourceManager> participants;

        TransactionCoordinator(List<ResourceManager> participants) {
            this.participants = participants;
        }

        String execute(String txId) {
            System.out.println("--- Fase 1: PREPARE ---");
            List<ResourceManager> prepared = new ArrayList<>();
            boolean allYes = true;

            for (ResourceManager rm : participants) {
                if (rm.prepare(txId)) {
                    prepared.add(rm);
                } else {
                    allYes = false;
                    break;
                }
            }

            if (!allYes) {
                System.out.println("--- Resultado: ABORT (rollback de los que prepararon) ---");
                for (ResourceManager rm : prepared) {
                    rm.rollback(txId);
                }
                return "ABORTED";
            }

            System.out.println("--- Fase 2: COMMIT ---");
            boolean commitFailed = false;
            for (ResourceManager rm : participants) {
                rm.commit(txId);
                if (rm.getState(txId) != TxState.COMMITTED) {
                    commitFailed = true;
                }
            }

            if (commitFailed) {
                System.out.println("--- Resultado: COMMIT PARCIAL (se requiere recovery) ---");
                return "PARTIAL_COMMIT";
            }

            System.out.println("--- Resultado: COMMITTED ---");
            return "COMMITTED";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Escenario 1: todos confirman (éxito) ===");
        {
            List<ResourceManager> rms = List.of(
                new ResourceManager("DB-Pedidos", false, false),
                new ResourceManager("DB-Inventario", false, false),
                new ResourceManager("DB-Pagos", false, false)
            );
            String result = new TransactionCoordinator(new ArrayList<>(rms)).execute("TX-001");
            System.out.println("Resultado final: " + result);
        }

        System.out.println("\n=== Escenario 2: uno falla en prepare (abort) ===");
        {
            List<ResourceManager> rms = new ArrayList<>();
            rms.add(new ResourceManager("DB-Pedidos", false, false));
            rms.add(new ResourceManager("DB-Inventario", true, false));
            rms.add(new ResourceManager("DB-Pagos", false, false));
            String result = new TransactionCoordinator(rms).execute("TX-002");
            System.out.println("Resultado final: " + result);
        }

        System.out.println("\n=== Escenario 3: todos preparan pero uno falla en commit (recovery) ===");
        {
            List<ResourceManager> rms = new ArrayList<>();
            rms.add(new ResourceManager("DB-Pedidos", false, false));
            rms.add(new ResourceManager("DB-Inventario", false, true));
            rms.add(new ResourceManager("DB-Pagos", false, false));
            String result = new TransactionCoordinator(rms).execute("TX-003");
            System.out.println("Resultado final: " + result);
        }
    }
}
