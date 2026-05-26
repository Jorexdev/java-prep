import java.util.*;

public class ExpServiceDiscovery {

    static class PodEndpoint {
        private final String podName;
        private final String ip;
        private boolean healthy;

        PodEndpoint(String podName, String ip) {
            this.podName = podName;
            this.ip = ip;
            this.healthy = true;
        }

        void setHealthy(boolean healthy) { this.healthy = healthy; }
        boolean isHealthy()    { return healthy; }
        String getPodName()    { return podName; }
        String getIp()         { return ip; }
    }

    static class KubeService {
        private final String name;
        private final String clusterIp;
        private final List<PodEndpoint> allPods = new ArrayList<>();
        private final List<PodEndpoint> endpoints = new ArrayList<>();  // healthy pods only
        private int rrIndex = 0;  // round-robin cursor

        KubeService(String name, String clusterIp) {
            this.name = name;
            this.clusterIp = clusterIp;
        }

        void addPod(PodEndpoint pod) {
            allPods.add(pod);
            endpoints.add(pod);
        }

        // Round-robin over healthy endpoints only
        PodEndpoint loadBalance() {
            if (endpoints.isEmpty()) return null;
            PodEndpoint ep = endpoints.get(rrIndex % endpoints.size());
            rrIndex++;
            return ep;
        }

        List<PodEndpoint> getAllPods()   { return allPods; }
        List<PodEndpoint> getEndpoints(){ return endpoints; }
        String getName()                 { return name; }
        String getClusterIp()            { return clusterIp; }
    }

    // Watches pod health and syncs the Service endpoint list
    static class EndpointController {
        private final KubeService service;

        EndpointController(KubeService service) {
            this.service = service;
        }

        void sync() {
            List<PodEndpoint> healthy = service.getAllPods().stream()
                    .filter(PodEndpoint::isHealthy)
                    .toList();
            service.getEndpoints().clear();
            service.getEndpoints().addAll(healthy);

            List<String> names = healthy.stream().map(PodEndpoint::getPodName).toList();
            System.out.printf("[EndpointController] sync '%s': endpoints activos = %s%n",
                    service.getName(), names);
        }
    }

    static void sendRequest(KubeService svc, int requestNum) {
        PodEndpoint ep = svc.loadBalance();
        if (ep == null) {
            System.out.printf("  [Request %d] → %s (%s) — SIN ENDPOINTS (503)%n",
                    requestNum, svc.getName(), svc.getClusterIp());
        } else {
            System.out.printf("  [Request %d] → %s (%s) routed to pod=%s (%s)%n",
                    requestNum, svc.getName(), svc.getClusterIp(), ep.getPodName(), ep.getIp());
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  KUBERNETES SERVICE DISCOVERY — simulación");
        System.out.println("═".repeat(60));

        PodEndpoint p1 = new PodEndpoint("api-pod-1", "10.0.0.1");
        PodEndpoint p2 = new PodEndpoint("api-pod-2", "10.0.0.2");
        PodEndpoint p3 = new PodEndpoint("api-pod-3", "10.0.0.3");

        KubeService svc = new KubeService("api-service", "172.20.0.100");
        svc.addPod(p1);
        svc.addPod(p2);
        svc.addPod(p3);

        EndpointController ctrl = new EndpointController(svc);

        System.out.println("\n[Fase 1] 6 requests con 3 pods sanos (round-robin)");
        System.out.println("─".repeat(60));
        for (int i = 1; i <= 6; i++) sendRequest(svc, i);

        // Pod 2 falls unhealthy mid-traffic
        System.out.println("\n[Fase 2] api-pod-2 falla — EndpointController lo detecta");
        System.out.println("─".repeat(60));
        p2.setHealthy(false);
        ctrl.sync();   // removes p2 from endpoints

        System.out.println("\n[Fase 3] 6 requests tras eliminar pod-2 del pool");
        System.out.println("─".repeat(60));
        for (int i = 7; i <= 12; i++) sendRequest(svc, i);

        System.out.println("\n── Conclusión ──");
        System.out.println("  ClusterIP es la VIP estable; los endpoints detrás cambian dinámicamente.");
        System.out.println("  El EndpointController mantiene sincronizado el pool de pods sanos.");
    }
}
