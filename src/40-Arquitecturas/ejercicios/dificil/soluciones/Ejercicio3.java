import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    // --- Snapshot ---

    static class Snapshot {
        final int version;
        final double saldo;
        final boolean bloqueada;

        Snapshot(int version, double saldo, boolean bloqueada) {
            this.version = version;
            this.saldo = saldo;
            this.bloqueada = bloqueada;
        }
    }

    static class SnapshotStore {
        private final Map<String, Snapshot> store = new HashMap<>();

        void save(String aggregateId, Snapshot snapshot) {
            store.put(aggregateId, snapshot);
        }

        Snapshot load(String aggregateId) {
            return store.get(aggregateId);
        }

        boolean has(String aggregateId) {
            return store.containsKey(aggregateId);
        }
    }

    // --- Event Store ---

    record StoredEvent(String aggregateId, int version, String type, double monto) {}

    static class EventStore {
        private final Map<String, List<StoredEvent>> store = new HashMap<>();

        void append(StoredEvent event) {
            store.computeIfAbsent(event.aggregateId(), k -> new ArrayList<>()).add(event);
        }

        List<StoredEvent> loadFrom(String aggregateId, int fromVersion) {
            return store.getOrDefault(aggregateId, List.of())
                .stream()
                .filter(e -> e.version() > fromVersion)
                .toList();
        }

        List<StoredEvent> loadAll(String aggregateId) {
            return store.getOrDefault(aggregateId, List.of());
        }
    }

    // --- Aggregate ---

    static class CuentaBancaria {
        String id;
        double saldo;
        boolean bloqueada;
        int version;

        private static final int SNAPSHOT_THRESHOLD = 5;

        static CuentaBancaria reconstituir(String aggregateId, EventStore eventStore,
                SnapshotStore snapshotStore) {
            CuentaBancaria cuenta = new CuentaBancaria();
            cuenta.id = aggregateId;

            int fromVersion = 0;

            if (snapshotStore.has(aggregateId)) {
                Snapshot snap = snapshotStore.load(aggregateId);
                cuenta.saldo = snap.saldo;
                cuenta.bloqueada = snap.bloqueada;
                cuenta.version = snap.version;
                fromVersion = snap.version;

                List<StoredEvent> pending = eventStore.loadFrom(aggregateId, fromVersion);
                System.out.println("Usando snapshot v" + snap.version
                    + ", replaying " + pending.size() + " eventos");

                for (StoredEvent e : pending) {
                    cuenta.apply(e);
                }
            } else {
                List<StoredEvent> all = eventStore.loadAll(aggregateId);
                for (StoredEvent e : all) {
                    cuenta.apply(e);
                }
            }

            return cuenta;
        }

        private void apply(StoredEvent e) {
            switch (e.type()) {
                case "CuentaAbierta" -> { saldo = 0.0; bloqueada = false; }
                case "Depositado"    -> saldo += e.monto();
                case "Retirado"      -> saldo -= e.monto();
                case "Bloqueada"     -> bloqueada = true;
            }
            version = e.version();
        }

        Snapshot toSnapshot() {
            return new Snapshot(version, saldo, bloqueada);
        }

        boolean shouldSnapshot() {
            return version > 0 && version % SNAPSHOT_THRESHOLD == 0;
        }

        @Override
        public String toString() {
            return "CuentaBancaria{saldo=" + saldo + ", bloqueada=" + bloqueada + ", version=" + version + "}";
        }
    }

    // --- Service ---

    static class CuentaService {
        private final EventStore eventStore;
        private final SnapshotStore snapshotStore;
        private final Map<String, Integer> versions = new HashMap<>();

        CuentaService(EventStore eventStore, SnapshotStore snapshotStore) {
            this.eventStore = eventStore;
            this.snapshotStore = snapshotStore;
        }

        private int nextVersion(String id) {
            return versions.merge(id, 1, Integer::sum);
        }

        private void appendAndSnapshot(String id, String type, double monto) {
            int v = nextVersion(id);
            StoredEvent e = new StoredEvent(id, v, type, monto);
            eventStore.append(e);

            CuentaBancaria temp = new CuentaBancaria();
            temp.id = id;
            eventStore.loadAll(id).forEach(temp::apply);

            if (temp.shouldSnapshot()) {
                snapshotStore.save(id, temp.toSnapshot());
                System.out.println("[SNAPSHOT] Guardado snapshot en v" + v + " para " + id);
            }
        }

        void abrir(String id)               { appendAndSnapshot(id, "CuentaAbierta", 0); }
        void depositar(String id, double m) { appendAndSnapshot(id, "Depositado", m); }
        void retirar(String id, double m)   { appendAndSnapshot(id, "Retirado", m); }
        void bloquear(String id)            { appendAndSnapshot(id, "Bloqueada", 0); }
    }

    public static void main(String[] args) {
        EventStore eventStore = new EventStore();
        SnapshotStore snapshotStore = new SnapshotStore();
        CuentaService service = new CuentaService(eventStore, snapshotStore);

        String id = "cuenta-1";

        System.out.println("--- Generando 13 eventos ---");
        service.abrir(id);
        service.depositar(id, 100);
        service.depositar(id, 200);
        service.depositar(id, 50);
        service.depositar(id, 75);   // v5 → snapshot
        service.retirar(id, 25);
        service.depositar(id, 300);
        service.depositar(id, 100);
        service.depositar(id, 50);
        service.depositar(id, 25);   // v10 → snapshot
        service.depositar(id, 10);
        service.depositar(id, 20);
        service.retirar(id, 5);      // v13

        System.out.println("\nTotal eventos en store: " + eventStore.loadAll(id).size());
        Snapshot snap = snapshotStore.load(id);
        System.out.println("Snapshot más reciente: v" + snap.version + ", saldo=" + snap.saldo);

        System.out.println("\n--- Reconstituyendo desde snapshot ---");
        CuentaBancaria cuenta = CuentaBancaria.reconstituir(id, eventStore, snapshotStore);
        System.out.println(cuenta);
    }
}
