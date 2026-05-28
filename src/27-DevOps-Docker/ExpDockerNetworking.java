import java.util.*;

public class ExpDockerNetworking {

    enum NetworkDriver { BRIDGE, OVERLAY, HOST, NONE }

    static class Container {
        private final String name;
        private final String ip;

        Container(String name, String ip) {
            this.name = name;
            this.ip   = ip;
        }

        String getName() { return name; }
        String getIp()   { return ip; }

        @Override public String toString() {
            return String.format("%-15s (%s)", name, ip);
        }
    }

    // Simula una red Docker: contiene contenedores y resuelve nombres por DNS interno
    static class DockerNetwork {
        private final String name;
        private final NetworkDriver driver;
        private final Map<String, Container> containers = new LinkedHashMap<>();
        // DNS interno: nombre de contenedor → IP
        private final Map<String, String> dnsTable = new LinkedHashMap<>();

        DockerNetwork(String name, NetworkDriver driver) {
            this.name   = name;
            this.driver = driver;
        }

        void connect(Container c) {
            containers.put(c.getName(), c);
            dnsTable.put(c.getName(), c.getIp());
            System.out.printf("  [NET %-12s] conectado: %s%n", name, c);
        }

        void disconnect(String containerName) {
            Container removed = containers.remove(containerName);
            dnsTable.remove(containerName);
            if (removed != null)
                System.out.printf("  [NET %-12s] desconectado: %s%n", name, removed);
        }

        // Resolucion DNS interna: como el daemon de Docker resuelve nombres
        // Equivalente: ping <container-name> dentro de un contenedor en la misma red
        String resolveDns(String containerName) {
            return dnsTable.getOrDefault(containerName, "<no resuelto>");
        }

        // Comprueba si dos contenedores pueden comunicarse entre sí
        boolean canReach(String from, String to) {
            return containers.containsKey(from) && containers.containsKey(to);
        }

        void printDnsTable() {
            System.out.printf("  DNS interno de red '%s' (driver=%s):%n", name, driver);
            if (dnsTable.isEmpty()) {
                System.out.println("    (vacío)");
            } else {
                dnsTable.forEach((host, ip) ->
                        System.out.printf("    %-15s → %s%n", host, ip));
            }
        }

        String getName()         { return name; }
        NetworkDriver getDriver(){ return driver; }
    }

    // Simula el daemon Docker: gestiona redes y conecta contenedores
    static class DockerDaemon {
        private final Map<String, DockerNetwork> networks = new LinkedHashMap<>();

        DockerNetwork createNetwork(String name, NetworkDriver driver) {
            // docker network create --driver bridge mynet
            DockerNetwork net = new DockerNetwork(name, driver);
            networks.put(name, net);
            System.out.printf("  [DAEMON] Red creada: '%s' driver=%s%n", name, driver);
            return net;
        }

        DockerNetwork getNetwork(String name) { return networks.get(name); }
    }

    // ── 1. BRIDGE NETWORK ──────────────────────────────────────────────────────
    static void bridgeNetwork() {
        System.out.println("\n── 1. Bridge Network (default) ──");
        System.out.println("─".repeat(60));
        /*
         * docker network create --driver bridge app-net
         * docker run --network app-net --name db   postgres:15
         * docker run --network app-net --name api  myapi:1.0
         * docker run --network app-net --name cache redis:7
         *
         * Los contenedores en la misma bridge se ven por nombre.
         * El daemon embebe un servidor DNS que resuelve nombres de contenedor → IP.
         */
        DockerDaemon daemon = new DockerDaemon();
        DockerNetwork bridge = daemon.createNetwork("app-net", NetworkDriver.BRIDGE);

        Container db    = new Container("db",    "172.18.0.2");
        Container api   = new Container("api",   "172.18.0.3");
        Container cache = new Container("cache", "172.18.0.4");

        bridge.connect(db);
        bridge.connect(api);
        bridge.connect(cache);

        System.out.println();
        bridge.printDnsTable();

        // api puede hablar con db por nombre — sin conocer la IP
        System.out.println("\n  Resolución DNS desde 'api':");
        for (String target : List.of("db", "cache", "unknown")) {
            String resolved = bridge.resolveDns(target);
            System.out.printf("    api → ping %-10s resuelve: %s%n", target, resolved);
        }

        System.out.println("\n  Alcance entre contenedores:");
        System.out.printf("    api ↔ db:    %s%n", bridge.canReach("api", "db")    ? "SI" : "NO");
        System.out.printf("    api ↔ cache: %s%n", bridge.canReach("api", "cache") ? "SI" : "NO");
        System.out.println("  (bridge aísla del host pero permite comunicación entre contenedores)");
    }

