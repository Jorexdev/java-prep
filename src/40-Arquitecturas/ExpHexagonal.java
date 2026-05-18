import java.util.*;

/**
 * Arquitectura Hexagonal (Ports & Adapters) con Java puro.
 *
 * Capas demostradas:
 *  - Dominio: Pedido (entity), PedidoRepository (port/interfaz)
 *  - Application: CrearPedidoUseCase (use case)
 *  - Adaptadores de entrada: PedidoControllerSimulado (HTTP), PedidoWorkerSimulado (queue)
 *  - Adaptadores de salida: PedidoRepositorioMemoria, PedidoRepositorioLog
 *
 * Regla clave: el dominio no importa NADA de fuera. Solo depende de sus
 * propias clases e interfaces. Los adaptadores sí pueden conocer el dominio.
 */
public class ExpHexagonal {

    // ═══════════════════════════════════════════════════════════════
    // DOMINIO — el núcleo. No importa frameworks, no importa infra.
    // ═══════════════════════════════════════════════════════════════

    // Entity: identidad propia (pedidoId)
    static class Pedido {
        private final String pedidoId;
        private final String producto;
        private final int cantidad;
        private String estado;

        Pedido(String pedidoId, String producto, int cantidad) {
            if (cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser positiva");
            if (producto == null || producto.isBlank()) throw new IllegalArgumentException("Producto requerido");
            this.pedidoId = pedidoId;
            this.producto = producto;
            this.cantidad = cantidad;
            this.estado = "PENDIENTE";
        }

        void confirmar() {
            if (!"PENDIENTE".equals(estado)) throw new IllegalStateException("Solo se puede confirmar un pedido PENDIENTE");
            this.estado = "CONFIRMADO";
        }

        void cancelar() {
            if ("ENVIADO".equals(estado)) throw new IllegalStateException("No se puede cancelar un pedido ENVIADO");
            this.estado = "CANCELADO";
        }

        String pedidoId() { return pedidoId; }
        String producto()  { return producto; }
        int cantidad()     { return cantidad; }
        String estado()    { return estado; }

        @Override
        public String toString() {
            return String.format("Pedido{id='%s', producto='%s', cantidad=%d, estado='%s'}",
                    pedidoId, producto, cantidad, estado);
        }
    }

    // Puerto de salida (OUTPUT PORT): interfaz definida por el dominio para persistencia
    // El dominio NO sabe cómo se implementa — puede ser JPA, MongoDB, en memoria, etc.
    interface PedidoRepository {
        void guardar(Pedido pedido);
        Optional<Pedido> buscarPorId(String pedidoId);
        List<Pedido> buscarTodos();
    }

    // ═══════════════════════════════════════════════════════════════
    // CAPA DE APLICACIÓN — Use Cases. Orquesta el dominio.
    // Solo depende de interfaces del dominio.
    // ═══════════════════════════════════════════════════════════════

    // Puerto de entrada (INPUT PORT): interfaz del use case
    interface CrearPedidoPort {
        Pedido crear(String producto, int cantidad);
    }

    interface ObtenerPedidoPort {
        Optional<Pedido> obtener(String pedidoId);
    }

    // Use Case: orquesta entidades y repositorios
    static class GestionPedidosUseCase implements CrearPedidoPort, ObtenerPedidoPort {
        private final PedidoRepository repositorio;
        private int contadorId = 1;

        GestionPedidosUseCase(PedidoRepository repositorio) {
            this.repositorio = repositorio;
        }

        @Override
        public Pedido crear(String producto, int cantidad) {
            String id = "PED-" + String.format("%03d", contadorId++);
            Pedido pedido = new Pedido(id, producto, cantidad);
            pedido.confirmar(); // regla de negocio: al crear se confirma automáticamente
            repositorio.guardar(pedido);
            System.out.printf("  [UseCase] Pedido creado y confirmado: %s%n", pedido);
            return pedido;
        }

