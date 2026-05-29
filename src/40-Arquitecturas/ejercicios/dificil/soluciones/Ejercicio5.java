import java.util.*;

public class Ejercicio5 {

    // --- StoredEvent ---

    record StoredEvent(String aggregateId, int version, String type, Map<String, String> data) {
        @Override
        public String toString() {
            return "v" + version + " [" + type + "] " + data;
        }
    }

    // --- EventStore append-only ---

    static class EventStore {
        private final Map<String, List<StoredEvent>> store = new HashMap<>();

        void append(StoredEvent event) {
            store.computeIfAbsent(event.aggregateId(), k -> new ArrayList<>()).add(event);
        }

        List<StoredEvent> load(String aggregateId) {
            return Collections.unmodifiableList(
                store.getOrDefault(aggregateId, List.of()));
        }

        List<StoredEvent> loadFrom(String aggregateId, int fromVersion) {
            return store.getOrDefault(aggregateId, List.of()).stream()
                .filter(e -> e.version() > fromVersion)
                .toList();
        }
    }

    // --- Snapshot ---

    record Snapshot(String aggregateId, int version, double saldo, boolean bloqueada) {}

    // --- SnapshotStore ---

    static class SnapshotStore {
        private final Map<String, Snapshot> store = new HashMap<>();
        private final int snapshotInterval;

        SnapshotStore(int snapshotInterval) { this.snapshotInterval = snapshotInterval; }

        void save(Snapshot snapshot) {
            store.put(snapshot.aggregateId(), snapshot);
            System.out.println("  [SnapshotStore] Snapshot guardado en v" + snapshot.version()
                + " — saldo=" + snapshot.saldo());
        }

        Optional<Snapshot> load(String aggregateId) {
            return Optional.ofNullable(store.get(aggregateId));
        }

        boolean shouldTakeSnapshot(int version) {
            return version % snapshotInterval == 0;
        }
    }

    // --- Agregado CuentaBancaria ---

    static class CuentaBancaria {
        String id;
        String titular;
        double saldo;
        boolean bloqueada;
        int version;

        // Reconstituye desde una lista de eventos (con snapshot opcional)
        static CuentaBancaria reconstituir(String aggregateId,
                                           EventStore eventStore,
                                           SnapshotStore snapshotStore) {
            CuentaBancaria cuenta = new CuentaBancaria();
            cuenta.id = aggregateId;

            Optional<Snapshot> snap = snapshotStore.load(aggregateId);
            List<StoredEvent> events;

            if (snap.isPresent()) {
                Snapshot s = snap.get();
                cuenta.saldo     = s.saldo();
                cuenta.bloqueada = s.bloqueada();
                cuenta.version   = s.version();
                events = eventStore.loadFrom(aggregateId, s.version());
                System.out.println("  [Reconstituir] Usando snapshot v" + s.version()
                    + ", replaying " + events.size() + " evento(s)");
            } else {
                events = eventStore.load(aggregateId);
                System.out.println("  [Reconstituir] Sin snapshot, replaying " + events.size() + " evento(s)");
            }

            for (StoredEvent e : events) {
                cuenta.apply(e);
            }
            return cuenta;
        }

        // Reconstituye sin snapshot (comparación)
        static CuentaBancaria reconstituirSinSnapshot(String aggregateId, EventStore eventStore) {
            CuentaBancaria cuenta = new CuentaBancaria();
            cuenta.id = aggregateId;
            for (StoredEvent e : eventStore.load(aggregateId)) {
                cuenta.apply(e);
            }
            return cuenta;
        }

        void apply(StoredEvent e) {
            version = e.version();
            switch (e.type()) {
                case "CuentaAbierta" -> {
                    titular = e.data().get("titular");
                    saldo   = Double.parseDouble(e.data().get("saldoInicial"));
                }
                case "Depositado" -> saldo += Double.parseDouble(e.data().get("importe"));
                case "Retirado"   -> saldo -= Double.parseDouble(e.data().get("importe"));
                case "Bloqueada"  -> bloqueada = true;
            }
        }

        Snapshot toSnapshot() {
            return new Snapshot(id, version, saldo, bloqueada);
        }

        @Override
        public String toString() {
            return "CuentaBancaria{id=" + id + ", titular=" + titular
                + ", saldo=" + saldo + ", bloqueada=" + bloqueada + ", version=" + version + "}";
        }
    }

