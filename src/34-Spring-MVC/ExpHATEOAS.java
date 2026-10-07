import java.util.*;

// Simula HATEOAS (Hypermedia As The Engine Of Application State) con HAL.
// En producción: spring-boot-starter-hateoas — EntityModel, CollectionModel, WebMvcLinkBuilder
//
// Richardson Maturity Model:
//   Level 0 — un endpoint, un verbo: POST /api con "action":"getPedido" en body
//   Level 1 — recursos en URIs:       GET /pedidos/42     POST /pedidos
//   Level 2 — verbos HTTP correctos:  GET(leer) POST(crear) PUT/PATCH(modificar) DELETE(borrar) + status codes
//   Level 3 — HATEOAS:                la respuesta incluye qué acciones son posibles desde ese estado

public class ExpHATEOAS {

    // ── Link ──────────────────────────────────────────────────────────────────

    // HAL: cada link tiene al menos "href". "method" es una extensión común.
    // En Spring HATEOAS: Link.of(href, relation)
    record Link(String href, String method) {
        // toString simula el fragmento HAL: "pagar": { "href": "/pedidos/42/pago", "method": "POST" }
        String toHal() {
            return "{ \"href\": \"" + href + "\", \"method\": \"" + method + "\" }";
        }
    }

    // ── EntityModel ────────────────────────────────────────────────────────────

    // En Spring HATEOAS: EntityModel.of(objeto).add(link1, link2, ...)
    // Envuelve un recurso y añade _links dinámicamente según el estado del recurso.
    static class EntityModel<T> {
        private final T content;
        // "relation" → Link. La relación "self" es obligatoria en HAL.
        private final Map<String, Link> links = new LinkedHashMap<>();

        EntityModel(T content) { this.content = content; }

        EntityModel<T> add(String relation, Link link) {
            links.put(relation, link);
            return this;
        }

        void print(String titulo) {
            System.out.println(titulo);
            System.out.println("{");
            printContent();
            System.out.println("  \"_links\": {");
            int i = 0;
            for (Map.Entry<String, Link> e : links.entrySet()) {
                String comma = ++i < links.size() ? "," : "";
                System.out.println("    \"" + e.getKey() + "\": " + e.getValue().toHal() + comma);
            }
            System.out.println("  }");
            System.out.println("}");
        }

        private void printContent() {
            // imprime el contenido delegando al record si es conocido
            if (content instanceof Pedido p) {
                System.out.println("  \"id\": " + p.id() + ",");
                System.out.println("  \"estado\": \"" + p.estado() + "\",");
                System.out.println("  \"total\": " + p.total() + ",");
            }
        }
    }

    // ── CollectionModel ────────────────────────────────────────────────────────

    // En Spring HATEOAS: CollectionModel.of(lista).add(selfLink)
    static class CollectionModel<T> {
        private final List<EntityModel<T>> items;
        private Link selfLink;

        CollectionModel(List<EntityModel<T>> items) { this.items = items; }

        CollectionModel<T> withSelf(String href) {
            this.selfLink = new Link(href, "GET");
            return this;
        }

        void print(String titulo) {
            System.out.println(titulo);
            System.out.println("{");
            System.out.println("  \"_embedded\": {");
            System.out.println("    \"pedidos\": [");
            for (int i = 0; i < items.size(); i++) {
                System.out.print("      { ... pedido " + (i + 1) + " con _links ... }");
                System.out.println(i < items.size() - 1 ? "," : "");
            }
            System.out.println("    ]");
            System.out.println("  },");
            System.out.println("  \"_links\": {");
            if (selfLink != null) {
                System.out.println("    \"self\": " + selfLink.toHal());
            }
            System.out.println("  }");
            System.out.println("}");
        }
    }

    // ── Modelo de dominio ──────────────────────────────────────────────────────

    enum EstadoPedido { PENDIENTE, PAGADO, ENVIADO, ENTREGADO, CANCELADO }

    record Pedido(int id, EstadoPedido estado, double total) {}

    // ── HalController (simula @RestController con Spring HATEOAS) ─────────────

    // En Spring: linkTo(methodOn(PedidoController.class).getPedido(id)).withSelfRel()
    // Aquí construimos las URLs manualmente para que el ejemplo sea ejecutable sin Spring.
    static class PedidoController {

        private static final String BASE = "/pedidos";

