import java.util.*;

public class Ejercicio5 {

    static class Pod {
        String name;
        double cpuRequest;   // en millicores
        double memRequest;   // en MB

        Pod(String name, double cpuRequest, double memRequest) {
            this.name       = name;
            this.cpuRequest = cpuRequest;
            this.memRequest = memRequest;
        }

        @Override
        public String toString() {
            return String.format("%-18s cpu=%.0fm  mem=%.0fMB", name, cpuRequest, memRequest);
        }
    }

    static class Node {
        String name;
        double cpuCapacity;
        double memCapacity;
        List<Pod> running = new ArrayList<>();

        Node(String name, double cpuCapacity, double memCapacity) {
            this.name        = name;
            this.cpuCapacity = cpuCapacity;
            this.memCapacity = memCapacity;
        }

        double usedCpu() { return running.stream().mapToDouble(p -> p.cpuRequest).sum(); }
        double usedMem() { return running.stream().mapToDouble(p -> p.memRequest).sum(); }
        double freeCpu() { return cpuCapacity - usedCpu(); }
        double freeMem() { return memCapacity - usedMem(); }

        boolean canSchedule(Pod pod) {
            return freeCpu() >= pod.cpuRequest && freeMem() >= pod.memRequest;
        }

        boolean schedule(Pod pod) {
            if (canSchedule(pod)) {
                running.add(pod);
                return true;
            }
            return false;
        }

        void printStatus() {
            System.out.printf("Node '%s'  CPU: %.0f/%.0fm (%.0f%%)  MEM: %.0f/%.0fMB (%.0f%%)%n",
                    name,
                    usedCpu(), cpuCapacity, usedCpu() / cpuCapacity * 100,
                    usedMem(), memCapacity, usedMem() / memCapacity * 100);
            running.forEach(p -> System.out.println("  " + p));
            System.out.printf("  Libre: cpu=%.0fm  mem=%.0fMB%n", freeCpu(), freeMem());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Resource Fit ===\n");

        Node node = new Node("worker-1", 4000, 8192); // 4 CPU, 8 GB

        // Pre-cargar el nodo al ~80%
        node.schedule(new Pod("api-server",    1000, 2048));
        node.schedule(new Pod("web-frontend",   500, 1024));
        node.schedule(new Pod("metrics-agent",  200,  512));
        node.schedule(new Pod("sidecar-proxy",  300,  256));
        node.schedule(new Pod("log-forwarder",  100,  256));

        node.printStatus();
        System.out.printf("%nUso aproximado: CPU=%.0f%%  MEM=%.0f%%%n%n",
                node.usedCpu() / node.cpuCapacity * 100,
                node.usedMem() / node.memCapacity * 100);

        // Intentar schedulear pods de distintos tamaños
        List<Pod> candidates = List.of(
                new Pod("tiny-job",     100,  128),   // cabe
                new Pod("medium-job",   800,  768),   // cabe
                new Pod("large-job",   1500, 3000),   // no cabe (mem)
                new Pod("cpu-hungry",  3000,  256),   // no cabe (cpu)
                new Pod("small-job",    200,  400)    // cabe
        );

        System.out.println("=== Intentando schedulear nuevos pods ===");
        for (Pod p : candidates) {
            boolean ok = node.schedule(p);
            System.out.printf("  %-18s cpu=%.0fm mem=%.0fMB → %s%n",
                    p.name, p.cpuRequest, p.memRequest,
                    ok ? "SCHEDULED" : "UNSCHEDULABLE (recursos insuficientes)");
        }

        System.out.println("\n=== Estado final del nodo ===");
        node.printStatus();
    }
}
