import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    record StoredEvent(String aggregateId, int version, String type, Map<String, String> data) {}

    static class EventStore {
        private final Map<String, List<StoredEvent>> store = new HashMap<>();

        void append(StoredEvent event) {
            store.computeIfAbsent(event.aggregateId(), k -> new ArrayList<>()).add(event);
        }

        List<StoredEvent> load(String aggregateId) {
            return store.getOrDefault(aggregateId, List.of());
        }
    }

    static class CuentaBancaria {
        String id;
        String titular;
        double saldo;
        boolean bloqueada;
        int version;

        private CuentaBancaria() {}

        static CuentaBancaria reconstituir(List<StoredEvent> events) {
            return reconstituir(events, Integer.MAX_VALUE);
        }

        static CuentaBancaria reconstituir(List<StoredEvent> events, int upToVersion) {
            CuentaBancaria cuenta = new CuentaBancaria();
            for (StoredEvent e : events) {
                if (e.version() > upToVersion) break;
                cuenta.apply(e);
            }
            return cuenta;
        }

        private void apply(StoredEvent e) {
            switch (e.type()) {
                case "CuentaAbierta" -> {
                    this.id = e.aggregateId();
                    this.titular = e.data().get("titular");
                    this.saldo = 0.0;
                    this.bloqueada = false;
                }
                case "Depositado" -> this.saldo += Double.parseDouble(e.data().get("monto"));
                case "Retirado"   -> this.saldo -= Double.parseDouble(e.data().get("monto"));
                case "Bloqueada"  -> this.bloqueada = true;
            }
            this.version = e.version();
        }

        @Override
        public String toString() {
            return "CuentaBancaria{id='" + id + "', titular='" + titular
                + "', saldo=" + saldo + ", bloqueada=" + bloqueada + ", version=" + version + "}";
        }
    }

    static class CuentaService {
        private final EventStore store;
        private final Map<String, Integer> versions = new HashMap<>();

        CuentaService(EventStore store) {
            this.store = store;
        }

        private int nextVersion(String id) {
            return versions.merge(id, 1, Integer::sum);
        }

        void abrir(String id, String titular) {
            store.append(new StoredEvent(id, nextVersion(id), "CuentaAbierta",
                Map.of("titular", titular)));
        }

        void depositar(String id, double monto) {
            store.append(new StoredEvent(id, nextVersion(id), "Depositado",
                Map.of("monto", String.valueOf(monto))));
        }

        void retirar(String id, double monto) {
            store.append(new StoredEvent(id, nextVersion(id), "Retirado",
                Map.of("monto", String.valueOf(monto))));
        }

        void bloquear(String id) {
            store.append(new StoredEvent(id, nextVersion(id), "Bloqueada", Map.of()));
        }
    }

    public static void main(String[] args) {
        EventStore eventStore = new EventStore();
        CuentaService service = new CuentaService(eventStore);

        String cuentaId = "cuenta-1";

        service.abrir(cuentaId, "Ana");
        service.depositar(cuentaId, 500.0);
        service.depositar(cuentaId, 200.0);
        service.depositar(cuentaId, 100.0);
        service.retirar(cuentaId, 150.0);
        service.bloquear(cuentaId);

        List<StoredEvent> events = eventStore.load(cuentaId);
        System.out.println("Total eventos: " + events.size());

        System.out.println("\n--- Estado actual (todos los eventos) ---");
        CuentaBancaria estadoActual = CuentaBancaria.reconstituir(events);
        System.out.println(estadoActual);

        System.out.println("\n--- Temporal query: estado en versión 2 (tras primer depósito) ---");
        CuentaBancaria v2 = CuentaBancaria.reconstituir(events, 2);
        System.out.println(v2);

        System.out.println("\n--- Temporal query: estado en versión 4 (tras tercer depósito) ---");
        CuentaBancaria v4 = CuentaBancaria.reconstituir(events, 4);
        System.out.println(v4);
    }
}
