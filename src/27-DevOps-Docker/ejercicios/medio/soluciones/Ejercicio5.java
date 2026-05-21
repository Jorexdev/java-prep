import java.util.*;

public class Ejercicio5 {

    static class Container {
        String name;

        Container(String name) {
            this.name = name;
        }

        @Override
        public String toString() { return name; }
    }

    static class DockerNetwork {
        String name;
        Set<Container> containers = new LinkedHashSet<>();

        DockerNetwork(String name) {
            this.name = name;
        }

        void connect(Container... cs) {
            containers.addAll(Arrays.asList(cs));
        }

        boolean contains(Container c) {
            return containers.stream().anyMatch(x -> x.name.equals(c.name));
        }
    }

    static class NetworkBridge {
        List<DockerNetwork> networks = new ArrayList<>();

        void addNetwork(DockerNetwork n) {
            networks.add(n);
        }

        boolean canCommunicate(Container c1, Container c2) {
            for (DockerNetwork net : networks) {
                if (net.contains(c1) && net.contains(c2)) return true;
            }
            return false;
        }

        void printNetworks() {
            System.out.println("=== Docker Networks ===");
            for (DockerNetwork n : networks) {
                System.out.printf("  Network '%-12s': %s%n", n.name, n.containers);
            }
        }

        void check(Container c1, Container c2) {
            boolean ok = canCommunicate(c1, c2);
            System.out.printf("  %-12s ↔ %-12s : %s%n",
                    c1.name, c2.name, ok ? "CONECTADOS" : "AISLADOS");
        }
    }

    public static void main(String[] args) {
        Container web     = new Container("web");
        Container api     = new Container("api");
        Container db      = new Container("db");
        Container monitor = new Container("monitor");

        DockerNetwork frontend = new DockerNetwork("frontend");
        frontend.connect(web, api);

        DockerNetwork backend = new DockerNetwork("backend");
        backend.connect(api, db);

        DockerNetwork isolated = new DockerNetwork("isolated");
        isolated.connect(monitor);

        NetworkBridge bridge = new NetworkBridge();
        bridge.addNetwork(frontend);
        bridge.addNetwork(backend);
        bridge.addNetwork(isolated);

        bridge.printNetworks();

        System.out.println("\n=== Comprobaciones de comunicación ===");
        bridge.check(web,     api);      // true  (comparten frontend)
        bridge.check(web,     db);       // false (no comparten red)
        bridge.check(api,     db);       // true  (comparten backend)
        bridge.check(web,     monitor);  // false
        bridge.check(api,     monitor);  // false
        bridge.check(db,      monitor);  // false
    }
}
