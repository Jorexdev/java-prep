import java.util.*;

// ===== Modelo de artefacto Maven =====

class Artifact {
    final String groupId;
    final String artifactId;
    final String version;

    public Artifact(String groupId, String artifactId, String version) {
        this.groupId    = groupId;
        this.artifactId = artifactId;
        this.version    = version;
    }

    // Clave de identidad sin versión (para detectar conflictos)
    public String key() { return groupId + ":" + artifactId; }

    @Override
    public String toString() { return groupId + ":" + artifactId + ":" + version; }
}

// ===== Grafo de dependencias =====

class DependencyGraph {
    // Arbol: artifact → lista de sus dependencias directas
    private final Map<String, List<Artifact>> graph = new LinkedHashMap<>();
    // Exclusiones: artefacto padre → conjunto de artefactos excluidos
    private final Map<String, Set<String>>    exclusions = new HashMap<>();
    // DependencyManagement: versiones forzadas independientemente de la ruta
    private final Map<String, String>         managedVersions = new LinkedHashMap<>();

    public void addDependency(Artifact from, Artifact... deps) {
        graph.computeIfAbsent(from.toString(), k -> new ArrayList<>());
        for (Artifact dep : deps) {
            graph.get(from.toString()).add(dep);
        }
    }

    public void addExclusion(Artifact from, String excludedKey) {
        exclusions.computeIfAbsent(from.toString(), k -> new HashSet<>()).add(excludedKey);
    }

    public void addManagedVersion(String groupId, String artifactId, String version) {
        managedVersions.put(groupId + ":" + artifactId, version);
    }

    // BFS — "nearest wins": la primera vez que se alcanza un artifact:id gana
    public Map<String, Artifact> resolve(Artifact root) {
        Map<String, Artifact>  resolved  = new LinkedHashMap<>();
        Map<String, Integer>   depthMap  = new LinkedHashMap<>();
        List<String>           conflicts = new ArrayList<>();
        Queue<Artifact>        queue     = new LinkedList<>();

        queue.add(root);
        depthMap.put(root.toString(), 0);

        while (!queue.isEmpty()) {
            Artifact current = queue.poll();
            int      depth   = depthMap.getOrDefault(current.toString(), 0);

            String key = current.key();

            // DependencyManagement: sobreescribe la versión si hay una gestionada
            String managedVersion = managedVersions.get(key);
            Artifact effective = managedVersion != null
                    ? new Artifact(current.groupId, current.artifactId, managedVersion)
                    : current;

            if (!resolved.containsKey(key)) {
                resolved.put(key, effective);
            } else {
                // Conflicto: ya está resuelto — nearest wins (no se modifica)
                Artifact winner = resolved.get(key);
                if (!winner.version.equals(effective.version)) {
                    conflicts.add(String.format(
                            "  CONFLICTO: %s — depth=%d selecciona %s (ignora %s)",
                            key, depth, winner.version, effective.version));
                }
            }

            // Añadir dependencias transitivas de este artefacto
            List<Artifact> deps = graph.getOrDefault(current.toString(), Collections.emptyList());
            Set<String>    excl = exclusions.getOrDefault(current.toString(), Collections.emptySet());

            for (Artifact dep : deps) {
                if (excl.contains(dep.key())) {
                    System.out.println("  [exclusion] " + dep.key() + " excluido desde " + current.artifactId);
                    continue;
                }
                depthMap.put(dep.toString(), depth + 1);
                queue.add(dep);
            }
        }

        if (!conflicts.isEmpty()) {
            System.out.println("\n[!] Conflictos detectados (nearest wins):");
            conflicts.forEach(System.out::println);
        }

        return resolved;
    }
}

public class ExpDependencyResolution {

