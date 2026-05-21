import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    static class EventRecord {
        final String aggregateId;
        final String eventType;
        final Map<String, String> data;

        EventRecord(String aggregateId, String eventType, Map<String, String> data) {
            this.aggregateId = aggregateId;
            this.eventType = eventType;
            this.data = data;
        }
    }

    static class EventLog {
        private final List<EventRecord> log = new ArrayList<>();

        void append(String aggregateId, String eventType, Map<String, String> data) {
            log.add(new EventRecord(aggregateId, eventType, data));
        }

        List<EventRecord> getAll() {
            return log;
        }
    }

    static class UsuarioProjection {
        private final Map<String, Map<String, String>> state = new HashMap<>();

        void consume(EventLog log) {
            for (EventRecord r : log.getAll()) {
                state.computeIfAbsent(r.aggregateId, k -> new HashMap<>());
                Map<String, String> user = state.get(r.aggregateId);
                switch (r.eventType) {
                    case "UsuarioCreado" -> {
                        user.put("nombre", r.data.get("nombre"));
                        user.put("email", r.data.get("email"));
                        user.put("plan", "FREE");
                        user.put("bloqueado", "false");
                    }
                    case "EmailCambiado" -> user.put("email", r.data.get("nuevoEmail"));
                    case "PlanUpgraded" -> user.put("plan", r.data.get("plan"));
                    case "CuentaBloqueada" -> user.put("bloqueado", "true");
                }
            }
        }

        Map<String, Map<String, String>> getState() {
            return state;
        }
    }

    static class HistorialProjection {
        private final Map<String, List<String>> historial = new HashMap<>();

        void consume(EventLog log) {
            for (EventRecord r : log.getAll()) {
                historial.computeIfAbsent(r.aggregateId, k -> new ArrayList<>())
                        .add(r.eventType + " " + r.data);
            }
        }

        Map<String, List<String>> getHistorial() {
            return historial;
        }
    }

    public static void main(String[] args) {
        EventLog log = new EventLog();

        log.append("U1", "UsuarioCreado", Map.of("nombre", "Ana", "email", "ana@mail.com"));
        log.append("U2", "UsuarioCreado", Map.of("nombre", "Luis", "email", "luis@mail.com"));
        log.append("U3", "UsuarioCreado", Map.of("nombre", "Marta", "email", "marta@mail.com"));
        log.append("U1", "PlanUpgraded", Map.of("plan", "PRO"));
        log.append("U2", "EmailCambiado", Map.of("nuevoEmail", "luis2@mail.com"));
        log.append("U3", "CuentaBloqueada", Map.of());
        log.append("U1", "EmailCambiado", Map.of("nuevoEmail", "ana-pro@mail.com"));
        log.append("U2", "PlanUpgraded", Map.of("plan", "ENTERPRISE"));
        log.append("U3", "EmailCambiado", Map.of("nuevoEmail", "marta-nueva@mail.com"));
        log.append("U1", "PlanUpgraded", Map.of("plan", "ENTERPRISE"));

        UsuarioProjection projection = new UsuarioProjection();
        projection.consume(log);

        HistorialProjection historial = new HistorialProjection();
        historial.consume(log);

        System.out.println("[ESTADO ACTUAL]");
        for (Map.Entry<String, Map<String, String>> entry : projection.getState().entrySet()) {
            System.out.println("  " + entry.getKey() + " → " + entry.getValue());
        }

        System.out.println("\n[HISTORIAL DE CAMBIOS]");
        for (Map.Entry<String, List<String>> entry : historial.getHistorial().entrySet()) {
            System.out.println("  " + entry.getKey() + ":");
            for (String event : entry.getValue()) {
                System.out.println("    - " + event);
            }
        }
    }
}
