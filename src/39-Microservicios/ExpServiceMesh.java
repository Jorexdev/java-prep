import java.util.*;
import java.util.function.*;

/**
 * Simulación del patrón Service Mesh con sidecar proxy.
 *
 * Componentes:
 *  - Sidecar: proxy que intercepta TODAS las llamadas de un servicio
 *  - ControlPlane: distribuye reglas de routing a todos los sidecars
 *  - Capacidades del sidecar: retries, timeouts, circuit breaker, mTLS simulado
 *
 * Por qué sidecar: la lógica de resiliencia vive en el proxy, no en el código
 * de la app. La app no sabe que hay reintentos, mTLS ni circuit breakers.
 */
public class ExpServiceMesh {

    // ─────────────────────────────────────────────
    // ROUTING RULE (distribuida por el Control Plane)
    // ─────────────────────────────────────────────

    record RoutingRule(String destination, int maxRetries, long timeoutMs, int cbFailureThreshold) {}

    // ─────────────────────────────────────────────
    // CONTROL PLANE: distribuye configuración a sidecars
    // ─────────────────────────────────────────────

    static class ControlPlane {
        private final Map<String, RoutingRule> rules = new LinkedHashMap<>();
        private final List<Sidecar> sidecars = new ArrayList<>();

        void addRule(String destination, RoutingRule rule) {
            rules.put(destination, rule);
            System.out.printf("[ControlPlane] Regla añadida: %s → retries=%d timeout=%dms cb=%d%n",
                    destination, rule.maxRetries(), rule.timeoutMs(), rule.cbFailureThreshold());
        }

        void registerSidecar(Sidecar sidecar) {
            sidecars.add(sidecar);
        }

        // Push de configuración a todos los sidecars registrados (xDS protocol simulado)
        void pushConfig() {
            System.out.printf("%n[ControlPlane] Push de configuración a %d sidecars%n", sidecars.size());
            sidecars.forEach(s -> {
                rules.forEach(s::applyRule);
                System.out.printf("  → Sidecar '%s' actualizado%n", s.serviceName());
            });
        }

        RoutingRule getRule(String destination) {
            return rules.get(destination);
        }
    }

    // ─────────────────────────────────────────────
    // BACKEND SERVICE SIMULADO
    // ─────────────────────────────────────────────

    static class Service {
        private final String name;
        private int callCount = 0;
        private int failUntilCall = 0; // falla las primeras N llamadas

        Service(String name) { this.name = name; }

        void failFirstNCalls(int n) { failUntilCall = n; }

        String call(String endpoint) {
            callCount++;
            if (callCount <= failUntilCall) {
                throw new RuntimeException("Transient error (call #" + callCount + ")");
            }
            return String.format("{\"service\":\"%s\",\"endpoint\":\"%s\",\"call\":%d}",
                    name, endpoint, callCount);
        }

        String name() { return name; }
    }

    // ─────────────────────────────────────────────
    // SIDECAR PROXY
    // ─────────────────────────────────────────────

    static class Sidecar {
        private final String serviceName;
        private final Map<String, RoutingRule> rules = new LinkedHashMap<>();

        // Circuit breaker state por destino
        private final Map<String, Integer> cbFailures = new HashMap<>();
        private final Set<String> openCircuits = new HashSet<>();

        Sidecar(String serviceName) {
            this.serviceName = serviceName;
        }

        String serviceName() { return serviceName; }

        void applyRule(String destination, RoutingRule rule) {
            rules.put(destination, rule);
        }

        // Intercepta la llamada saliente, aplica retry + timeout + CB + mTLS
        String outbound(String destination, String endpoint, Supplier<String> call) {
            System.out.printf("%n  [Sidecar '%s'] → outbound a '%s'%s%n",
                    serviceName, destination, endpoint);

            // mTLS: simular verificación de certificado mutuo
            System.out.printf("  [Sidecar] mTLS: verificando certificado de '%s'... OK%n", destination);

            // Circuit breaker: rechazar si el circuito está abierto
            if (openCircuits.contains(destination)) {
                System.out.printf("  [Sidecar] Circuit OPEN para '%s' → rechazando%n", destination);
                return "{\"error\":\"circuit open — destination unavailable\"}";
            }

            RoutingRule rule = rules.getOrDefault(destination,
                    new RoutingRule(destination, 1, 1000, 3));

            int maxRetries = rule.maxRetries();
            long timeoutMs = rule.timeoutMs();

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                System.out.printf("  [Sidecar] Intento %d/%d (timeout=%dms)%n",
                        attempt, maxRetries, timeoutMs);
                try {
                    // Simular timeout: en producción sería un Future con get(timeout)
                    long start = System.currentTimeMillis();
                    String result = call.get();
                    long elapsed = System.currentTimeMillis() - start;

                    if (elapsed > timeoutMs) {
                        throw new RuntimeException("Timeout: " + elapsed + "ms > " + timeoutMs + "ms");
                    }

                    // Éxito: resetear contador de fallos del circuit breaker
                    cbFailures.remove(destination);
                    System.out.printf("  [Sidecar] Éxito en intento %d%n", attempt);
                    return result;

                } catch (Exception e) {
                    System.out.printf("  [Sidecar] Fallo en intento %d: %s%n", attempt, e.getMessage());

                    // Actualizar circuit breaker
                    int failures = cbFailures.merge(destination, 1, Integer::sum);
                    System.out.printf("  [Sidecar] CB fallos acumulados para '%s': %d/%d%n",
                            destination, failures, rule.cbFailureThreshold());

                    if (failures >= rule.cbFailureThreshold()) {
                        openCircuits.add(destination);
                        System.out.printf("  [Sidecar] *** CIRCUITO ABIERTO para '%s' ***%n", destination);
                        return "{\"error\":\"circuit opened after " + failures + " failures\"}";
                    }

                    if (attempt == maxRetries) {
                        return "{\"error\":\"all retries exhausted: " + e.getMessage() + "\"}";
                    }

                    // Pequeño backoff entre reintentos (simulado sin Thread.sleep real)
                    System.printf("  [Sidecar] Backoff antes del siguiente intento...%n");
                }
            }

