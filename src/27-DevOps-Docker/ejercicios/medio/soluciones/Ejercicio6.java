import java.util.*;

public class Ejercicio6 {

    static class OverlayNetwork {
        String name;
        String subnet;
        // hostname → IP
        Map<String, String> dns = new LinkedHashMap<>();

        OverlayNetwork(String name, String subnet) {
            this.name   = name;
            this.subnet = subnet;
        }

        void registerContainer(String hostname, String ip) {
            dns.put(hostname, ip);
            System.out.printf("  [%s] registrado: %s → %s%n", name, hostname, ip);
        }

        boolean hasContainer(String hostname) {
            return dns.containsKey(hostname);
        }

        // Resuelve el hostname solo si el solicitante también está en esta red
        Optional<String> resolve(String requester, String hostname) {
            if (!hasContainer(requester)) return Optional.empty();
            return Optional.ofNullable(dns.get(hostname));
        }
    }

    static class NetworkRouter {
        List<OverlayNetwork> networks = new ArrayList<>();

        void addNetwork(OverlayNetwork n) {
            networks.add(n);
        }

        // Busca en qué red está el requester y resuelve desde esa misma red
        void resolve(String requester, String hostname) {
            for (OverlayNetwork net : networks) {
                if (net.hasContainer(requester)) {
                    Optional<String> ip = net.resolve(requester, hostname);
                    if (ip.isPresent()) {
                        System.out.printf("  %s → resolve('%s') en red '%s' → %s%n",
                                requester, hostname, net.name, ip.get());
                        return;
                    }
                    // Está en esta red pero el hostname no existe aquí
                    System.out.printf("  %s → resolve('%s') en red '%s' → NXDOMAIN " +
                                    "(hostname no pertenece a esta red)%n",
                            requester, hostname, net.name);
                    return;
                }
            }
            System.out.printf("  %s → resolve('%s') → ERROR (requester no registrado en ninguna red)%n",
                    requester, hostname);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Docker Overlay Networks con DNS ===\n");

        OverlayNetwork appNet = new OverlayNetwork("app-net", "10.0.1.0/24");
        OverlayNetwork monNet = new OverlayNetwork("monitoring-net", "10.0.2.0/24");

        System.out.println("Registrando containers:");
        appNet.registerContainer("api",        "10.0.1.2");
        appNet.registerContainer("db",         "10.0.1.3");
        monNet.registerContainer("prometheus", "10.0.2.2");
        monNet.registerContainer("grafana",    "10.0.2.3");

        NetworkRouter router = new NetworkRouter();
        router.addNetwork(appNet);
        router.addNetwork(monNet);

        System.out.println("\n=== Resoluciones DNS ===");
        // api → db: misma red, debe resolver
        router.resolve("api", "db");
        // api → prometheus: redes distintas, NXDOMAIN
        router.resolve("api", "prometheus");
        // prometheus → grafana: misma red monitoring, debe resolver
        router.resolve("prometheus", "grafana");
        // grafana → db: redes distintas, NXDOMAIN
        router.resolve("grafana", "db");
        // container no registrado
        router.resolve("unknown", "api");

        System.out.println("\n=== Resumen de redes ===");
        for (OverlayNetwork net : List.of(appNet, monNet)) {
            System.out.printf("  Red '%-15s' subnet=%-15s containers=%s%n",
                    net.name, net.subnet, net.dns.keySet());
        }
    }
}
