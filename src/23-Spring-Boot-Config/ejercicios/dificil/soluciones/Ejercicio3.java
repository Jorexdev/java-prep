import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

// Ejercicio 3 (Difícil) — Config watcher
// Compara dos snapshots y emite PropertyChangedEvent para cada diferencia
public class Ejercicio3 {

    record PropertyChangedEvent(
            String key,
            String oldValue,   // null si la clave fue añadida
            String newValue    // null si la clave fue eliminada
    ) {
        String type() {
            if (oldValue == null) return "ADDED";
            if (newValue == null) return "REMOVED";
            return "CHANGED";
        }

        @Override
        public String toString() {
            return switch (type()) {
                case "ADDED"   -> "[ADDED]   " + key + " = '" + newValue + "'";
                case "REMOVED" -> "[REMOVED] " + key + " (era '" + oldValue + "')";
                default        -> "[CHANGED] " + key + ": '" + oldValue + "' → '" + newValue + "'";
            };
        }
    }

    static class ConfigWatcher {
        private final List<Consumer<PropertyChangedEvent>> listeners = new ArrayList<>();

        public void onChange(Consumer<PropertyChangedEvent> listener) {
            listeners.add(listener);
        }

        /**
         * Compara el snapshot anterior con el nuevo.
         * Emite un evento por cada propiedad que haya cambiado, añadido o eliminado.
         */
        public List<PropertyChangedEvent> diff(Map<String, String> before, Map<String, String> after) {
            List<PropertyChangedEvent> events = new ArrayList<>();

            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(before.keySet());
            allKeys.addAll(after.keySet());

            for (String key : allKeys) {
                String oldVal = before.get(key);
                String newVal = after.get(key);

                if (!Objects.equals(oldVal, newVal)) {
                    PropertyChangedEvent event = new PropertyChangedEvent(key, oldVal, newVal);
                    events.add(event);
                    listeners.forEach(l -> l.accept(event));
                }
            }

            return events;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Config watcher ===");
        System.out.println();

        // Snapshot 1 — estado actual de las propiedades
        Map<String, String> snapshot1 = new HashMap<>();
        snapshot1.put("server.port", "8080");
        snapshot1.put("log.level", "DEBUG");
        snapshot1.put("db.url", "jdbc:h2:mem:testdb");
        snapshot1.put("cache.enabled", "false");
        snapshot1.put("feature.experimental", "false");

        // Snapshot 2 — después de una actualización en el Config Server
        Map<String, String> snapshot2 = new HashMap<>();
        snapshot2.put("server.port", "9090");              // CHANGED
        snapshot2.put("log.level", "DEBUG");               // sin cambio
        snapshot2.put("db.url", "jdbc:postgresql://prod/db"); // CHANGED
        snapshot2.put("cache.enabled", "true");            // CHANGED
        // feature.experimental fue REMOVED
        snapshot2.put("app.maintenance", "false");         // ADDED

        System.out.println("Snapshot 1 (antes):");
        snapshot1.forEach((k, v) -> System.out.println("  " + k + " = " + v));
        System.out.println();
        System.out.println("Snapshot 2 (después):");
        snapshot2.forEach((k, v) -> System.out.println("  " + k + " = " + v));
        System.out.println();

        ConfigWatcher watcher = new ConfigWatcher();
        List<String> auditLog = new ArrayList<>();

        watcher.onChange(event -> {
            auditLog.add(event.toString());
        });

        System.out.println("--- Ejecutando diff ---");
        List<PropertyChangedEvent> events = watcher.diff(snapshot1, snapshot2);
        System.out.println("Eventos detectados: " + events.size());
        System.out.println();

        System.out.println("--- Eventos emitidos ---");
        events.stream()
              .sorted((a, b) -> a.key().compareTo(b.key()))
              .forEach(System.out::println);

        System.out.println();
        System.out.println("--- Audit log (capturado por listener) ---");
        auditLog.forEach(System.out::println);

        System.out.println();
        System.out.println("--- Resumen ---");
        long added   = events.stream().filter(e -> e.type().equals("ADDED")).count();
        long removed = events.stream().filter(e -> e.type().equals("REMOVED")).count();
        long changed = events.stream().filter(e -> e.type().equals("CHANGED")).count();
        System.out.println("  ADDED:   " + added);
        System.out.println("  REMOVED: " + removed);
        System.out.println("  CHANGED: " + changed);
        System.out.println("  TOTAL:   " + events.size());
    }
}
