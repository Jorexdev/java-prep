import java.util.*;

public class Ejercicio6 {

    // Representa una entrada de versión con su origen y prioridad
    static class VersionEntry {
        String version;
        String source;   // "POM directo", "BOM-A", "BOM-B", etc.
        int priority;    // menor = más alta prioridad (1 = POM directo)

        VersionEntry(String version, String source, int priority) {
            this.version  = version;
            this.source   = source;
            this.priority = priority;
        }
    }

    static class BomManager {
        // dep → lista ordenada de candidatos (POM directo primero, luego BOMs en orden)
        private final Map<String, List<VersionEntry>> candidates = new LinkedHashMap<>();

        // Registra versiones del POM directo (máxima prioridad)
        void addPomDirect(String dep, String version) {
            candidates.computeIfAbsent(dep, k -> new ArrayList<>())
                    .add(new VersionEntry(version, "POM directo", 1));
        }

        // Registra versiones de un BOM (prioridad según orden de importación)
        void addBom(String bomName, int bomOrder, Map<String, String> versions) {
            versions.forEach((dep, version) ->
                candidates.computeIfAbsent(dep, k -> new ArrayList<>())
                        .add(new VersionEntry(version, bomName, 10 + bomOrder)));
        }

        // Resuelve la versión ganadora para cada dependencia (nearest wins = menor prioridad)
        void resolve(List<String> dependencies) {
            System.out.println("=== Resolución de versiones (nearest wins) ===\n");

            for (String dep : dependencies) {
                List<VersionEntry> entries = candidates.getOrDefault(dep, List.of());

                if (entries.isEmpty()) {
                    System.out.printf("  %-20s → SIN VERSIÓN (error: debe declararse en BOM o POM)%n", dep);
                    continue;
                }

                // La entrada con menor priority gana
                VersionEntry winner = entries.stream()
                        .min(Comparator.comparingInt(e -> e.priority))
                        .orElseThrow();

                System.out.printf("  %-20s → %-10s (ganador: %s)%n",
                        dep, winner.version, winner.source);

                // Mostrar candidatos descartados
                entries.stream()
                        .filter(e -> e != winner)
                        .forEach(e -> System.out.printf("    ↳ omitido: %-8s de %s%n",
                                e.version, e.source));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Maven BOM con nearest-wins ===\n");

        // BOM-A: importado primero (orden 1)
        Map<String, String> bomA = Map.of(
                "jackson",    "2.15.0",
                "slf4j",      "2.0.7"
        );

        // BOM-B: importado segundo (orden 2, menor prioridad que BOM-A)
        Map<String, String> bomB = Map.of(
                "jackson",    "2.14.0",
                "spring",     "6.1.0",
                "slf4j",      "2.0.5"
        );

        // POM directo: máxima prioridad sobre cualquier BOM
        // El POM declara jackson:2.16 explícitamente
        BomManager manager = new BomManager();

        System.out.println("BOMs importados:");
        System.out.println("  BOM-A (orden 1): jackson=2.15.0, slf4j=2.0.7");
        System.out.println("  BOM-B (orden 2): jackson=2.14.0, spring=6.1.0, slf4j=2.0.5");
        System.out.println("  POM directo    : jackson=2.16.0");
        System.out.println();

        manager.addBom("BOM-A", 1, bomA);
        manager.addBom("BOM-B", 2, bomB);
        manager.addPomDirect("jackson", "2.16.0");

        List<String> deps = List.of("jackson", "spring", "slf4j", "guava");
        manager.resolve(deps);

        System.out.println("\n=== Reglas aplicadas ===");
        System.out.println("  1. POM directo > BOM (nearest wins)");
        System.out.println("  2. BOM-A > BOM-B (primer BOM importado tiene precedencia)");
        System.out.println("  3. jackson: POM directo (2.16.0) gana sobre BOM-A (2.15.0) y BOM-B (2.14.0)");
        System.out.println("  4. spring:  solo en BOM-B, se usa 6.1.0");
        System.out.println("  5. slf4j:   BOM-A (2.0.7) gana sobre BOM-B (2.0.5)");
    }
}
