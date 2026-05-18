import java.util.*;

/**
 * Implementación del patrón Saga coreografiado con Java puro.
 *
 * Saga "Crear Pedido":
 *   PedidoService → InventarioService → PagoService → EnvioService
 *
 * Si un paso falla, se emiten eventos de compensación en orden inverso:
 *   PagoService falla → InventarioService libera stock → PedidoService cancela pedido
 *
 * Componentes:
 *  - EventBus: bus de eventos simple (publish/subscribe)
 *  - Cada servicio escucha sus eventos y emite el siguiente
 *  - Compensaciones: cada paso tiene su evento de rollback
 */
public class ExpSagaPattern {

    // ─────────────────────────────────────────────
    // EVENT BUS
    // ─────────────────────────────────────────────

    static class EventBus {
        private final Map<String, List<java.util.function.Consumer<Map<String, Object>>>> handlers =
                new HashMap<>();

        void suscribir(String evento, java.util.function.Consumer<Map<String, Object>> handler) {
            handlers.computeIfAbsent(evento, k -> new ArrayList<>()).add(handler);
        }

        void publicar(String evento, Map<String, Object> datos) {
            System.out.printf("  [EventBus] ← %s %s%n", evento, datos);
            List<java.util.function.Consumer<Map<String, Object>>> subs =
                    handlers.getOrDefault(evento, Collections.emptyList());
            for (var handler : subs) {
                handler.accept(datos);
            }
        }
    }

