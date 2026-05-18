import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementación de Circuit Breaker con Java puro.
 *
 * Estados: CLOSED → OPEN → HALF_OPEN → CLOSED
 *
 * Configuración simulada:
 *  - failureRateThreshold: 50% en las últimas 5 llamadas → OPEN
 *  - waitDurationMs: 2000ms en OPEN antes de pasar a HALF_OPEN
 *  - permittedCallsInHalfOpen: 2 llamadas de prueba en HALF_OPEN
 */
public class ExpCircuitBreaker {

    // ─────────────────────────────────────────────
    // ESTADOS DEL CIRCUIT BREAKER
    // ─────────────────────────────────────────────

    enum Estado {
        CLOSED,    // Normal: las llamadas pasan
        OPEN,      // Protegido: rechaza llamadas, llama fallback
        HALF_OPEN  // Prueba: deja pasar N llamadas para ver si el servicio se recuperó
    }

    // ─────────────────────────────────────────────
    // CIRCUIT BREAKER
    // ─────────────────────────────────────────────

    static class CircuitBreaker {
        private final String nombre;
        private final int slidingWindowSize;
        private final double failureRateThreshold; // porcentaje 0.0-1.0
        private final long waitDurationMs;
        private final int permittedCallsInHalfOpen;

        private Estado estado = Estado.CLOSED;
        private final Deque<Boolean> ventana = new ArrayDeque<>(); // true=éxito, false=fallo
        private long openedAt = 0;
        private int halfOpenCalls = 0;
        private int halfOpenSuccesses = 0;

        CircuitBreaker(String nombre, int slidingWindowSize, double failureRateThreshold,
                       long waitDurationMs, int permittedCallsInHalfOpen) {
            this.nombre = nombre;
            this.slidingWindowSize = slidingWindowSize;
            this.failureRateThreshold = failureRateThreshold;
            this.waitDurationMs = waitDurationMs;
            this.permittedCallsInHalfOpen = permittedCallsInHalfOpen;
        }

        // Intentar ejecutar una llamada protegida por el circuit breaker
        <T> T ejecutar(java.util.function.Supplier<T> llamada, java.util.function.Supplier<T> fallback) {
            verificarTransicionHalfOpen();

            switch (estado) {
                case OPEN -> {
                    System.out.printf("  [CB %s] Estado OPEN → fallback directo (sin llamar al servicio)%n", nombre);
                    return fallback.get();
                }
                case HALF_OPEN -> {
                    if (halfOpenCalls >= permittedCallsInHalfOpen) {
                        System.out.printf("  [CB %s] Estado HALF_OPEN → máx llamadas de prueba alcanzado → fallback%n", nombre);
                        return fallback.get();
                    }
                    halfOpenCalls++;
                    System.out.printf("  [CB %s] Estado HALF_OPEN → llamada de prueba %d/%d%n",
                            nombre, halfOpenCalls, permittedCallsInHalfOpen);
                }
                case CLOSED -> {
                    // pasa normalmente
                }
            }

            try {
                T resultado = llamada.get();
                registrarExito();
                return resultado;
            } catch (Exception e) {
                registrarFallo();
                System.out.printf("  [CB %s] Fallo registrado: %s%n", nombre, e.getMessage());
                return fallback.get();
            }
        }

        private void registrarExito() {
            ventana.addLast(true);
            if (ventana.size() > slidingWindowSize) ventana.removeFirst();

            if (estado == Estado.HALF_OPEN) {
                halfOpenSuccesses++;
                System.out.printf("  [CB %s] HALF_OPEN éxito %d/%d%n",
                        nombre, halfOpenSuccesses, permittedCallsInHalfOpen);
                if (halfOpenSuccesses >= permittedCallsInHalfOpen) {
                    transicionar(Estado.CLOSED);
                }
            }
        }

        private void registrarFallo() {
            ventana.addLast(false);
            if (ventana.size() > slidingWindowSize) ventana.removeFirst();

            if (estado == Estado.HALF_OPEN) {
                System.out.printf("  [CB %s] HALF_OPEN fallo → volviendo a OPEN%n", nombre);
                transicionar(Estado.OPEN);
                return;
            }

            if (estado == Estado.CLOSED && ventana.size() >= slidingWindowSize) {
                double tasaFallos = ventana.stream().filter(b -> !b).count() / (double) ventana.size();
                System.out.printf("  [CB %s] Tasa de fallos: %.0f%% (umbral: %.0f%%)%n",
                        nombre, tasaFallos * 100, failureRateThreshold * 100);
                if (tasaFallos >= failureRateThreshold) {
                    transicionar(Estado.OPEN);
                }
            }
        }

        private void verificarTransicionHalfOpen() {
            if (estado == Estado.OPEN) {
                long tiempoEsperado = System.currentTimeMillis() - openedAt;
                if (tiempoEsperado >= waitDurationMs) {
                    transicionar(Estado.HALF_OPEN);
                }
            }
        }

