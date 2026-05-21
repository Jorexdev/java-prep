import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    static class Commit {
        final String hash;
        final String message;
        final List<Commit> parents; // puede tener 1 o 2 padres
        final Map<String, String> changes; // línea → contenido

        Commit(String hash, String message, List<Commit> parents, Map<String, String> changes) {
            this.hash = hash;
            this.message = message;
            this.parents = parents;
            this.changes = changes;
        }

        // Constructor para commit de merge (sin cambios propios)
        Commit(String hash, String message, Commit parent1, Commit parent2) {
            this(hash, message, List.of(parent1, parent2), Map.of());
        }
    }

    static class MergeResult {
        final Commit mergeCommit;
        final List<String> conflicts;

        MergeResult(Commit mergeCommit, List<String> conflicts) {
            this.mergeCommit = mergeCommit;
            this.conflicts = conflicts;
        }
    }

    // Encontrar el ancestro común más reciente (simplificado: recorre hasta encontrar hash igual)
    static Commit findAncestor(Commit a, Commit b) {
        List<String> ancestorsA = new ArrayList<>();
        Commit cur = a;
        while (cur != null) {
            ancestorsA.add(cur.hash);
            cur = cur.parents.isEmpty() ? null : cur.parents.get(0);
        }
        cur = b;
        while (cur != null) {
            if (ancestorsA.contains(cur.hash)) return cur;
            cur = cur.parents.isEmpty() ? null : cur.parents.get(0);
        }
        return null;
    }

    // Recopilar todos los cambios desde `ancestor` hasta `head`
    static Map<String, String> changesFrom(Commit ancestor, Commit head) {
        Map<String, String> result = new LinkedHashMap<>();
        List<Commit> path = new ArrayList<>();
        Commit cur = head;
        while (cur != null && !cur.hash.equals(ancestor.hash)) {
            path.add(0, cur);
            cur = cur.parents.isEmpty() ? null : cur.parents.get(0);
        }
        for (Commit c : path) {
            result.putAll(c.changes);
        }
        return result;
    }

    static MergeResult merge(Commit mainHead, Commit featureHead, String mergeHash) {
        Commit ancestor = findAncestor(mainHead, featureHead);
        if (ancestor == null) {
            throw new IllegalStateException("No hay ancestro común");
        }
        System.out.println("Ancestro común: " + ancestor.hash + " — " + ancestor.message);

        Map<String, String> mainChanges    = changesFrom(ancestor, mainHead);
        Map<String, String> featureChanges = changesFrom(ancestor, featureHead);

        List<String> conflicts = new ArrayList<>();
        Map<String, String> merged = new LinkedHashMap<>();

        // Combinar cambios
        for (String line : mainChanges.keySet()) {
            merged.put(line, mainChanges.get(line));
        }
        for (String line : featureChanges.keySet()) {
            if (mainChanges.containsKey(line)
                    && !mainChanges.get(line).equals(featureChanges.get(line))) {
                conflicts.add(line);
            } else {
                merged.put(line, featureChanges.get(line));
            }
        }

        Commit mergeCommit = new Commit(mergeHash, "Merge feature into main",
                mainHead, featureHead);
        return new MergeResult(mergeCommit, conflicts);
    }

    public static void main(String[] args) {
        // Ancestro común
        Commit base = new Commit("aaa0001", "Initial commit", List.of(), Map.of());

        // main modifica línea 1 y línea 3
        Commit mainCommit = new Commit("bbb0001", "main: update config",
                List.of(base),
                Map.of("line1", "main-value", "line3", "shared-main"));

        // feature modifica línea 2 y línea 3 (conflicto en línea 3)
        Commit featureCommit = new Commit("ccc0001", "feature: add feature",
                List.of(base),
                Map.of("line2", "feature-value", "line3", "shared-feature"));

        MergeResult result = merge(mainCommit, featureCommit, "ddd0001");

        System.out.println("\n=== Resultado del merge ===");
        if (result.conflicts.isEmpty()) {
            System.out.println("Merge commit creado: " + result.mergeCommit.hash);
            System.out.println("Padres: " + result.mergeCommit.parents.stream()
                    .map(c -> c.hash).toList());
        } else {
            System.out.println("CONFLICTOS detectados — merge bloqueado:");
            result.conflicts.forEach(c -> {
                System.out.println("  CONFLICT en '" + c + "':");
                System.out.println("    <<<< HEAD");
                System.out.println("    " + mainCommit.changes.get(c));
                System.out.println("    ====");
                System.out.println("    " + featureCommit.changes.get(c));
                System.out.println("    >>>> feature");
            });
        }
    }
}
