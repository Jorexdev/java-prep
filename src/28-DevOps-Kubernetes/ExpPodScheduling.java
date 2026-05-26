import java.util.*;

public class ExpPodScheduling {

    static class Node {
        private final String name;
        private int availableCpu;    // in millicores
        private int availableMemMb;

        Node(String name, int cpuMillicores, int memMb) {
            this.name = name;
            this.availableCpu = cpuMillicores;
            this.availableMemMb = memMb;
        }

        boolean canFit(int cpuReq, int memReq) {
            return availableCpu >= cpuReq && availableMemMb >= memReq;
        }

        void allocate(int cpuReq, int memReq) {
            availableCpu -= cpuReq;
            availableMemMb -= memReq;
        }

        String getName()         { return name; }
        int getAvailableCpu()    { return availableCpu; }
        int getAvailableMemMb()  { return availableMemMb; }
    }

    static class Pod {
        private final String name;
        private final int cpuRequest;    // millicores
        private final int memRequest;    // MB
        private final String nodeSelector; // null means any node

        Pod(String name, int cpuMillicores, int memMb, String nodeSelector) {
            this.name = name;
            this.cpuRequest = cpuMillicores;
            this.memRequest = memMb;
            this.nodeSelector = nodeSelector;
        }

        Pod(String name, int cpuMillicores, int memMb) {
            this(name, cpuMillicores, memMb, null);
        }

        String getName()        { return name; }
        int getCpuRequest()     { return cpuRequest; }
        int getMemRequest()     { return memRequest; }
        String getNodeSelector(){ return nodeSelector; }
    }

    static class Scheduler {
        private final List<Node> nodes;

        Scheduler(List<Node> nodes) {
            // First-fit decreasing: process largest pods first (caller sorts)
            this.nodes = nodes;
        }

        // Returns the node selected, or null if unschedulable
        Node schedule(Pod pod) {
            for (Node node : nodes) {
                // nodeSelector filter: skip nodes that don't match
                if (pod.getNodeSelector() != null && !node.getName().equals(pod.getNodeSelector())) {
                    continue;
                }
                if (node.canFit(pod.getCpuRequest(), pod.getMemRequest())) {
                    node.allocate(pod.getCpuRequest(), pod.getMemRequest());
                    return node;
                }
            }
            return null; // Pending: no node has enough capacity
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  KUBERNETES POD SCHEDULING — simulación");
        System.out.println("═".repeat(60));

        List<Node> nodes = new ArrayList<>(List.of(
                new Node("node-1", 2000, 4096),   // 2 CPU cores, 4 GB
                new Node("node-2", 4000, 8192),   // 4 CPU cores, 8 GB
                new Node("node-3",  500,  512)    // small node
        ));

        System.out.println("\n[Nodos disponibles]");
        System.out.println("─".repeat(60));
        for (Node n : nodes) {
            System.out.printf("  %-8s  cpu=%4dm  mem=%4d MB%n",
                    n.getName(), n.getAvailableCpu(), n.getAvailableMemMb());
        }

        // Pods sorted largest first (first-fit decreasing heuristic)
        List<Pod> pods = new ArrayList<>(List.of(
                new Pod("api-server",    1000, 2048),
                new Pod("worker",        1500, 3000),
                new Pod("cache",          250,  256),
                new Pod("metrics",        100,   64),
                new Pod("heavy-job",     3000, 6000, "node-2") // nodeSelector
        ));
        pods.sort(Comparator.comparingInt(Pod::getCpuRequest).reversed());

        Scheduler scheduler = new Scheduler(nodes);

        System.out.println("\n[Scheduling pods]");
        System.out.println("─".repeat(60));
        for (Pod pod : pods) {
            Node placed = scheduler.schedule(pod);
            if (placed != null) {
                System.out.printf("  %-12s (cpu=%4dm, mem=%4d MB) → %-8s  [restante: cpu=%4dm, mem=%4d MB]%n",
                        pod.getName(), pod.getCpuRequest(), pod.getMemRequest(),
                        placed.getName(), placed.getAvailableCpu(), placed.getAvailableMemMb());
            } else {
                System.out.printf("  %-12s (cpu=%4dm, mem=%4d MB) → PENDING (sin nodo con capacidad suficiente)%n",
                        pod.getName(), pod.getCpuRequest(), pod.getMemRequest());
            }
        }

        System.out.println("\n[Capacidad restante por nodo]");
        System.out.println("─".repeat(60));
        for (Node n : nodes) {
            System.out.printf("  %-8s  cpu=%4dm  mem=%4d MB%n",
                    n.getName(), n.getAvailableCpu(), n.getAvailableMemMb());
        }
    }
}
