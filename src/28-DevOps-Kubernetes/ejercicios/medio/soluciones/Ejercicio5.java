import java.util.*;

public class Ejercicio5 {

    enum Operator { IN, NOT_IN }

    static class NodeAffinityRule {
        String labelKey;
        List<String> values;
        Operator operator;

        NodeAffinityRule(String labelKey, List<String> values, Operator operator) {
            this.labelKey = labelKey;
            this.values   = new ArrayList<>(values);
            this.operator = operator;
        }

        boolean matches(Map<String, String> nodeLabels) {
            String nodeValue = nodeLabels.get(labelKey);
            if (nodeValue == null) return operator == Operator.NOT_IN;
            boolean contains = values.contains(nodeValue);
            return operator == Operator.IN ? contains : !contains;
        }

        @Override
        public String toString() {
            return labelKey + " " + operator + " " + values;
        }
    }

    static class Node {
        String name;
        Map<String, String> labels;
        double cpuFree;
        double memFree;

        Node(String name, Map<String, String> labels, double cpuFree, double memFree) {
            this.name    = name;
            this.labels  = new LinkedHashMap<>(labels);
            this.cpuFree = cpuFree;
            this.memFree = memFree;
        }

        @Override
        public String toString() {
            return String.format("%-12s labels=%s", name, labels);
        }
    }

    static class Pod {
        String name;
        double cpuReq;
        double memReq;
        List<NodeAffinityRule> affinityRules;

        Pod(String name, double cpuReq, double memReq, List<NodeAffinityRule> affinityRules) {
            this.name          = name;
            this.cpuReq        = cpuReq;
            this.memReq        = memReq;
            this.affinityRules = new ArrayList<>(affinityRules);
        }
    }

    static class Scheduler {

        Node schedule(Pod pod, List<Node> nodes) {
            System.out.printf("Scheduling pod '%s' (cpu=%.0f mem=%.0f)%n",
                    pod.name, pod.cpuReq, pod.memReq);
            System.out.printf("  Affinity rules: %s%n", pod.affinityRules);

            List<Node> eligible = new ArrayList<>();
            for (Node n : nodes) {
                boolean passesAffinity = pod.affinityRules.stream()
                        .allMatch(r -> r.matches(n.labels));
                boolean hasResources   = n.cpuFree >= pod.cpuReq && n.memFree >= pod.memReq;

                System.out.printf("  Node %-12s → affinity=%b resources=%b%n",
                        n.name, passesAffinity, hasResources);

                if (passesAffinity && hasResources) eligible.add(n);
            }

            if (eligible.isEmpty()) {
                System.out.println("  → UNSCHEDULABLE (ningún nodo elegible)");
                return null;
            }

            Node best = eligible.get(0);
            best.cpuFree -= pod.cpuReq;
            best.memFree -= pod.memReq;
            System.out.printf("  → Asignado a '%s'%n", best.name);
            return best;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Pod Affinity Scheduling ===\n");

        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node("node-eu-west-1a",
                Map.of("zone", "eu-west-1a", "type", "standard"), 4000, 8192));
        nodes.add(new Node("node-eu-west-1b",
                Map.of("zone", "eu-west-1b", "type", "standard"), 3500, 6144));
        nodes.add(new Node("node-us-east-1a",
                Map.of("zone", "us-east-1a", "type", "highmem"),  6000, 32768));

        System.out.println("Nodos disponibles:");
        nodes.forEach(n -> System.out.println("  " + n));
        System.out.println();

        // Pod 1: requiere zona EU
        Pod pod1 = new Pod("frontend", 500, 512,
                List.of(new NodeAffinityRule("zone",
                        List.of("eu-west-1a", "eu-west-1b"), Operator.IN)));

        // Pod 2: requiere tipo highmem, NOT en zona eu-west-1a
        Pod pod2 = new Pod("data-processor", 2000, 4096,
                List.of(
                        new NodeAffinityRule("type",  List.of("highmem"),    Operator.IN),
                        new NodeAffinityRule("zone",  List.of("eu-west-1a"), Operator.NOT_IN)
                ));

        // Pod 3: requiere zona que no existe
        Pod pod3 = new Pod("exotic-workload", 100, 256,
                List.of(new NodeAffinityRule("zone",
                        List.of("ap-southeast-1"), Operator.IN)));

        Scheduler scheduler = new Scheduler();
        System.out.println("---");
        scheduler.schedule(pod1, nodes);
        System.out.println("---");
        scheduler.schedule(pod2, nodes);
        System.out.println("---");
        scheduler.schedule(pod3, nodes);
    }
}
