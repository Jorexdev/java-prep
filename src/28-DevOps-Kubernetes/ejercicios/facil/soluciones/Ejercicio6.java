import java.util.*;

public class Ejercicio6 {

    enum PodState { PENDING, RUNNING }

    static class Pod {
        String name;
        String namespace;
        PodState state;
        Map<String, String> labels;

        Pod(String name, String namespace, Map<String, String> labels) {
            this.name      = name;
            this.namespace = namespace;
            this.labels    = labels;
            this.state     = PodState.RUNNING;
        }
    }

    static class KubeService {
        String name;
        String namespace;
        Map<String, String> selector;

        KubeService(String name, String namespace, Map<String, String> selector) {
            this.name      = name;
            this.namespace = namespace;
            this.selector  = selector;
        }

        boolean exposesPod(Pod pod) {
            if (!pod.namespace.equals(namespace)) return false;
            for (Map.Entry<String, String> e : selector.entrySet()) {
                if (!e.getValue().equals(pod.labels.get(e.getKey()))) return false;
            }
            return true;
        }
    }

    static class Namespace {
        String name;
        List<Pod> pods = new ArrayList<>();

        Namespace(String name) { this.name = name; }

        void addPod(Pod p) { pods.add(p); }

        boolean contains(Pod pod) {
            return pods.stream().anyMatch(p -> p.name.equals(pod.name));
        }
    }

    static class Cluster {
        List<Namespace>   namespaces = new ArrayList<>();
        List<KubeService> services   = new ArrayList<>();

        void addNamespace(Namespace ns) { namespaces.add(ns); }
        void addService(KubeService svc) { services.add(svc); }

        Namespace findNamespace(Pod pod) {
            return namespaces.stream()
                    .filter(ns -> ns.contains(pod))
                    .findFirst().orElse(null);
        }

        boolean canCommunicate(Pod p1, Pod p2) {
            // Misma namespace → sí
            Namespace ns1 = findNamespace(p1);
            Namespace ns2 = findNamespace(p2);
            if (ns1 != null && ns1 == ns2) return true;

            // Existe un Service que expone p2 accesible desde p1's namespace
            for (KubeService svc : services) {
                if (svc.exposesPod(p2)) return true;
            }
            return false;
        }

        void check(Pod p1, Pod p2) {
            boolean ok = canCommunicate(p1, p2);
            Namespace ns1 = findNamespace(p1);
            Namespace ns2 = findNamespace(p2);
            System.out.printf("  %-18s [ns=%s]  ↔  %-18s [ns=%s]  : %s%n",
                    p1.name, ns1 != null ? ns1.name : "?",
                    p2.name, ns2 != null ? ns2.name : "?",
                    ok ? "PERMITIDO" : "BLOQUEADO");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Namespace Isolation ===\n");

        Namespace frontend = new Namespace("frontend");
        Namespace backend  = new Namespace("backend");

        Pod webPod  = new Pod("web-pod",  "frontend", Map.of("app", "web"));
        Pod reactPod= new Pod("react-pod","frontend", Map.of("app", "react"));
        Pod apiPod  = new Pod("api-pod",  "backend",  Map.of("app", "api"));
        Pod dbPod   = new Pod("db-pod",   "backend",  Map.of("app", "db"));

        frontend.addPod(webPod);
        frontend.addPod(reactPod);
        backend.addPod(apiPod);
        backend.addPod(dbPod);

        // Service que expone api-pod al exterior (mismo namespace backend)
        KubeService apiSvc = new KubeService("api-service", "backend",
                Map.of("app", "api"));

        Cluster cluster = new Cluster();
        cluster.addNamespace(frontend);
        cluster.addNamespace(backend);
        cluster.addService(apiSvc);

        System.out.println("Namespaces:");
        System.out.println("  frontend: web-pod, react-pod");
        System.out.println("  backend:  api-pod, db-pod");
        System.out.println("Services: api-service expone api-pod (backend)");

        System.out.println("\n=== Comprobaciones de comunicación ===");
        cluster.check(webPod,   reactPod);  // misma ns → PERMITIDO
        cluster.check(apiPod,   dbPod);     // misma ns → PERMITIDO
        cluster.check(webPod,   apiPod);    // distinta ns pero hay Service → PERMITIDO
        cluster.check(webPod,   dbPod);     // distinta ns, sin Service → BLOQUEADO
        cluster.check(reactPod, dbPod);     // distinta ns, sin Service → BLOQUEADO
    }
}
