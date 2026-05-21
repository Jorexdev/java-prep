import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    static class Dependency {
        final String artifactId;
        final String version;
        final List<Dependency> transitive = new ArrayList<>();

        Dependency(String artifactId, String version, Dependency... deps) {
            this.artifactId = artifactId;
            this.version = version;
            for (Dependency dep : deps) transitive.add(dep);
        }

        @Override
        public String toString() {
            return artifactId + ":" + version;
        }
    }

    // Mapa de versiones resueltas: artifactId → (version, depth)
    static class ResolvedDep {
        String version;
        int depth;
        String resolvedFrom;

        ResolvedDep(String version, int depth, String from) {
            this.version = version;
            this.depth = depth;
            this.resolvedFrom = from;
        }
    }

    // BFS/DFS para resolver deps con "nearest wins"
    static Map<String, ResolvedDep> resolve(Dependency root) {
        Map<String, ResolvedDep> resolved = new LinkedHashMap<>();
        resolve(root, 0, resolved);
        return resolved;
    }

    static void resolve(Dependency dep, int depth, Map<String, ResolvedDep> resolved) {
        String key = dep.artifactId;
        if (!resolved.containsKey(key)) {
            // Primera vez que vemos este artifactId — gana
            resolved.put(key, new ResolvedDep(dep.version, depth, dep.toString()));
        } else {
            ResolvedDep existing = resolved.get(key);
            if (depth < existing.depth) {
                // Más cercano — sobreescribir (nearest wins)
                System.out.printf("  [CONFLICT] %s:%s vs %s:%s -> gana %s (depth %d < %d)%n",
                        key, dep.version, key, existing.version, dep.version, depth, existing.depth);
                resolved.put(key, new ResolvedDep(dep.version, depth, dep.toString()));
            } else {
                System.out.printf("  [OMITTED]  %s:%s omitido (depth %d), ya resuelto como %s:%s (depth %d)%n",
                        key, dep.version, depth, key, existing.version, existing.depth);
            }
        }
        // Resolver dependencias transitivas
        for (Dependency transitive : dep.transitive) {
            resolve(transitive, depth + 1, resolved);
        }
    }

    static void printTree(Dependency dep, String indent) {
        System.out.println(indent + dep);
        dep.transitive.forEach(t -> printTree(t, indent + "   "));
    }

    public static void main(String[] args) {
        // D:2.0 está a distancia 2 a través de B
        // D:1.5 está a distancia 2 a través de C
        // Con "nearest wins" y llegada en orden: D:2.0 gana (llega primero)
        Dependency D_v20 = new Dependency("D", "2.0");
        Dependency D_v15 = new Dependency("D", "1.5");
        Dependency B    = new Dependency("B", "1.0", D_v20);
        Dependency C    = new Dependency("C", "1.0", D_v15);
        Dependency A    = new Dependency("A", "1.0", B, C);

        System.out.println("=== Árbol de dependencias ===");
        printTree(A, "");
        System.out.println();

        System.out.println("=== Resolución (nearest wins) ===");
        Map<String, ResolvedDep> resolved = resolve(A);
        System.out.println();

        System.out.println("=== Dependencias resueltas ===");
        resolved.forEach((id, dep) ->
            System.out.printf("  %-6s -> %s (depth=%d)%n", id, dep.version, dep.depth));

        System.out.println();
        System.out.println("Nota: D se resuelve como " + resolved.get("D").version +
                " porque B:D está en el primer path encontrado a depth=2.");
        System.out.println("Si A hubiera declarado D directamente (depth=1), esa versión habría ganado.");
    }
}
