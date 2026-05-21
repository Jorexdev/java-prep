import java.util.*;

public class Ejercicio3 {

    static class PortMapping {
        int hostPort;
        int containerPort;
        String protocol;

        PortMapping(int hostPort, int containerPort, String protocol) {
            this.hostPort      = hostPort;
            this.containerPort = containerPort;
            this.protocol      = protocol;
        }

        @Override
        public String toString() {
            return hostPort + ":" + containerPort + "/" + protocol;
        }
    }

    static class Container {
        String name;
        String image;
        List<PortMapping> ports;

        Container(String name, String image, PortMapping... mappings) {
            this.name  = name;
            this.image = image;
            this.ports = new ArrayList<>(Arrays.asList(mappings));
        }
    }

    static void printPortTable(List<Container> containers) {
        System.out.println("\n=== Tabla de puertos ===");
        System.out.printf("%-16s %-12s %-10s %-10s %-8s%n",
                "Container", "Image", "HostPort", "ContPort", "Protocol");
        System.out.println("-".repeat(60));
        for (Container c : containers) {
            for (PortMapping p : c.ports) {
                System.out.printf("%-16s %-12s %-10d %-10d %-8s%n",
                        c.name, c.image, p.hostPort, p.containerPort, p.protocol);
            }
        }
    }

    static void detectConflicts(List<Container> containers) {
        System.out.println("\n=== Detección de conflictos de puertos ===");
        Map<String, String> hostPortOwner = new LinkedHashMap<>();
        boolean hasConflict = false;

        for (Container c : containers) {
            for (PortMapping p : c.ports) {
                String key = p.hostPort + "/" + p.protocol;
                if (hostPortOwner.containsKey(key)) {
                    System.out.printf("CONFLICTO: puerto %s ya ocupado por '%s', '%s' no puede usarlo%n",
                            key, hostPortOwner.get(key), c.name);
                    hasConflict = true;
                } else {
                    hostPortOwner.put(key, c.name);
                }
            }
        }
        if (!hasConflict) {
            System.out.println("Sin conflictos de puertos.");
        }
    }

    public static void main(String[] args) {
        List<Container> containers = new ArrayList<>();
        containers.add(new Container("web-1", "nginx:latest",
                new PortMapping(80,   80,   "tcp"),
                new PortMapping(443,  443,  "tcp")));
        containers.add(new Container("web-2", "nginx:latest",
                new PortMapping(80,   80,   "tcp"),   // conflicto con web-1
                new PortMapping(8080, 80,   "tcp")));
        containers.add(new Container("api",   "node:18",
                new PortMapping(3000, 3000, "tcp")));
        containers.add(new Container("db",    "postgres:15",
                new PortMapping(5432, 5432, "tcp")));

        printPortTable(containers);
        detectConflicts(containers);
    }
}