        // GET /pedidos/{id}
        EntityModel<Pedido> getPedido(int id) {
            Pedido pedido = cargarPedido(id);

            // Los links disponibles dependen del ESTADO actual del pedido.
            // Esto es la esencia de HATEOAS: el servidor decide qué acciones son válidas.
            EntityModel<Pedido> model = new EntityModel<>(pedido)
                    .add("self", new Link(BASE + "/" + id, "GET"))
                    .add("pedidos", new Link(BASE, "GET"));

            switch (pedido.estado()) {
                case PENDIENTE -> {
                    // Desde PENDIENTE el cliente puede pagar o cancelar
                    model.add("pagar",    new Link(BASE + "/" + id + "/pago", "POST"));
                    model.add("cancelar", new Link(BASE + "/" + id + "/cancelar", "DELETE"));
                }
                case PAGADO -> {
                    // Desde PAGADO ya no se puede pagar de nuevo; solo cancelar antes del envío
                    model.add("cancelar", new Link(BASE + "/" + id + "/cancelar", "DELETE"));
                }
                case ENVIADO -> {
                    // En tránsito: solo consultar seguimiento
                    model.add("seguimiento", new Link(BASE + "/" + id + "/seguimiento", "GET"));
                }
                case ENTREGADO, CANCELADO -> {
                    // Estado terminal: solo self (sin más acciones posibles)
                }
            }

            return model;
        }

        // GET /pedidos
        CollectionModel<Pedido> getPedidos() {
            List<EntityModel<Pedido>> items = List.of(
                    getPedido(42),
                    getPedido(43)
            );
            return new CollectionModel<>(items).withSelf(BASE);
        }

        private Pedido cargarPedido(int id) {
            // Datos de muestra
            return switch (id) {
                case 42 -> new Pedido(42, EstadoPedido.PENDIENTE, 129.99);
                case 43 -> new Pedido(43, EstadoPedido.ENVIADO,   49.50);
                default -> new Pedido(id, EstadoPedido.ENTREGADO, 0.0);
            };
        }
    }

    // ── Level 0-2 comparación ─────────────────────────────────────────────────

    static void printRichardsonLevels() {
        System.out.println("─ Richardson Maturity Model ─\n");

        System.out.println("Level 0 — RPC sobre HTTP (un endpoint, acción en body):");
        System.out.println("  POST /api");
        System.out.println("  { \"action\": \"getPedido\", \"id\": 42 }");
        System.out.println("  // URI no describe el recurso; el verbo HTTP no aporta semántica\n");

        System.out.println("Level 1 — Recursos en URIs:");
        System.out.println("  GET  /api/pedidos/42");
        System.out.println("  POST /api/pedidos");
        System.out.println("  // URIs descriptivas, pero aún se usa POST para todo\n");

        System.out.println("Level 2 — Verbos HTTP + Status Codes:");
        System.out.println("  GET    /pedidos/42      → 200 OK");
        System.out.println("  POST   /pedidos         → 201 Created + Location: /pedidos/99");
        System.out.println("  PATCH  /pedidos/42      → 200 OK");
        System.out.println("  DELETE /pedidos/42      → 204 No Content");
        System.out.println("  GET    /pedidos/999     → 404 Not Found");
        System.out.println("  // El cliente necesita conocer las URLs de antemano\n");

        System.out.println("Level 3 — HATEOAS:");
        System.out.println("  El servidor devuelve qué acciones son posibles desde el estado actual.");
        System.out.println("  El cliente sigue links → no está acoplado a las URLs.\n");
    }

    // ── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═".repeat(65));
        System.out.println("  HATEOAS — HAL + Richardson Maturity Model");
        System.out.println("═".repeat(65));
        System.out.println();

        printRichardsonLevels();

        PedidoController controller = new PedidoController();

        System.out.println("─ GET /pedidos/42  (estado: PENDIENTE → links: pagar, cancelar) ─\n");
        controller.getPedido(42).print("Respuesta HAL:");

        System.out.println();
        System.out.println("─ GET /pedidos/43  (estado: ENVIADO → links: seguimiento) ─\n");
        controller.getPedido(43).print("Respuesta HAL:");

        System.out.println();
        System.out.println("─ GET /pedidos  (CollectionModel) ─\n");
        controller.getPedidos().print("Respuesta HAL (colección):");

        System.out.println();
        System.out.println("─ Cuándo usar HATEOAS ─");
        System.out.println("  SI:  APIs públicas que evolucionan — los clientes descubren endpoints dinámicamente");
        System.out.println("       Flujos de estado complejos — el servidor controla qué transiciones son válidas");
        System.out.println("       APIs que quieren desacoplar cliente/servidor (versiones independientes)");
        System.out.println("  NO:  APIs internas entre microservicios donde el acoplamiento es aceptable");
        System.out.println("       Equipos pequeños que no necesitan la complejidad extra");
        System.out.println("       Clientes que ya conocen el contrato (SDK generado desde OpenAPI)");
        System.out.println("\n" + "═".repeat(65));
    }
}
