import java.util.*;

public class Ejercicio2 {

    static class Service {
        String name;
        String image;
        List<String> dependsOn;
        List<String> ports;

        Service(String name, String image, List<String> dependsOn, List<String> ports) {
            this.name      = name;
            this.image     = image;
            this.dependsOn = new ArrayList<>(dependsOn);
            this.ports     = new ArrayList<>(ports);
        }
    }

    static List<String> topologicalSort(List<Service> services) {
        Map<String, Service> byName = new LinkedHashMap<>();
        for (Service s : services) byName.put(s.name, s);

        Map<String, Integer> inDegree  = new LinkedHashMap<>();
        Map<String, List<String>> deps = new LinkedHashMap<>();

        for (Service s : services) {
            inDegree.put(s.name, s.dependsOn.size());
            deps.put(s.name, new ArrayList<>(s.dependsOn));
        }

        // Kahn's algorithm
        Queue<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) queue.add(e.getKey());
        }

        List<String> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            order.add(current);
            // actualizar in-degree de los que dependen del actual
            for (Service s : services) {
                if (s.dependsOn.contains(current)) {
                    int newDegree = inDegree.get(s.name) - 1;
                    inDegree.put(s.name, newDegree);
                    if (newDegree == 0) queue.add(s.name);
                }
            }
        }

        if (order.size() != services.size()) {
            List<String> remaining = new ArrayList<>();
            for (Service s : services) {
                if (!order.contains(s.name)) remaining.add(s.name);
            }
            throw new IllegalStateException("Ciclo de dependencias detectado en: " + remaining);
        }

        return order;
    }

    public static void main(String[] args) {
        System.out.println("=== docker-compose Startup Order ===\n");

        List<Service> services = new ArrayList<>();
        services.add(new Service("db",    "postgres:15",  List.of(),              List.of("5432:5432")));
        services.add(new Service("cache", "redis:7",      List.of(),              List.of("6379:6379")));
        services.add(new Service("api",   "my-api:1.0",   List.of("db", "cache"), List.of("8080:8080")));
        services.add(new Service("web",   "nginx:latest", List.of("api"),         List.of("80:80", "443:443")));

        System.out.println("Servicios definidos:");
        for (Service s : services) {
            System.out.printf("  %-8s (%-20s) depends_on=%s%n",
                    s.name, s.image, s.dependsOn);
        }

        System.out.println("\nOrden de startup calculado:");
        List<String> order = topologicalSort(services);
        for (int i = 0; i < order.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, order.get(i));
        }

        // Test con ciclo
        System.out.println("\n=== Test ciclo de dependencias ===");
        List<Service> cyclic = new ArrayList<>();
        cyclic.add(new Service("a", "img:1", List.of("c"), List.of()));
        cyclic.add(new Service("b", "img:2", List.of("a"), List.of()));
        cyclic.add(new Service("c", "img:3", List.of("b"), List.of()));
        try {
            topologicalSort(cyclic);
        } catch (IllegalStateException e) {
            System.out.println("Error esperado: " + e.getMessage());
        }
    }
}
