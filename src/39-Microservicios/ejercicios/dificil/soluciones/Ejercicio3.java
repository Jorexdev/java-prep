import java.util.*;
import java.util.function.Supplier;

public class Ejercicio3 {

    enum CBState { CLOSED, OPEN, HALF_OPEN }

    static class Sidecar {
        private final int cbThreshold;
        private final int maxRetries;
        private final long timeoutMs;
        private int cbFailures = 0;
        private CBState cbState = CBState.CLOSED;
        private long cbOpenedAt = 0;
        private long clock = 0;
        private final Map<String, int[]> metrics = new LinkedHashMap<>();

        Sidecar(int cbThreshold, int maxRetries, long timeoutMs) {
            this.cbThreshold = cbThreshold;
            this.maxRetries = maxRetries;
            this.timeoutMs = timeoutMs;
        }

        void advanceClock(long ms) { clock += ms; }

        <T> T call(String operation, long simulatedDurationMs, Supplier<T> task) {
            metrics.computeIfAbsent(operation, k -> new int[]{0, 0});
            int[] m = metrics.get(operation);
            m[0]++;

            if (cbState == CBState.OPEN) {
                if (clock - cbOpenedAt >= 500) {
                    cbState = CBState.HALF_OPEN;
                    System.out.println("  [CB] → HALF_OPEN");
                } else {
                    m[1]++;
                    throw new RuntimeException("Circuit OPEN — llamada rechazada");
                }
            }

            for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
                try {
                    if (simulatedDurationMs > timeoutMs) {
                        throw new RuntimeException("Timeout: " + simulatedDurationMs + "ms > " + timeoutMs + "ms");
                    }
                    T result = task.get();
                    onSuccess();
                    return result;
                } catch (Exception e) {
                    System.out.println("  [Sidecar] Intento " + attempt + " fallido: " + e.getMessage());
                    if (attempt >= maxRetries + 1) {
                        onFailure();
                        m[1]++;
                        throw e;
                    }
                }
            }
            throw new RuntimeException("Inalcanzable");
        }

        private void onSuccess() {
            if (cbState == CBState.HALF_OPEN) {
                cbState = CBState.CLOSED;
                cbFailures = 0;
                System.out.println("  [CB] → CLOSED");
            } else {
                cbFailures = 0;
            }
        }

        private void onFailure() {
            cbFailures++;
            if (cbState == CBState.HALF_OPEN || cbFailures >= cbThreshold) {
                cbState = CBState.OPEN;
                cbOpenedAt = clock;
                System.out.println("  [CB] → OPEN (fallos=" + cbFailures + ")");
            }
        }

        void printMetrics() {
            System.out.println("\n=== Métricas ===");
            metrics.forEach((op, m) ->
                System.out.printf("  %-20s total=%d errores=%d%n", op, m[0], m[1]));
        }
    }

    static class CatalogoService {
        String obtenerProducto(String id) {
            if (id.contains("error")) throw new RuntimeException("Producto no disponible: " + id);
            return "Producto[" + id + "]";
        }
    }

    public static void main(String[] args) {
        Sidecar sidecar = new Sidecar(2, 2, 200);
        CatalogoService catalogo = new CatalogoService();

        String[] params = {
            "laptop", "mouse", "error-1", "teclado", "error-2",
            "monitor", "error-3", "auriculares", "webcam", "silla"
        };

        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            System.out.println("Llamada " + (i + 1) + ": obtenerProducto('" + param + "')");
            try {
                String result = sidecar.call("obtenerProducto", 50, () -> catalogo.obtenerProducto(param));
                System.out.println("  Resultado: " + result);
            } catch (Exception e) {
                System.out.println("  Error final: " + e.getMessage());
                sidecar.advanceClock(600);
            }
        }

        sidecar.printMetrics();
    }
}
