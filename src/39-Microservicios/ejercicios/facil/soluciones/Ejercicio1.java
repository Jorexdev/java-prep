import java.util.function.Supplier;

public class Ejercicio1 {

    enum State { CLOSED, OPEN, HALF_OPEN }

    static class CircuitOpenException extends RuntimeException {
        CircuitOpenException(String msg) { super(msg); }
    }

    static class CircuitBreaker {
        private final int failThreshold;
        private final long waitMs;
        private State state = State.CLOSED;
        private int failCount = 0;
        private long openedAt = 0;
        private long clock = 0;

        CircuitBreaker(int failThreshold, long waitMs) {
            this.failThreshold = failThreshold;
            this.waitMs = waitMs;
        }

        void advanceClock(long ms) { clock += ms; }

        <T> T call(Supplier<T> action) {
            if (state == State.OPEN) {
                if (clock - openedAt >= waitMs) {
                    state = State.HALF_OPEN;
                    System.out.println("[CB] → HALF_OPEN");
                } else {
                    throw new CircuitOpenException("Circuito abierto, llamada rechazada");
                }
            }
            try {
                T result = action.get();
                onSuccess();
                return result;
            } catch (CircuitOpenException e) {
                throw e;
            } catch (Exception e) {
                onFailure();
                throw e;
            }
        }

        private void onSuccess() {
            if (state == State.HALF_OPEN) {
                state = State.CLOSED;
                failCount = 0;
                System.out.println("[CB] → CLOSED (prueba exitosa)");
            } else {
                failCount = 0;
            }
        }

        private void onFailure() {
            failCount++;
            if (state == State.HALF_OPEN || failCount >= failThreshold) {
                state = State.OPEN;
                openedAt = clock;
                System.out.println("[CB] → OPEN (fallos=" + failCount + ")");
            }
        }

        State getState() { return state; }
    }

    public static void main(String[] args) {
        CircuitBreaker cb = new CircuitBreaker(3, 1000);
        Supplier<String> failingService = () -> { throw new RuntimeException("Servicio caído"); };
        Supplier<String> okService = () -> "OK";

        for (int i = 1; i <= 3; i++) {
            try {
                cb.call(failingService);
            } catch (CircuitOpenException e) {
                System.out.println("Rechazado: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Fallo " + i + ": " + e.getMessage());
            }
        }

        System.out.println("Estado: " + cb.getState());

        try {
            cb.call(okService);
        } catch (CircuitOpenException e) {
            System.out.println("Rechazado (correcto): " + e.getMessage());
        }

        cb.advanceClock(1500);

        try {
            String result = cb.call(okService);
            System.out.println("Prueba HALF_OPEN exitosa: " + result);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("Estado final: " + cb.getState());
    }
}
