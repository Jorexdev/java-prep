import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    static class Dep {
        final String groupId;
        final String artifactId;
        final String version;
        final List<Dep> children = new ArrayList<>();

        Dep(String g, String a, String v, Dep... kids) {
            this.groupId    = g;
            this.artifactId = a;
            this.version    = v;
            for (Dep k : kids) children.add(k);
        }

        String key() { return groupId + ":" + artifactId; }
        String coords() { return key() + ":" + version; }
    }

    static class ResolvedTree {
        // key -> versión ganadora
        private final Map<String, String> winners = new LinkedHashMap<>();

        // Construye el árbol resuelto (primera pasada: asignar versiones ganadoras)
        void collectWinners(Dep dep, int depth) {
            String key = dep.key();
            if (!winners.containsKey(key)) {
                winners.put(key, dep.version);
            }
            // aunque ya existe, los hijos siguen procesándose para que sus propios hijos sean registrados
            dep.children.forEach(c -> collectWinners(c, depth + 1));
        }

        // Imprime el árbol con formato de `mvn dependency:tree`
        void printTree(Dep dep, String indent, boolean isRoot) {
            if (isRoot) {
                System.out.println(dep.coords());
            } else {
                String winnerVersion = winners.get(dep.key());
                if (winnerVersion.equals(dep.version)) {
                    System.out.println(indent + "+- " + dep.coords());
                } else {
                    System.out.println(indent + "+- " + dep.coords()
                            + "  (omitted for conflict with " + winnerVersion + ")");
                }
            }
            String childIndent = isRoot ? "|  " : indent + "|  ";
            for (int i = 0; i < dep.children.size(); i++) {
                Dep child = dep.children.get(i);
                boolean last = (i == dep.children.size() - 1);
                String marker = last ? "\\- " : "+- ";
                String nextIndent = isRoot ? "|  " : indent + "|  ";
                printNode(child, nextIndent, marker);
            }
        }

        void printNode(Dep dep, String indent, String marker) {
            String winnerVersion = winners.get(dep.key());
            String line;
            if (winnerVersion != null && winnerVersion.equals(dep.version)) {
                line = indent + marker + dep.coords();
            } else if (winnerVersion != null) {
                line = indent + marker + dep.coords()
                        + "  (omitted for conflict with " + winnerVersion + ")";
            } else {
                line = indent + marker + dep.coords();
            }
            System.out.println(line);

            for (int i = 0; i < dep.children.size(); i++) {
                Dep child = dep.children.get(i);
                boolean last = (i == dep.children.size() - 1);
                String childMarker = last ? "\\- " : "+- ";
                printNode(child, indent + "   ", childMarker);
            }
        }
    }

    public static void main(String[] args) {
        // Árbol con conflictos:
        // A depende de B:1.0 y C:1.0
        // B depende de D:2.0 y E:1.0
        // C depende de D:1.5 (conflicto con D:2.0) y F:1.0
        // F depende de E:2.0 (conflicto con E:1.0)
        Dep E_v10 = new Dep("com.example", "E", "1.0");
        Dep E_v20 = new Dep("com.example", "E", "2.0");
        Dep D_v20 = new Dep("com.example", "D", "2.0");
        Dep D_v15 = new Dep("com.example", "D", "1.5");
        Dep F     = new Dep("com.example", "F", "1.0", E_v20);
        Dep B     = new Dep("com.example", "B", "1.0", D_v20, E_v10);
        Dep C     = new Dep("com.example", "C", "1.0", D_v15, F);
        Dep A     = new Dep("com.example", "A", "1.0", B, C);

        ResolvedTree tree = new ResolvedTree();
        // Primera pasada: registrar versiones ganadoras (DFS, primera aparición = winner)
        tree.collectWinners(A, 0);

        System.out.println("=== mvn dependency:tree ===");
        System.out.println();
        System.out.println(A.coords());
        for (int i = 0; i < A.children.size(); i++) {
            Dep child = A.children.get(i);
            boolean last = (i == A.children.size() - 1);
            String marker = last ? "\\- " : "+- ";
            tree.printNode(child, "", marker);
        }

        System.out.println();
        System.out.println("=== Versiones ganadoras ===");
        tree.winners.forEach((key, version) ->
            System.out.printf("  %-30s -> %s%n", key, version));
    }
}