            return "{\"error\":\"unexpected\"}";
        }
    }

    // ─────────────────────────────────────────────
    // APLICACIÓN: usa el sidecar de forma transparente
    // ─────────────────────────────────────────────

    static class AppServiceA {
        private final Sidecar sidecar;
        private final Service serviceB;

        AppServiceA(Sidecar sidecar, Service serviceB) {
            this.sidecar = sidecar;
            this.serviceB = serviceB;
        }

        // La app llama al sidecar como si fuera localhost — no sabe de retries ni CB
        String callServiceB(String endpoint) {
            System.out.printf("%n[AppServiceA] Llamando a service-b%s (via sidecar)%n", endpoint);
            return sidecar.outbound("service-b", endpoint, () -> serviceB.call(endpoint));
        }
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  SERVICE MESH — Sidecar Pattern");
        System.out.println("  Java puro, sin frameworks");
        System.out.println("═".repeat(65));

        ControlPlane cp = new ControlPlane();
        cp.addRule("service-b", new RoutingRule("service-b", 3, 500, 3));

        Sidecar sidecarA = new Sidecar("service-a");
        cp.registerSidecar(sidecarA);
        cp.pushConfig();

        Service serviceB = new Service("service-b");
        AppServiceA appA = new AppServiceA(sidecarA, serviceB);

        // ── Demo 1: Retry en fallo transitorio ────────────────────────
        System.out.println("\n══ DEMO 1: Retry en fallo transitorio ══");
        System.out.println("  service-b falla las primeras 2 llamadas (fallo transitorio)");
        serviceB.failFirstNCalls(2);
        String result = appA.callServiceB("/orders/1");
        System.out.printf("  Resultado: %s%n", result);

        // ── Demo 2: Circuit breaker — 3 fallos consecutivos ───────────
        System.out.println("\n══ DEMO 2: Circuit breaker se abre tras 3 fallos ══");
        Service badService = new Service("service-b-bad");
        badService.failFirstNCalls(100); // siempre falla

        Sidecar sidecarB = new Sidecar("service-a-2");
        cp.registerSidecar(sidecarB);
        cp.pushConfig();

        AppServiceA appA2 = new AppServiceA(sidecarB, badService);

        // Primera llamada: 3 reintentos → todos fallan → circuito abre
        System.out.println("  Primera llamada (fallos hasta abrir circuito):");
        String r1 = appA2.callServiceB("/orders/2");
        System.out.printf("  Resultado: %s%n", r1);

        // Segunda llamada: circuito abierto → rechazada sin intentar el backend
        System.out.println("\n  Segunda llamada (circuito ya abierto):");
        String r2 = appA2.callServiceB("/orders/3");
        System.out.printf("  Resultado: %s%n", r2);

        // ── Demo 3: Control Plane push de nueva configuración ─────────
        System.out.println("\n══ DEMO 3: Control Plane actualiza configuración en caliente ══");
        cp.addRule("service-b", new RoutingRule("service-b", 5, 2000, 10));
        cp.pushConfig();

        System.out.println("  → Sidecars actualizados con nuevas reglas: retries=5 timeout=2000ms cb=10");
        System.out.println("  → La app NO sabe que la configuración cambió");

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN SERVICE MESH");
        System.out.println("═".repeat(65));
        System.out.println("  Sidecar: proxy inyectado junto a cada instancia de servicio");
        System.out.println("  Intercepta: TODO el tráfico entrante y saliente");
        System.out.println("  Capacidades: retries, timeouts, CB, mTLS, tracing, rate limit");
        System.out.println("  Control Plane: Istio/Linkerd pushea config a todos los sidecar");
        System.out.println("  Beneficio: la app no implementa lógica de resiliencia — zero code");
        System.out.println("═".repeat(65));
    }
}
