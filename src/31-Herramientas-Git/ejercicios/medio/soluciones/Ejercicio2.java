import java.util.LinkedHashMap;
import java.util.Map;

public class Ejercicio2 {

    // Representa una línea que puede tener conflicto
    static class Line {
        final String key;
        boolean hasConflict;
        String headValue;
        String featureValue;
        String resolved; // null si no resuelto

        Line(String key, String headValue, String featureValue) {
            this.key = key;
            this.hasConflict = true;
            this.headValue = headValue;
            this.featureValue = featureValue;
            this.resolved = null;
        }

        Line(String key, String value) {
            this.key = key;
            this.hasConflict = false;
            this.resolved = value;
        }

        String display() {
            if (!hasConflict) return key + " = " + resolved;
            if (resolved != null)  return key + " = " + resolved + "  [resuelto]";
            return key + ":\n"
                + "  <<<< HEAD\n"
                + "  " + headValue + "\n"
                + "  ====\n"
                + "  " + featureValue + "\n"
                + "  >>>> feature";
        }
    }

    static class ConflictResolver {
        private final Map<String, Line> lines;

        ConflictResolver(Map<String, Line> lines) {
            this.lines = lines;
        }

        // Acepta la versión HEAD para todas las líneas conflictivas
        void ours() {
            lines.values().stream()
                .filter(l -> l.hasConflict && l.resolved == null)
                .forEach(l -> {
                    l.resolved = l.headValue;
                    System.out.println("Resolviendo '" + l.key + "' -> ours: " + l.headValue);
                });
        }

        // Acepta la versión feature para todas las líneas conflictivas
        void theirs() {
            lines.values().stream()
                .filter(l -> l.hasConflict && l.resolved == null)
                .forEach(l -> {
                    l.resolved = l.featureValue;
                    System.out.println("Resolviendo '" + l.key + "' -> theirs: " + l.featureValue);
                });
        }

        // Resuelve un conflicto específico con valor personalizado
        void resolve(String key, String value) {
            Line line = lines.get(key);
            if (line == null || !line.hasConflict) throw new IllegalArgumentException("No hay conflicto en: " + key);
            line.resolved = value;
            System.out.println("Resolviendo '" + key + "' -> custom: " + value);
        }

        boolean hasUnresolved() {
            return lines.values().stream().anyMatch(l -> l.hasConflict && l.resolved == null);
        }

        void printState(String title) {
            System.out.println("=== " + title + " ===");
            lines.values().forEach(l -> System.out.println(l.display()));
            System.out.println();
        }
    }

    public static void main(String[] args) {
        // Simular resultado de un 3-way merge con conflictos
        Map<String, Line> mergeResult = new LinkedHashMap<>();
        mergeResult.put("server.port",    new Line("server.port", "8080"));         // sin conflicto
        mergeResult.put("db.url",         new Line("db.url", "jdbc:main/db", "jdbc:feature/db")); // conflicto
        mergeResult.put("app.name",       new Line("app.name", "MyApp-main", "MyApp-feature")); // conflicto
        mergeResult.put("log.level",      new Line("log.level", "INFO"));            // sin conflicto
        mergeResult.put("cache.enabled",  new Line("cache.enabled", "true", "false")); // conflicto

        ConflictResolver resolver = new ConflictResolver(mergeResult);

        resolver.printState("Estado inicial con conflictos");

        System.out.println("Conflictos sin resolver: " + resolver.hasUnresolved());
        System.out.println();

        // Resolver 'db.url' con valor personalizado
        System.out.println("=== Resolución manual de 'db.url' ===");
        resolver.resolve("db.url", "jdbc:prod/db");
        System.out.println();

        // Resolver 'app.name' con la versión HEAD (ours)
        System.out.println("=== Resolviendo 'app.name' con ours() ===");
        resolver.ours(); // solo afecta al no resuelto
        System.out.println();

        resolver.printState("Estado después de resoluciones parciales");

        System.out.println("Conflictos sin resolver: " + resolver.hasUnresolved());
        System.out.println();

        // Estado final
        resolver.printState("Estado final — todos resueltos");
    }
}
