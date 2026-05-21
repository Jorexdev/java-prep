import java.util.*;

public class Ejercicio1 {

    enum Operator { IN, NOT_IN }

    static class AffinityRule {
        String key;
        List<String> values;
        Operator op;

        AffinityRule(String key, List<String> values, Operator op) {
            this.key    = key;
            this.values = values;
            this.op     = op;
        }

        boolean matches(Map<String, String> labels) {
            String v = labels.get(key);
            if (v == null) return op == Operator.NOT_IN;
            return op == Operator.IN ? values.contains(v) : !values.contains(v);
        }
    }

    static class Node {
        String name;
        double cpuFree;
        double memFree;
        Map<String, String> labels;
        List<String> assignedPods = new ArrayList<>();

        Node(String name, double cpuFree, double memFree, Map<String, String> labels) {
            this.name    = name;
            this.cpuFree = cpuFree;
            this.memFree = memFree;
            this.labels  = new LinkedHashMap<>(labels);
        }

        // Puntuación: porcentaje de CPU libre (0-100)
        double score() {
            return cpuFree;
        }
    }

    static class Pod {
        String name;
        double cpuReq;
        double memReq;
        List<AffinityRule> affinity;

        Pod(String name, double cpuReq, double memReq, List<AffinityRule> affinity) {
            this.name     = name;
            this.cpuReq   = cpuReq;
            this.memReq   = memReq;
            this.affinity = affinity != null ? affinity : List.of();
        }
    }

    static class Scheduler {

        void schedule(List<Pod> pods, List<Node> nodes) {
            System.out.println("=== Kubernetes Scheduler ===\n");

            for (Pod pod : pods) {
                System.out.printf("Scheduling '%s' (cpu=%.0f mem=%.0f)%n",
                        pod.name, pod.cpuReq, pod.memReq);

                // Paso 1: filtrado
                List<Node> candidates = new ArrayList<>();
                for (Node n : nodes) {
                    boolean resourcesOk = n.cpuFree >= pod.cpuReq && n.memFree >= pod.memReq;
                    boolean affinityOk  = pod.affinity.stream().allMatch(r -> r.matches(n.labels));

                    String reason = !resourcesOk ? "resources" : !affinityOk ? "affinity" : "OK";
                    System.out.printf("  Filter %-16s → %s%n", n.name, reason);

                    if (resourcesOk && affinityOk) candidates.add(n);
                }

                if (candidates.isEmpty()) {
                    System.out.println("  → UNSCHEDULABLE\n");
                    continue;
                }

                // Paso 2: scoring
                System.out.println("  Scoring:");
                candidates.forEach(n ->
                        System.out.printf("    %-16s score=%.0f%n", n.name, n.score()));

                // Paso 3: asignación al mejor
                Node best = candidates.stream()
                        .max(Comparator.comparingDouble(Node::score))
                        .orElseThrow();

                best.cpuFree -= pod.cpuReq;
                best.memFree -= pod.memReq;
                best.assignedPods.add(pod.name);
                System.out.printf("  → Asignado a '%s'%n%n", best.name);
            }

            System.out.println("=== Resultado final por nodo ===");
            nodes.forEach(n -> System.out.printf("  %-16s pods=%s  cpuFree=%.0f  memFree=%.0f%n",
                    n.name, n.assignedPods, n.cpuFree, n.memFree));
        }
    }

    public static void main(String[] args) {
        List<Node> nodes = List.of(
                new Node("node-1", 4000, 8192, Map.of("zone", "eu-west",  "type", "standard")),
                new Node("node-2", 2000, 4096, Map.of("zone", "eu-west",  "type", "highmem")),
                new Node("node-3", 6000, 16384,Map.of("zone", "us-east",  "type", "standard"))
        );

        List<Pod> pods = List.of(
                new Pod("frontend",  500,  512, List.of(
                        new AffinityRule("zone", List.of("eu-west"), Operator.IN))),
                new Pod("backend",   1000, 2048, List.of(
                        new AffinityRule("type", List.of("standard"), Operator.IN))),
                new Pod("db",        3000, 6144, null),
                new Pod("batch-job", 5000, 1024, List.of(
                        new AffinityRule("zone", List.of("us-east"), Operator.IN))),
                new Pod("tiny",      100,  64,  null)
        );

        new Scheduler().schedule(pods, new ArrayList<>(nodes));
    }
}
