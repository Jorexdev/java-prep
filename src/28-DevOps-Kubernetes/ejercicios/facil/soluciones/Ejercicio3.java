import java.util.*;

public class Ejercicio3 {

    enum PodState { PENDING, RUNNING, SUCCEEDED }

    static class Pod {
        String name;
        Map<String, String> labels;
        PodState state;

        Pod(String name, Map<String, String> labels, PodState state) {
            this.name   = name;
            this.labels = new LinkedHashMap<>(labels);
            this.state  = state;
        }

        boolean matchesSelector(Map<String, String> selector) {
            for (Map.Entry<String, String> e : selector.entrySet()) {
                if (!e.getValue().equals(labels.get(e.getKey()))) return false;
            }
            return true;
        }
    }

    static class KubeService {
        String name;
        Map<String, String> selector;
        int port;
        List<Pod> allPods;
        int roundRobinIdx = 0;

        KubeService(String name, Map<String, String> selector, int port, List<Pod> allPods) {
            this.name     = name;
            this.selector = selector;
            this.port     = port;
            this.allPods  = allPods;
        }

        List<Pod> endpoints() {
            return allPods.stream()
                    .filter(p -> p.state == PodState.RUNNING && p.matchesSelector(selector))
                    .collect(java.util.stream.Collectors.toList());
        }

        Pod route() {
            List<Pod> eps = endpoints();
            if (eps.isEmpty()) {
                System.out.println("  No hay endpoints disponibles para " + name);
                return null;
            }
            Pod selected = eps.get(roundRobinIdx % eps.size());
            roundRobinIdx++;
            return selected;
        }

        void printEndpoints() {
            System.out.printf("Service '%s' (port %d) selector=%s%n",
                    name, port, selector);
            List<Pod> eps = endpoints();
            System.out.printf("  Endpoints activos: %d%n", eps.size());
            eps.forEach(p -> System.out.printf("    → %-16s [RUNNING]%n", p.name));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Service Load Balancer ===\n");

        List<Pod> pods = new ArrayList<>();
        pods.add(new Pod("web-pod-1", Map.of("app", "web", "env", "prod"), PodState.RUNNING));
        pods.add(new Pod("web-pod-2", Map.of("app", "web", "env", "prod"), PodState.RUNNING));
        pods.add(new Pod("web-pod-3", Map.of("app", "web", "env", "prod"), PodState.PENDING)); // no sirve
        pods.add(new Pod("api-pod-1", Map.of("app", "api", "env", "prod"), PodState.RUNNING));

        KubeService webSvc = new KubeService("web-service",
                Map.of("app", "web"), 80, pods);
        KubeService apiSvc = new KubeService("api-service",
                Map.of("app", "api"), 8080, pods);

        webSvc.printEndpoints();
        System.out.println();
        apiSvc.printEndpoints();

        System.out.println("\n=== Round-Robin routing (6 requests a web-service) ===");
        for (int i = 1; i <= 6; i++) {
            Pod target = webSvc.route();
            System.out.printf("  Request %d → %s%n", i, target != null ? target.name : "N/A");
        }

        System.out.println("\n=== Routing con todos PENDING (sin endpoints) ===");
        List<Pod> noPods = List.of(
                new Pod("svc-pod-1", Map.of("app", "svc"), PodState.PENDING)
        );
        KubeService emptySvc = new KubeService("empty-service",
                Map.of("app", "svc"), 9090, noPods);
        emptySvc.printEndpoints();
        emptySvc.route();
    }
}
