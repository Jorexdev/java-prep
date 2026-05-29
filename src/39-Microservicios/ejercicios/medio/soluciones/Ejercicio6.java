import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class Ejercicio6 {

    // --- Estados del Circuit Breaker ---

    enum State { CLOSED, OPEN, HALF_OPEN }

    // --- Excepción cuando el circuito está abierto ---

    static class CircuitOpenException extends RuntimeException {
        CircuitOpenException(String name) {
            super("Circuit breaker [" + name + "] está ABIERTO — llamada rechazada");
        }
    }

    // --- Circuit Breaker ---

    static class CircuitBreaker {
        private final String name;
        private final int failureThreshold;   // nº de fallos para abrir el circuito
        private final long openTimeoutMs;     // tiempo en OPEN antes de pasar a HALF_OPEN
        private final AtomicLong clock;

        private State state = State.CLOSED;
        private int failureCount = 0;
        private long openedAt = -1;

        CircuitBreaker(String name, int failureThreshold, long openTimeoutMs, AtomicLong clock) {
            this.name             = name;
            this.failureThreshold = failureThreshold;
            this.openTimeoutMs    = openTimeoutMs;
            this.clock            = clock;
        }

        <T> T call(Supplier<T> action) {
            long now = clock.get();

            // Transición OPEN → HALF_OPEN si ha pasado el timeout
            if (state == State.OPEN) {
                if (now - openedAt >= openTimeoutMs) {
                    transitionTo(State.HALF_OPEN);
                } else {
                    throw new CircuitOpenException(name);
                }
            }

            try {
                T result = action.get();
                onSuccess();
                return result;
            } catch (CircuitOpenException e) {
                throw e; // re-lanzar sin contar como fallo de la lógica
            } catch (Exception e) {
                onFailure();
                throw e;
            }
        }

        private void onSuccess() {
            if (state == State.HALF_OPEN) {
                transitionTo(State.CLOSED);
                failureCount = 0;
                System.out.println("  [CB:" + name + "] Llamada de prueba exitosa → CLOSED");
            } else {
                failureCount = 0;
            }
        }

        private void onFailure() {
            failureCount++;
            System.out.println("  [CB:" + name + "] Fallo #" + failureCount
                + " en estado " + state);
            if (state == State.HALF_OPEN) {
                transitionTo(State.OPEN);
                openedAt = clock.get();
                System.out.println("  [CB:" + name + "] Prueba fallida → vuelve a OPEN");
            } else if (state == State.CLOSED && failureCount >= failureThreshold) {
                transitionTo(State.OPEN);
                openedAt = clock.get();
            }
        }

        private void transitionTo(State newState) {
            System.out.println("  [CB:" + name + "] " + state + " → " + newState);
            state = newState;
        }

        State getState() { return state; }
    }

    // --- Servicio que puede fallar ---

    static class RemoteService {
        private boolean failNext = false;

        void setFailNext(boolean fail) { this.failNext = fail; }

        String call(String id) {
            if (failNext) {
                failNext = false;
                throw new RuntimeException("Error del servicio remoto para id=" + id);
            }
            return "OK — datos para id=" + id;
        }
    }

    // --- Helper ---

    static void invoke(CircuitBreaker cb, RemoteService svc, String id) {
        try {
            String result = cb.call(() -> svc.call(id));
            System.out.println("  Resultado: " + result);
        } catch (CircuitOpenException e) {
            System.out.println("  BLOQUEADO: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  ERROR: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreaker cb = new CircuitBreaker("pagos", 3, 5_000L, clock);
        RemoteService svc = new RemoteService();

        System.out.println("=== Fase 1: llamadas exitosas (estado CLOSED) ===");
        invoke(cb, svc, "req-1");
        invoke(cb, svc, "req-2");
        System.out.println("Estado: " + cb.getState());

        System.out.println("\n=== Fase 2: 3 fallos consecutivos → OPEN ===");
        for (int i = 3; i <= 5; i++) {
            svc.setFailNext(true);
            invoke(cb, svc, "req-" + i);
        }
        System.out.println("Estado: " + cb.getState());

        System.out.println("\n=== Fase 3: llamadas bloqueadas mientras está OPEN ===");
        invoke(cb, svc, "req-6");
        invoke(cb, svc, "req-7");
        System.out.println("Estado: " + cb.getState());

        System.out.println("\n=== Fase 4: avanzar reloj 6000ms → pasa a HALF_OPEN ===");
        clock.addAndGet(6_000L);
        System.out.println("Reloj: " + clock.get() + "ms");

        System.out.println("\n=== Fase 5: llamada de prueba en HALF_OPEN — FALLA ===");
        svc.setFailNext(true);
        invoke(cb, svc, "req-8");
        System.out.println("Estado: " + cb.getState());

        System.out.println("\n=== Fase 6: avanzar reloj y llamada de prueba — ÉXITO ===");
        clock.addAndGet(6_000L);
        invoke(cb, svc, "req-9");
        System.out.println("Estado: " + cb.getState());

        System.out.println("\n=== Fase 7: servicio recuperado — tráfico normal ===");
        invoke(cb, svc, "req-10");
        invoke(cb, svc, "req-11");
        System.out.println("Estado final: " + cb.getState());
    }
}