    // ─── 1. "Nearest wins": A→B:1.0 vs A→C→B:2.0 ────────────────────────────
    static void nearestWins() {
        System.out.println("\n── 1. Nearest wins ──");
        // Árbol:
        //   A
        //   ├─ B:1.0    (depth 1)
        //   └─ C:1.0
        //       └─ B:2.0 (depth 2 — pierde)

        Artifact A   = new Artifact("com.example", "A",   "1.0");
        Artifact B10 = new Artifact("com.example", "B",   "1.0");
        Artifact B20 = new Artifact("com.example", "B",   "2.0");
        Artifact C   = new Artifact("com.example", "C",   "1.0");

        DependencyGraph g = new DependencyGraph();
        g.addDependency(A,   B10, C);   // A depende de B:1.0 y C
        g.addDependency(C,   B20);      // C depende de B:2.0

        Map<String, Artifact> resolved = g.resolve(A);
        System.out.println("Árbol resuelto:");
        resolved.forEach((k, v) -> System.out.println("  " + k + " → " + v.version));
        System.out.println("B seleccionada: " + resolved.get("com.example:B").version
                + " (nearest = depth 1 gana sobre depth 2)");
    }

    // ─── 2. <exclusion>: eliminar una dependencia transitiva ─────────────────
    static void exclusion() {
        System.out.println("\n── 2. <exclusion> — eliminar transitiva ──");
        // A→C→B:2.0, pero se excluye B desde C

        Artifact A   = new Artifact("com.example", "A",   "1.0");
        Artifact B20 = new Artifact("com.example", "B",   "2.0");
        Artifact C   = new Artifact("com.example", "C",   "1.0");

        DependencyGraph g = new DependencyGraph();
        g.addDependency(A, C);
        g.addDependency(C, B20);
        g.addExclusion(C, "com.example:B"); // <exclusion> en C

        Map<String, Artifact> resolved = g.resolve(A);
        System.out.println("Árbol resuelto (B debería estar excluida):");
        resolved.forEach((k, v) -> System.out.println("  " + k + " → " + v.version));
        boolean bPresent = resolved.containsKey("com.example:B");
        System.out.println("com.example:B en árbol: " + bPresent + " (esperado: false)");
    }

    // ─── 3. <dependencyManagement>: versión forzada ───────────────────────────
    static void dependencyManagement() {
        System.out.println("\n── 3. <dependencyManagement> — versión forzada ──");
        // A→B:1.0 y A→C→B:2.0, pero dependencyManagement fuerza B:3.0

        Artifact A   = new Artifact("com.example", "A",   "1.0");
        Artifact B10 = new Artifact("com.example", "B",   "1.0");
        Artifact B20 = new Artifact("com.example", "B",   "2.0");
        Artifact C   = new Artifact("com.example", "C",   "1.0");

        DependencyGraph g = new DependencyGraph();
        g.addDependency(A, B10, C);
        g.addDependency(C, B20);
        g.addManagedVersion("com.example", "B", "3.0"); // <dependencyManagement>

        Map<String, Artifact> resolved = g.resolve(A);
        System.out.println("Árbol resuelto:");
        resolved.forEach((k, v) -> System.out.println("  " + k + " → " + v.version));
        System.out.println("B seleccionada: " + resolved.get("com.example:B").version
                + " (dependencyManagement fuerza 3.0)");
    }

    // ─── 4. Árbol completo de dependencias ────────────────────────────────────
    static void fullDependencyTree() {
        System.out.println("\n── 4. Árbol de dependencias completo (mvn dependency:tree) ──");
        /*
         * Ejemplo:
         *   spring-boot-starter-web
         *     ├─ spring-boot-starter
         *     │     └─ spring-core:6.1.2
         *     ├─ spring-webmvc
         *     │     └─ spring-core:6.1.2  ← conflicto, mismo depth → nearest-wins por orden BFS
         *     └─ jackson-databind:2.16.1
         *
         * mvn dependency:tree imprime exactamente este árbol con versiones ganadoras.
         * mvn dependency:tree -Dverbose muestra también las versiones omitidas.
         */
        System.out.println("  mvn dependency:tree            → árbol con versiones ganadoras");
        System.out.println("  mvn dependency:tree -Dverbose  → incluye versiones omitidas");
        System.out.println("  mvn dependency:analyze         → detecta deps declaradas no usadas");
    }

    public static void main(String[] args) {
        nearestWins();
        exclusion();
        dependencyManagement();
        fullDependencyTree();
    }
}
