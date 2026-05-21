import java.util.*;

public class Ejercicio1 {

    static class Container {
        String name;
        int cpuShares;
        int memoryMB;
        boolean running;

        Container(String name, int cpuShares, int memoryMB) {
            this.name      = name;
            this.cpuShares = cpuShares;
            this.memoryMB  = memoryMB;
            this.running   = true;
        }

        @Override
        public String toString() {
            return String.format("%-14s cpu=%-4d mem=%4d MB", name, cpuShares, memoryMB);
        }
    }

    static class Node {
        String name;
        int cpuTotal;
        int memoryMB;
        List<Container> containers = new ArrayList<>();

        Node(String name, int cpuTotal, int memoryMB) {
            this.name      = name;
            this.cpuTotal  = cpuTotal;
            this.memoryMB  = memoryMB;
        }

        int usedMemory()   { return containers.stream().mapToInt(c -> c.memoryMB).sum(); }
        int usedCpu()      { return containers.stream().mapToInt(c -> c.cpuShares).sum(); }
        int freeMemory()   { return memoryMB - usedMemory(); }

        boolean scheduleContainer(Container c) {
            System.out.printf("Scheduling '%s' (cpu=%d, mem=%d MB)... ",
                    c.name, c.cpuShares, c.memoryMB);

            if (usedMemory() + c.memoryMB > memoryMB) {
                System.out.println("FALLO: sin memoria disponible");
                return false;
            }
            if (usedCpu() + c.cpuShares > cpuTotal) {
                System.out.println("FALLO: sin CPU disponible");
                return false;
            }
            containers.add(c);
            System.out.println("OK");
            return true;
        }

        void detectAndHandleOOM() {
            if (usedMemory() <= memoryMB) return;

            System.out.println("\n*** OOM DETECTED on node " + name + " ***");
            System.out.printf("Memoria usada: %d MB / %d MB%n", usedMemory(), memoryMB);

            Container victim = containers.stream()
                    .max(Comparator.comparingInt(c -> c.memoryMB))
                    .orElseThrow();

            System.out.printf("OOM Kill: matando container '%s' (%d MB)%n",
                    victim.name, victim.memoryMB);
            victim.running = false;
            containers.remove(victim);
            System.out.printf("Memoria liberada: %d MB  →  uso actual: %d MB%n",
                    victim.memoryMB, usedMemory());
        }

        void printStatus(String title) {
            System.out.println("\n--- " + title + " ---");
            System.out.printf("Node '%s'  CPU: %d/%d shares  MEM: %d/%d MB%n",
                    name, usedCpu(), cpuTotal, usedMemory(), memoryMB);
            for (Container c : containers) {
                System.out.println("  " + c);
            }
            System.out.printf("  Libre: %d MB%n", freeMemory());
        }
    }

    public static void main(String[] args) {
        Node node = new Node("worker-1", 1024, 4096);

        node.printStatus("Estado inicial");

        System.out.println("\n=== Scheduling containers ===");
        node.scheduleContainer(new Container("nginx",     128,  256));
        node.scheduleContainer(new Container("api-server",256, 1024));
        node.scheduleContainer(new Container("postgres",  512, 2048));

        node.printStatus("Después del scheduling normal");

        // Forzar OOM: añadir un container que excede la memoria restante
        System.out.println("\n=== Forzando OOM ===");
        Container big = new Container("memory-hog", 256, 1500);
        // Añadirlo directamente (sin pasar validación) para simular OOM
        node.containers.add(big);
        System.out.printf("Container '%s' añadido forzando uso = %d MB (límite = %d MB)%n",
                big.name, node.usedMemory(), node.memoryMB);

        node.detectAndHandleOOM();
        node.printStatus("Después del OOM Kill");
    }
}
