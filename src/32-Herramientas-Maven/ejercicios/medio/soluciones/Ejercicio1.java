import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Ejercicio1 {

    static class Dep {
        final String id;
        final String version;
        final List<Dep> children = new ArrayList<>();

        Dep(String id, String version, Dep... kids) {
            this.id = id;
            this.version = version;
            for (Dep k : kids) children.add(k);
        }

        @Override public String toString() { return id + ":" + version; }
    }

    static class ResolvedVersion {
        final String version;
        final int depth;
        final String path;

        ResolvedVersion(String version, int depth, String path) {
            this.version = version;
            this.depth = depth;
            this.path = path;
        }
    }

    // BFS para encontrar todas las ocurrencias de cada artifactId con su profundidad
    static Map<String, List<ResolvedVersion>> findAllVersions(Dep root) {
        Map<String, List<ResolvedVersion>> found = new LinkedHashMap<>();

        // BFS: (nodo, profundidad, camino)
        record Entry(Dep dep, int depth, String path) {}
        Queue<Entry> queue = new LinkedList<>();
        queue.add(new Entry(root, 0, root.id));

        while (!queue.isEmpty()) {
            Entry e = queue.poll();
            String key = e.dep().id;
            if (!key.equals(root.id)) { // ignorar el propio root
                found.computeIfAbsent(key, k -> new ArrayList<>())
                     .add(new ResolvedVersion(e.dep().version, e.depth(), e.path()));
            }
            for (Dep child : e.dep().children) {
                queue.add(new Entry(child, e.depth() + 1, e.path() + " -> " + child));
            }
        }
        return found;
    }

    // Resolver: para cada artifactId, gana la versión con menor depth (nearest wins)
    static Map<String, ResolvedVersion> resolve(Dep root) {
        Map<String, List<ResolvedVersion>> all = findAllVersions(root);
        Map<String, ResolvedVersion> resolved = new LinkedHashMap<>();

        all.forEach((id, versions) -> {
            ResolvedVersion winner = versions.stream()
                    .min((a, b) -> a.depth - b.depth)
                    .orElseThrow();
            resolved.put(id, winner);

            if (versions.size() > 1) {
                System.out.println("[CONFLICT] " + id + ":");
                versions.forEach(v ->
                    System.out.printf("  depth=%d  %s:%s  via [%s]%n",
                            v.depth, id, v.version, v.path));
                System.out.printf("  => WINNER: %s:%s (depth=%d)%n%n",
                        id, winner.version, winner.depth);
            }
        });
        return resolved;
    }

    static void printTree(Dep dep, String indent) {
        System.out.println(indent + dep);
        dep.children.forEach(c -> printTree(c, indent + "   "));
    }

    public static void main(String[] args) {
        // Árbol de 4 niveles con conflictos en D y E
        Dep D_v20  = new Dep("D", "2.0");
        Dep D_v15  = new Dep("D", "1.5");
        Dep D_v10  = new Dep("D", "1.0");
        Dep E_v30  = new Dep("E", "3.0");
        Dep E_v20  = new Dep("E", "2.0");

        Dep F   = new Dep("F", "1.0", D_v10, E_v20);
        Dep C   = new Dep("C", "1.0", D_v15, F);
        Dep B   = new Dep("B", "1.0", D_v20, E_v30);
        Dep A   = new Dep("A", "1.0", B, C); // root project

        System.out.println("=== Árbol de dependencias ===");
        printTree(A, "");
        System.out.println();

        System.out.println("=== Resolviendo conflictos (nearest wins) ===");
        System.out.println();
        Map<String, ResolvedVersion> resolved = resolve(A);

        System.out.println("=== Dependencias resueltas ===");
        resolved.forEach((id, rv) ->
            System.out.printf("  %-5s -> %s (depth=%d, via: %s)%n",
                    id, rv.version, rv.depth, rv.path));
    }
}
