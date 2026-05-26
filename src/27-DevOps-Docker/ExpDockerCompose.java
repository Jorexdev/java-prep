import java.util.*;

public class ExpDockerCompose {

    static class Service {
        private final String name;
        private final String image;
        private final List<String> ports;
        private final Map<String, String> envVars;
        private final List<String> dependsOn;
        private boolean healthy = false;

        Service(String name, String image, List<String> ports,
                Map<String, String> envVars, List<String> dependsOn) {
            this.name = name;
            this.image = image;
            this.ports = ports;
            this.envVars = envVars;
            this.dependsOn = dependsOn;
        }

        String getName()          { return name; }
        List<String> getDependsOn() { return dependsOn; }
        boolean isHealthy()       { return healthy; }
        void markHealthy()        { healthy = true; }

        void print() {
            System.out.printf("  service: %-10s  image=%-20s  ports=%s%n", name, image, ports);
            if (!envVars.isEmpty())  System.out.printf("             env=%s%n", envVars);
            if (!dependsOn.isEmpty()) System.out.printf("             depends_on=%s%n", dependsOn);
        }
    }

    static class Compose {
        private final Map<String, Service> services = new LinkedHashMap<>();

        void addService(Service s) {
            services.put(s.getName(), s);
        }

        // Topological sort (Kahn's algorithm) to determine start order
        List<String> resolveStartOrder() {
            Map<String, Integer> inDegree = new HashMap<>();
            Map<String, List<String>> dependents = new HashMap<>(); // who depends on me

            for (String name : services.keySet()) {
                inDegree.put(name, 0);
                dependents.put(name, new ArrayList<>());
            }
            for (Service s : services.values()) {
                for (String dep : s.getDependsOn()) {
                    inDegree.merge(s.getName(), 1, Integer::sum);
                    dependents.get(dep).add(s.getName());
                }
            }

            Queue<String> ready = new ArrayDeque<>();
            for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
                if (e.getValue() == 0) ready.add(e.getKey());
            }

            List<String> order = new ArrayList<>();
            while (!ready.isEmpty()) {
                String svc = ready.poll();
                order.add(svc);
                for (String dependent : dependents.get(svc)) {
                    int deg = inDegree.merge(dependent, -1, Integer::sum);
                    if (deg == 0) ready.add(dependent);
                }
            }
            return order;
        }

        void up() {
            System.out.println("\n[docker-compose up]");
            List<String> order = resolveStartOrder();
            System.out.println("  Orden de arranque resuelto: " + order);
            System.out.println("─".repeat(55));

            for (String name : order) {
                Service svc = services.get(name);
                // Verify all dependencies are healthy before starting
                boolean depsOk = svc.getDependsOn().stream()
                        .allMatch(dep -> services.get(dep).isHealthy());
                if (!depsOk) {
                    System.out.printf("  [WAIT] %s esperando a sus dependencias...%n", name);
                }
                System.out.printf("  [START] %-10s  (deps ok: %s)%n", name, depsOk);
                healthCheck(svc);
            }
        }

        private void healthCheck(Service svc) {
            // Simulated health check: always passes after startup
            svc.markHealthy();
            System.out.printf("  [HEALTH] %-10s → healthy ✓%n", svc.getName());
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(55));
        System.out.println("  DOCKER COMPOSE ORCHESTRATION — simulación");
        System.out.println("═".repeat(55));

        // Definición de servicios (equivalente a docker-compose.yml)
        Service redis = new Service("redis",
                "redis:7-alpine",
                List.of("6379:6379"),
                Collections.emptyMap(),
                Collections.emptyList());

        Service db = new Service("db",
                "postgres:15",
                List.of("5432:5432"),
                Map.of("POSTGRES_DB", "myapp", "POSTGRES_PASSWORD", "secret"),
                Collections.emptyList());

        Service app = new Service("app",
                "myapp:latest",
                List.of("8080:8080"),
                Map.of("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:5432/myapp",
                       "REDIS_URL", "redis://redis:6379"),
                List.of("db", "redis")); // app depende de db y redis

        System.out.println("\n[Servicios definidos]");
        System.out.println("─".repeat(55));
        redis.print();
        db.print();
        app.print();

        Compose compose = new Compose();
        compose.addService(redis);
        compose.addService(db);
        compose.addService(app);
        compose.up();

        System.out.println("\n── Conclusión ──");
        System.out.println("  depends_on garantiza que db y redis arrancan antes que app.");
        System.out.println("  El sort topológico calcula el orden; health checks validan la disponibilidad.");
    }
}