    // ── 2. OVERLAY NETWORK ────────────────────────────────────────────────────
    static void overlayNetwork() {
        System.out.println("\n── 2. Overlay Network (multi-host, Docker Swarm) ──");
        System.out.println("─".repeat(60));
        /*
         * docker network create --driver overlay --attachable swarm-net
         *
         * Overlay encapsula tráfico en VXLAN para conectar contenedores
         * en DISTINTOS hosts físicos como si estuvieran en la misma L2.
         * Requiere Docker Swarm o un key-value store externo (etcd/consul).
         *
         * Subnet lógica de overlay: 10.0.0.0/24
         *   node-1 (host: 192.168.1.10) → api:10.0.0.2
         *   node-2 (host: 192.168.1.11) → worker:10.0.0.3
         */
        DockerDaemon daemon = new DockerDaemon();
        DockerNetwork overlay = daemon.createNetwork("swarm-net", NetworkDriver.OVERLAY);

        // Contenedores en distintos hosts físicos pero misma red overlay
        Container apiNode1    = new Container("api",    "10.0.0.2");   // host1
        Container workerNode2 = new Container("worker", "10.0.0.3");   // host2

        overlay.connect(apiNode1);
        overlay.connect(workerNode2);

        System.out.println();
        overlay.printDnsTable();

        System.out.println("\n  api (host1) → worker (host2): alcance=" +
                (overlay.canReach("api", "worker") ? "SI (VXLAN tunnel)" : "NO"));
        System.out.println("  Tráfico encapsulado en UDP 4789 (VXLAN) entre hosts físicos.");
    }

    // ── 3. HOST NETWORK ───────────────────────────────────────────────────────
    static void hostNetwork() {
        System.out.println("\n── 3. Host Network (sin aislamiento de red) ──");
        System.out.println("─".repeat(60));
        /*
         * docker run --network host nginx:latest
         *
         * El contenedor comparte la interfaz de red del host directamente.
         * NO hay NAT, NO hay bridge virtual. El puerto 80 del contenedor
         * ES el puerto 80 del host — sin mapeo -p.
         *
         * Ventaja:  menor latencia (sin overhead de NAT/bridge)
         * Riesgo:   cualquier puerto abierto en el contenedor queda expuesto
         *           directamente en el host → sin aislamiento de red.
         */
        System.out.println("  docker run --network host nginx:latest");
        System.out.println("  → nginx escucha en 0.0.0.0:80 DEL HOST (sin NAT)");
        System.out.println();

        // Simulación: host network = sin capa de red intermedia
        DockerDaemon daemon = new DockerDaemon();
        DockerNetwork hostNet = daemon.createNetwork("host", NetworkDriver.HOST);

        Container nginx = new Container("nginx", "192.168.1.10"); // IP real del host
        hostNet.connect(nginx);

        System.out.println();
        hostNet.printDnsTable();
        System.out.println("  Sin DNS interno — el contenedor usa directamente /etc/resolv.conf del host.");
        System.out.println("  Aislamiento de red: NINGUNO.");
    }

    // ── 4. COMUNICACIÓN ENTRE REDES (docker network connect) ─────────────────
    static void multiNetworkContainer() {
        System.out.println("\n── 4. Contenedor en múltiples redes ──");
        System.out.println("─".repeat(60));
        /*
         * Un contenedor puede pertenecer a varias redes simultáneamente.
         * El gateway actúa como proxy entre frontend-net y backend-net.
         *
         * docker network connect backend-net gateway
         * → gateway tiene IPs en ambas redes y puede enrutar tráfico.
         */
        DockerDaemon daemon = new DockerDaemon();
        DockerNetwork frontendNet = daemon.createNetwork("frontend-net", NetworkDriver.BRIDGE);
        DockerNetwork backendNet  = daemon.createNetwork("backend-net",  NetworkDriver.BRIDGE);

        Container frontend = new Container("frontend", "172.19.0.2");
        Container gateway  = new Container("gateway",  "172.19.0.3");  // en frontend-net
        Container db       = new Container("db",       "172.20.0.2");

        frontendNet.connect(frontend);
        frontendNet.connect(gateway);
        backendNet.connect(gateway);   // docker network connect backend-net gateway
        backendNet.connect(db);

        System.out.println();
        System.out.println("  frontend-net:");
        frontendNet.printDnsTable();
        System.out.println("  backend-net:");
        backendNet.printDnsTable();

        System.out.printf("%n  frontend → gateway: %s%n",
                frontendNet.canReach("frontend", "gateway") ? "ALCANZABLE" : "NO");
        System.out.printf("  gateway  → db:      %s%n",
                backendNet.canReach("gateway", "db") ? "ALCANZABLE" : "NO");
        System.out.printf("  frontend → db:      %s (redes distintas — sin ruta directa)%n",
                frontendNet.canReach("frontend", "db") ? "ALCANZABLE" : "NO");
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  DOCKER NETWORKING — simulación");
        System.out.println("═".repeat(60));

        bridgeNetwork();
        overlayNetwork();
        hostNetwork();
        multiNetworkContainer();

        System.out.println("\n── Conclusión ──");
        System.out.println("  bridge:  contenedores en la misma host, DNS interno por nombre.");
        System.out.println("  overlay: multi-host (Swarm), encapsulación VXLAN.");
        System.out.println("  host:    sin aislamiento, máximo rendimiento de red.");
        System.out.println("  Un contenedor en N redes actúa como router entre ellas.");
    }
}