        private void transicionar(Estado nuevoEstado) {
            System.out.printf("%n  *** [CB %s] TRANSICIÓN: %s → %s ***%n%n", nombre, estado, nuevoEstado);
            estado = nuevoEstado;
            if (nuevoEstado == Estado.OPEN) {
                openedAt = System.currentTimeMillis();
            }
            if (nuevoEstado == Estado.HALF_OPEN) {
                halfOpenCalls = 0;
                halfOpenSuccesses = 0;
            }
            if (nuevoEstado == Estado.CLOSED) {
                ventana.clear();
            }
        }

        Estado estado() { return estado; }
    }

    // ─────────────────────────────────────────────
    // SERVICIO REMOTO QUE PUEDE FALLAR
    // ─────────────────────────────────────────────

    static class ServicioInventario {
        private final AtomicInteger llamadas = new AtomicInteger(0);
        private boolean simulandoFallo = false;
        private boolean recuperado = false;

        void activarFallos() { simulandoFallo = true; }
        void simularRecuperacion() { recuperado = true; }

        int consultarStock(String productoId) {
            int n = llamadas.incrementAndGet();
            if (simulandoFallo && !recuperado) {
                throw new RuntimeException("Servicio Inventario no disponible (timeout)");
            }
            return 100 - n; // stock simulado
        }
    }

    // ─────────────────────────────────────────────
    // MAIN: demo completa del ciclo CLOSED → OPEN → HALF_OPEN → CLOSED
    // ─────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {

        System.out.println("═".repeat(65));
        System.out.println("  CIRCUIT BREAKER — Java puro");
        System.out.println("  Configuración: ventana=5, umbral=50%, wait=500ms, halfOpen=2");
        System.out.println("═".repeat(65));

        ServicioInventario inventario = new ServicioInventario();

        // Circuit Breaker configurado con ventana de 5 llamadas, 50% umbral, 500ms wait
        CircuitBreaker cb = new CircuitBreaker("inventario", 5, 0.5, 500, 2);

        java.util.function.Supplier<Integer> fallback = () -> {
            System.out.println("  [Fallback] Usando caché de stock: 0 (servicio no disponible)");
            return 0;
        };

        // ── Fase 1: Estado CLOSED — llamadas normales ──────────────────
        System.out.println("\n─ FASE 1: Estado CLOSED (llamadas normales) ─");
        for (int i = 1; i <= 3; i++) {
            int stock = cb.ejecutar(() -> inventario.consultarStock("producto-1"), fallback);
            System.out.printf("  Llamada %d → stock=%d (estado: %s)%n", i, stock, cb.estado());
        }

        // ── Fase 2: El servicio empieza a fallar ───────────────────────
        System.out.println("\n─ FASE 2: Servicio falla → acumulando fallos ─");
        inventario.activarFallos();
        for (int i = 4; i <= 8; i++) {
            int stock = cb.ejecutar(() -> inventario.consultarStock("producto-1"), fallback);
            System.out.printf("  Llamada %d → stock=%d (estado: %s)%n", i, stock, cb.estado());
            if (cb.estado() == Estado.OPEN) {
                System.out.println("  → Circuit Breaker ABIERTO: las siguientes llamadas usarán fallback");
                break;
            }
        }

        // ── Fase 3: Estado OPEN — todo va a fallback ──────────────────
        System.out.println("\n─ FASE 3: Estado OPEN (rechazando llamadas) ─");
        for (int i = 1; i <= 3; i++) {
            int stock = cb.ejecutar(() -> inventario.consultarStock("producto-1"), fallback);
            System.out.printf("  Llamada %d → stock=%d (estado: %s)%n", i, stock, cb.estado());
        }

        // ── Fase 4: Esperar waitDuration → HALF_OPEN ─────────────────
        System.out.println("\n─ FASE 4: Esperando waitDuration (500ms) → HALF_OPEN ─");
        Thread.sleep(600); // simulamos paso del tiempo
        inventario.simularRecuperacion(); // servicio se recupera

        for (int i = 1; i <= 4; i++) {
            int stock = cb.ejecutar(() -> inventario.consultarStock("producto-1"), fallback);
            System.out.printf("  Llamada %d → stock=%d (estado: %s)%n", i, stock, cb.estado());
            if (cb.estado() == Estado.CLOSED) {
                System.out.println("  → Circuit Breaker CERRADO: servicio recuperado");
                break;
            }
        }

        // ── Fase 5: Estado CLOSED recuperado ─────────────────────────
        System.out.println("\n─ FASE 5: Estado CLOSED (recuperado) ─");
        for (int i = 1; i <= 3; i++) {
            int stock = cb.ejecutar(() -> inventario.consultarStock("producto-1"), fallback);
            System.out.printf("  Llamada %d → stock=%d (estado: %s)%n", i, stock, cb.estado());
        }

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN DEL CICLO");
        System.out.println("═".repeat(65));
        System.out.println("  CLOSED  → llamadas normales, registra tasa de fallos");
        System.out.println("  OPEN    → rechaza inmediatamente, ejecuta fallback, protege el servicio caído");
        System.out.println("  HALF_OPEN → N llamadas de prueba para verificar recuperación");
        System.out.println("  → CLOSED si pasan | → OPEN si fallan");
        System.out.println("═".repeat(65));
    }
}