    // --- Servicio de aplicación ---

    static class CuentaService {
        private final EventStore    eventStore;
        private final SnapshotStore snapshotStore;

        CuentaService(EventStore eventStore, SnapshotStore snapshotStore) {
            this.eventStore    = eventStore;
            this.snapshotStore = snapshotStore;
        }

        private int nextVersion(String id) {
            return eventStore.load(id).size() + 1;
        }

        private void saveEvent(String id, String type, Map<String, String> data) {
            int version = nextVersion(id);
            StoredEvent event = new StoredEvent(id, version, type, data);
            eventStore.append(event);
            System.out.println("  [EventStore] " + event);

            // Snapshot cada N eventos
            if (snapshotStore.shouldTakeSnapshot(version)) {
                CuentaBancaria cuenta = CuentaBancaria.reconstituirSinSnapshot(id, eventStore);
                snapshotStore.save(cuenta.toSnapshot());
            }
        }

        void abrirCuenta(String id, String titular, double saldoInicial) {
            saveEvent(id, "CuentaAbierta",
                Map.of("titular", titular, "saldoInicial", String.valueOf(saldoInicial)));
        }

        void depositar(String id, double importe) {
            saveEvent(id, "Depositado", Map.of("importe", String.valueOf(importe)));
        }

        void retirar(String id, double importe) {
            saveEvent(id, "Retirado", Map.of("importe", String.valueOf(importe)));
        }

        void bloquear(String id) {
            saveEvent(id, "Bloqueada", Map.of("motivo", "fraude"));
        }
    }

    public static void main(String[] args) {
        EventStore    eventStore    = new EventStore();
        SnapshotStore snapshotStore = new SnapshotStore(5); // snapshot cada 5 eventos
        CuentaService service       = new CuentaService(eventStore, snapshotStore);

        String id = "CTA-42";

        System.out.println("=== Aplicando 12 eventos ===");
        service.abrirCuenta(id, "María López", 1_000.0);  // v1
        service.depositar  (id, 200.0);                   // v2
        service.depositar  (id, 150.0);                   // v3
        service.retirar    (id, 100.0);                   // v4
        service.depositar  (id, 500.0);                   // v5 → SNAPSHOT
        service.retirar    (id, 250.0);                   // v6
        service.depositar  (id, 300.0);                   // v7
        service.depositar  (id, 50.0);                    // v8
        service.retirar    (id, 400.0);                   // v9
        service.depositar  (id, 100.0);                   // v10 → SNAPSHOT
        service.retirar    (id, 200.0);                   // v11
        service.bloquear   (id);                          // v12

        System.out.println("\n=== Reconstitución CON snapshot ===");
        CuentaBancaria conSnap = CuentaBancaria.reconstituir(id, eventStore, snapshotStore);
        System.out.println("Estado: " + conSnap);

        System.out.println("\n=== Reconstitución SIN snapshot (replay completo) ===");
        CuentaBancaria sinSnap = CuentaBancaria.reconstituirSinSnapshot(id, eventStore);
        System.out.println("  [Reconstituir] Sin snapshot, replaying 12 evento(s)");
        System.out.println("Estado: " + sinSnap);

        System.out.println("\n=== Verificación ===");
        System.out.printf("%s  saldo: con_snap=%.1f sin_snap=%.1f%n",
            conSnap.saldo == sinSnap.saldo ? "PASS" : "FAIL", conSnap.saldo, sinSnap.saldo);
        System.out.printf("%s  bloqueada: con_snap=%b sin_snap=%b%n",
            conSnap.bloqueada == sinSnap.bloqueada ? "PASS" : "FAIL",
            conSnap.bloqueada, sinSnap.bloqueada);
        System.out.printf("%s  version: con_snap=%d sin_snap=%d%n",
            conSnap.version == sinSnap.version ? "PASS" : "FAIL",
            conSnap.version, sinSnap.version);

        // Saldo esperado: 1000+200+150-100+500-250+300+50-400+100-200 = 1350
        double esperado = 1_000 + 200 + 150 - 100 + 500 - 250 + 300 + 50 - 400 + 100 - 200;
        System.out.printf("%s  saldo esperado=%.1f actual=%.1f%n",
            conSnap.saldo == esperado ? "PASS" : "FAIL", esperado, conSnap.saldo);
    }
}