        @Override
        public Optional<Pedido> obtener(String pedidoId) {
            return repositorio.buscarPorId(pedidoId);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ADAPTADORES DE SALIDA (Driven Adapters / Persistence Adapters)
    // Implementan los puertos del dominio.
    // ═══════════════════════════════════════════════════════════════

    // Adaptador 1: Repositorio en memoria (para tests o demo)
    static class PedidoRepositorioMemoria implements PedidoRepository {
        private final Map<String, Pedido> store = new LinkedHashMap<>();

        @Override
        public void guardar(Pedido pedido) {
            store.put(pedido.pedidoId(), pedido);
            System.out.printf("  [RepositorioMemoria] Guardado: %s%n", pedido.pedidoId());
        }

        @Override
        public Optional<Pedido> buscarPorId(String pedidoId) {
            return Optional.ofNullable(store.get(pedidoId));
        }

        @Override
        public List<Pedido> buscarTodos() {
            return new ArrayList<>(store.values());
        }
    }

    // Adaptador 2: Repositorio con log (simula otra implementación — misma interfaz)
    static class PedidoRepositorioLog implements PedidoRepository {
        private final List<String> log = new ArrayList<>();
        private final Map<String, Pedido> store = new LinkedHashMap<>();

        @Override
        public void guardar(Pedido pedido) {
            store.put(pedido.pedidoId(), pedido);
            String entry = String.format("INSERT pedido id=%s producto=%s cantidad=%d estado=%s",
                    pedido.pedidoId(), pedido.producto(), pedido.cantidad(), pedido.estado());
            log.add(entry);
            System.out.printf("  [RepositorioLog] SQL simulado: %s%n", entry);
        }

        @Override
        public Optional<Pedido> buscarPorId(String pedidoId) {
            System.out.printf("  [RepositorioLog] SQL simulado: SELECT * FROM pedidos WHERE id='%s'%n", pedidoId);
            return Optional.ofNullable(store.get(pedidoId));
        }

        @Override
        public List<Pedido> buscarTodos() {
            return new ArrayList<>(store.values());
        }

        List<String> obtenerLog() { return Collections.unmodifiableList(log); }
    }

    // ═══════════════════════════════════════════════════════════════
    // ADAPTADORES DE ENTRADA (Driving Adapters)
    // Llaman al use case. El dominio no los conoce.
    // ═══════════════════════════════════════════════════════════════

    // Adaptador HTTP simulado (en Spring sería @RestController)
    static class PedidoControllerSimulado {
        private final CrearPedidoPort crearPedido;
        private final ObtenerPedidoPort obtenerPedido;

        PedidoControllerSimulado(CrearPedidoPort crearPedido, ObtenerPedidoPort obtenerPedido) {
            this.crearPedido = crearPedido;
            this.obtenerPedido = obtenerPedido;
        }

        // Simula: POST /pedidos
        String handlePost(String producto, int cantidad) {
            System.out.printf("%n  [HTTP Controller] POST /pedidos {producto='%s', cantidad=%d}%n",
                    producto, cantidad);
            Pedido pedido = crearPedido.crear(producto, cantidad);
            return String.format("{\"pedidoId\":\"%s\",\"estado\":\"%s\"}",
                    pedido.pedidoId(), pedido.estado());
        }

        // Simula: GET /pedidos/{id}
        String handleGet(String pedidoId) {
            System.out.printf("%n  [HTTP Controller] GET /pedidos/%s%n", pedidoId);
            return obtenerPedido.obtener(pedidoId)
                    .map(p -> String.format("{\"pedidoId\":\"%s\",\"producto\":\"%s\",\"estado\":\"%s\"}",
                            p.pedidoId(), p.producto(), p.estado()))
                    .orElse("{\"error\":\"Pedido no encontrado\"}");
        }
    }

    // Adaptador Queue simulado (en Spring sería @KafkaListener)
    static class PedidoWorkerSimulado {
        private final CrearPedidoPort crearPedido;

        PedidoWorkerSimulado(CrearPedidoPort crearPedido) {
            this.crearPedido = crearPedido;
        }

        // Simula: @KafkaListener(topics = "pedidos-entrantes")
        void handleMensajeKafka(String mensajeJson) {
            System.out.printf("%n  [Kafka Worker] Mensaje recibido: %s%n", mensajeJson);
            // Parseo simulado del JSON
            String producto = mensajeJson.contains("laptop") ? "laptop" : "mouse";
            int cantidad = 1;
            crearPedido.crear(producto, cantidad);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN: ensambla la arquitectura con distintos adaptadores
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  ARQUITECTURA HEXAGONAL — Java puro");
        System.out.println("═".repeat(65));

        // ── Demo 1: HTTP Controller + Repositorio en Memoria ──────────
        System.out.println("\n── Demo 1: Adaptador HTTP + Repositorio en Memoria ──────────");

        PedidoRepositorioMemoria repoMemoria = new PedidoRepositorioMemoria();
        GestionPedidosUseCase useCase1 = new GestionPedidosUseCase(repoMemoria);
        PedidoControllerSimulado controller = new PedidoControllerSimulado(useCase1, useCase1);

        String respuesta1 = controller.handlePost("laptop", 2);
        System.out.printf("  → Response HTTP: %s%n", respuesta1);

        String respuesta2 = controller.handleGet("PED-001");
        System.out.printf("  → Response HTTP: %s%n", respuesta2);

        String respuesta3 = controller.handleGet("PED-999");
        System.out.printf("  → Response HTTP: %s%n", respuesta3);

        // ── Demo 2: Mismo UseCase, distinto adaptador de persistencia ─
        System.out.println("\n── Demo 2: Mismo UseCase + Repositorio Log (distinto adaptador) ──");

        PedidoRepositorioLog repoLog = new PedidoRepositorioLog();
        GestionPedidosUseCase useCase2 = new GestionPedidosUseCase(repoLog);
        PedidoControllerSimulado controller2 = new PedidoControllerSimulado(useCase2, useCase2);

        controller2.handlePost("teclado", 1);
        controller2.handlePost("monitor", 3);

        System.out.println("\n  Log de operaciones SQL simuladas:");
        repoLog.obtenerLog().forEach(entry -> System.out.println("    " + entry));

        // ── Demo 3: Adaptador Kafka + Repositorio en Memoria ─────────
        System.out.println("\n── Demo 3: Adaptador Kafka Worker (distinto adaptador de entrada) ──");

        PedidoRepositorioMemoria repoMemoria3 = new PedidoRepositorioMemoria();
        GestionPedidosUseCase useCase3 = new GestionPedidosUseCase(repoMemoria3);
        PedidoWorkerSimulado worker = new PedidoWorkerSimulado(useCase3);

        worker.handleMensajeKafka("{\"producto\":\"laptop\",\"cantidad\":1}");

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  CONCLUSIÓN");
        System.out.println("═".repeat(65));
        System.out.println("  El UseCase (dominio) es el mismo en los 3 demos.");
        System.out.println("  Lo que cambia son los adaptadores (HTTP, Log, Kafka).");
        System.out.println("  El dominio (Pedido, GestionPedidosUseCase) NO importa nada");
        System.out.println("  de los adaptadores — solo conoce sus propias interfaces.");
        System.out.println("  → Tests del dominio: solo instanciar PedidoRepositorioMemoria,");
        System.out.println("    sin Spring, sin BD, sin red. Ejecutan en microsegundos.");
        System.out.println("═".repeat(65));
    }
}