    // Helper para crear datos del evento
    static Map<String, Object> evento(Object... pares) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            map.put((String) pares[i], pares[i + 1]);
        }
        return map;
    }

    // ─────────────────────────────────────────────
    // SERVICIOS DE LA SAGA
    // ─────────────────────────────────────────────

    // ── Servicio de Pedidos ────────────────────────────────────────────

    static class PedidoService {
        private final EventBus bus;
        private final Map<String, String> pedidos = new HashMap<>(); // id → estado

        PedidoService(EventBus bus) {
            this.bus = bus;

            // Paso 1: Crear pedido
            bus.suscribir("CrearPedido", datos -> {
                String pedidoId = (String) datos.get("pedidoId");
                String producto = (String) datos.get("producto");
                System.out.printf("  [PedidoService] Creando pedido %s para producto '%s'%n",
                        pedidoId, producto);
                pedidos.put(pedidoId, "CREADO");
                // Emitir evento para que InventarioService reserve stock
                bus.publicar("PedidoCreado", evento(
                        "pedidoId", pedidoId,
                        "producto", producto,
                        "cantidad", datos.get("cantidad")));
            });

            // Compensación: cancelar pedido si algo falló después
            bus.suscribir("CancelarPedido", datos -> {
                String pedidoId = (String) datos.get("pedidoId");
                String motivo = (String) datos.get("motivo");
                pedidos.put(pedidoId, "CANCELADO");
                System.out.printf("  [PedidoService] COMPENSACIÓN: Pedido %s CANCELADO ← motivo: %s%n",
                        pedidoId, motivo);
            });

            // Completar pedido si todo fue bien
            bus.suscribir("EnvioCreado", datos -> {
                String pedidoId = (String) datos.get("pedidoId");
                pedidos.put(pedidoId, "COMPLETADO");
                System.out.printf("  [PedidoService] Pedido %s COMPLETADO exitosamente%n", pedidoId);
            });
        }

        void crearPedido(String pedidoId, String producto, int cantidad) {
            System.out.printf("%n[Saga] Iniciando: pedidoId=%s producto='%s' cantidad=%d%n",
                    pedidoId, producto, cantidad);
            System.out.println("─".repeat(65));
            bus.publicar("CrearPedido", evento(
                    "pedidoId", pedidoId,
                    "producto", producto,
                    "cantidad", cantidad));
        }

        String estadoPedido(String pedidoId) {
            return pedidos.getOrDefault(pedidoId, "DESCONOCIDO");
        }
    }

    // ── Servicio de Inventario ─────────────────────────────────────────

    static class InventarioService {
        private final EventBus bus;
        private final Map<String, Integer> stock = new HashMap<>();
        private final Map<String, Integer> reservas = new HashMap<>(); // pedidoId → cantidad reservada

        InventarioService(EventBus bus) {
            this.bus = bus;
            stock.put("laptop", 5);
            stock.put("mouse", 0); // sin stock para simular fallo

            // Paso 2: Reservar stock
            bus.suscribir("PedidoCreado", datos -> {
                String pedidoId = (String) datos.get("pedidoId");
                String producto = (String) datos.get("producto");
                int cantidad = (int) datos.get("cantidad");

                int stockDisponible = stock.getOrDefault(producto, 0);
                System.out.printf("  [InventarioService] Stock disponible de '%s': %d, solicitado: %d%n",
                        producto, stockDisponible, cantidad);

                if (stockDisponible >= cantidad) {
                    stock.put(producto, stockDisponible - cantidad);
                    reservas.put(pedidoId, cantidad);
                    System.out.printf("  [InventarioService] Stock reservado para pedido %s%n", pedidoId);
                    bus.publicar("StockReservado", evento(
                            "pedidoId", pedidoId,
                            "producto", producto,
                            "cantidad", cantidad));
                } else {
                    System.out.printf("  [InventarioService] FALLO: Stock insuficiente para '%s'%n", producto);
                    // Compensación directa hacia PedidoService
                    bus.publicar("CancelarPedido", evento(
                            "pedidoId", pedidoId,
                            "motivo", "Stock insuficiente de: " + producto));
                }
            });

            // Compensación: liberar stock si el pago falló
            bus.suscribir("LiberarStock", datos -> {
                String pedidoId = (String) datos.get("pedidoId");
                String producto = (String) datos.get("producto");
                Integer cantidad = reservas.remove(pedidoId);
                if (cantidad != null) {
                    stock.merge(producto, cantidad, Integer::sum);
                    System.out.printf("  [InventarioService] COMPENSACIÓN: Stock liberado para pedido %s " +
                            "(producto '%s' +%d)%n", pedidoId, producto, cantidad);
                }
                // Propagar compensación hacia PedidoService
                bus.publicar("CancelarPedido", evento(
                        "pedidoId", pedidoId,
                        "motivo", (String) datos.get("motivo")));
            });
        }
    }

    // ── Servicio de Pagos ──────────────────────────────────────────────

    static class PagoService {
        private final EventBus bus;
        private boolean simularFalloPago = false;

        PagoService(EventBus bus) {
            this.bus = bus;

            // Paso 3: Procesar pago
            bus.suscribir("StockReservado", datos -> {
                String pedidoId = (String) datos.get("pedidoId");
                String producto = (String) datos.get("producto");
                System.out.printf("  [PagoService] Procesando pago para pedido %s...%n", pedidoId);

                if (simularFalloPago) {
                    System.out.printf("  [PagoService] FALLO: Pago rechazado (fondos insuficientes)%n");
                    // Compensación: liberar el stock reservado
                    bus.publicar("LiberarStock", evento(
                            "pedidoId", pedidoId,
                            "producto", producto,
                            "cantidad", datos.get("cantidad"),
                            "motivo", "Pago rechazado"));
                } else {
                    System.out.printf("  [PagoService] Pago aprobado para pedido %s%n", pedidoId);
                    bus.publicar("PagoCompletado", evento(
                            "pedidoId", pedidoId,
                            "producto", producto,
                            "cantidad", datos.get("cantidad")));
                }
            });
        }

        void activarFalloPago() { simularFalloPago = true; }
    }

    // ── Servicio de Envío ──────────────────────────────────────────────

    static class EnvioService {
        private final EventBus bus;

        EnvioService(EventBus bus) {
            this.bus = bus;

            // Paso 4: Crear envío
            bus.suscribir("PagoCompletado", datos -> {
                String pedidoId = (String) datos.get("pedidoId");
                String envioId = "ENV-" + pedidoId;
                System.out.printf("  [EnvioService] Creando envío %s para pedido %s%n",
                        envioId, pedidoId);
                bus.publicar("EnvioCreado", evento(
                        "pedidoId", pedidoId,
                        "envioId", envioId));
            });
        }
    }

    // ─────────────────────────────────────────────
    // MAIN: demo de Saga exitosa y Saga con compensación
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  SAGA PATTERN (Coreografía) — Java puro");
        System.out.println("═".repeat(65));

        // ── Escenario 1: Saga exitosa ──────────────────────────────────
        System.out.println("\n══ ESCENARIO 1: Saga exitosa (todos los pasos OK) ══");

        EventBus bus1 = new EventBus();
        PedidoService pedidoSvc1 = new PedidoService(bus1);
        new InventarioService(bus1);
        new PagoService(bus1);
        new EnvioService(bus1);

        pedidoSvc1.crearPedido("P-001", "laptop", 2);
        System.out.printf("%n→ Estado final del pedido P-001: %s%n",
                pedidoSvc1.estadoPedido("P-001"));

        // ── Escenario 2: Saga con compensación — pago falla ───────────
        System.out.println("\n\n══ ESCENARIO 2: Pago falla → compensaciones en cascada ══");

        EventBus bus2 = new EventBus();
        PedidoService pedidoSvc2 = new PedidoService(bus2);
        new InventarioService(bus2);
        PagoService pagoSvc = new PagoService(bus2);
        new EnvioService(bus2);

        pagoSvc.activarFalloPago(); // simular pago rechazado
        pedidoSvc2.crearPedido("P-002", "laptop", 1);
        System.out.printf("%n→ Estado final del pedido P-002: %s%n",
                pedidoSvc2.estadoPedido("P-002"));

        // ── Escenario 3: Stock insuficiente ───────────────────────────
        System.out.println("\n\n══ ESCENARIO 3: Stock insuficiente → compensación inmediata ══");

        EventBus bus3 = new EventBus();
        PedidoService pedidoSvc3 = new PedidoService(bus3);
        new InventarioService(bus3);
        new PagoService(bus3);
        new EnvioService(bus3);

        pedidoSvc3.crearPedido("P-003", "mouse", 1); // mouse tiene stock=0
        System.out.printf("%n→ Estado final del pedido P-003: %s%n",
                pedidoSvc3.estadoPedido("P-003"));

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN SAGA COREOGRAFÍA");
        System.out.println("═".repeat(65));
        System.out.println("  + Cada servicio es autónomo y reactivo a eventos");
        System.out.println("  + No hay orquestador central → bajo acoplamiento");
        System.out.println("  - Difícil trazar el flujo completo (requiere distributed tracing)");
        System.out.println("  - Las compensaciones deben implementarse en cada servicio");
        System.out.println("  Clave: cada paso debe ser IDEMPOTENTE y la compensación");
        System.out.println("         debe ser el inverso lógico del paso original");
        System.out.println("═".repeat(65));
    }
}
