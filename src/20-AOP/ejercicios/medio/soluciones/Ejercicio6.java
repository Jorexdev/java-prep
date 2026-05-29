import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

// Aspect que implementa retry automatico con backoff exponencial
// Simula fallos aleatorios en un servicio externo y reintenta la llamada

public class Ejercicio6 {

    // Anotacion simulada (en Spring seria @Retryable)
    // @interface Retryable { int maxAttempts(); long initialDelayMs(); double multiplier(); }

    // Advice de retry: intercepta la llamada, reintenta con backoff exponencial
    static class RetryAspect {
        private final int maxAttempts;
        private final long initialDelayMs;
        private final double multiplier;

        RetryAspect(int maxAttempts, long initialDelayMs, double multiplier) {
            this.maxAttempts = maxAttempts;
            this.initialDelayMs = initialDelayMs;
            this.multiplier = multiplier;
        }

        // Ejecuta la tarea con retry y backoff exponencial
        <T> T execute(String methodName, Callable<T> task) throws Exception {
            long delay = initialDelayMs;
            Exception lastException = null;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    System.out.printf("  [Retry] %s | intento %d/%d%n",
                            methodName, attempt, maxAttempts);
                    T result = task.call();
                    System.out.printf("  [Retry] %s | exito en intento %d%n",
                            methodName, attempt);
                    return result;
                } catch (Exception e) {
                    lastException = e;
                    System.out.printf("  [Retry] %s | FALLO: %s%n", methodName, e.getMessage());

                    if (attempt < maxAttempts) {
                        System.out.printf("  [Retry] esperando %d ms antes del siguiente intento...%n",
                                delay);
                        Thread.sleep(delay);
                        delay = (long)(delay * multiplier); // backoff exponencial
                    }
                }
            }
            System.out.printf("  [Retry] %s | agotados %d intentos, propagando excepcion%n",
                    methodName, maxAttempts);
            throw lastException;
        }
    }

    // Proxy que aplica el RetryAspect
    static class RetryProxy<T extends Calculable> {
        private final T target;
        private final RetryAspect aspect;

        RetryProxy(T target, RetryAspect aspect) {
            this.target = target;
            this.aspect = aspect;
        }

        int calcular(int x) throws Exception {
            return aspect.execute("calcular(" + x + ")", () -> target.calcular(x));
        }

        String obtenerDato(String clave) throws Exception {
            return aspect.execute("obtenerDato(" + clave + ")", () -> target.obtenerDato(clave));
        }
    }

    interface Calculable {
        int calcular(int x) throws Exception;
        String obtenerDato(String clave) throws Exception;
    }

    // Servicio real que falla aleatoriamente (simula un servicio externo inestable)
    static class ServicioExterno implements Calculable {
        private final AtomicInteger llamadas = new AtomicInteger(0);
        private final double failRate; // probabilidad de fallo por llamada

        ServicioExterno(double failRate) { this.failRate = failRate; }

        public int calcular(int x) throws Exception {
            int n = llamadas.incrementAndGet();
            if (Math.random() < failRate) {
                throw new RuntimeException("timeout en llamada #" + n);
            }
            return x * x;
        }

        public String obtenerDato(String clave) throws Exception {
            int n = llamadas.incrementAndGet();
            if (Math.random() < failRate) {
                throw new RuntimeException("connection refused en llamada #" + n);
            }
            return "valor-de-" + clave;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Retry Aspect con Backoff Exponencial ===");
        System.out.println();

        // RetryAspect: max 4 intentos, delay inicial 50ms, multiplicador 2x
        RetryAspect aspect = new RetryAspect(4, 50, 2.0);
        ServicioExterno servicio = new ServicioExterno(0.65); // 65% de fallo
        RetryProxy<ServicioExterno> proxy = new RetryProxy<>(servicio, aspect);

        // --- Demo 1: calcular (probablemente necesitara varios intentos) ---
        System.out.println("[ Demo 1 ] calcular(5) con servicio inestable (65% fallo)");
        try {
            int resultado = proxy.calcular(5);
            System.out.println("  Resultado: " + resultado);
        } catch (Exception e) {
            System.out.println("  Fallido definitivamente: " + e.getMessage());
        }

        System.out.println();

        // --- Demo 2: obtenerDato ---
        System.out.println("[ Demo 2 ] obtenerDato('usuario-id') con servicio inestable");
        try {
            String dato = proxy.obtenerDato("usuario-id");
            System.out.println("  Dato obtenido: " + dato);
        } catch (Exception e) {
            System.out.println("  Fallido definitivamente: " + e.getMessage());
        }

        System.out.println();

        // --- Demo 3: servicio muy inestable (casi siempre falla: 95%) ---
        System.out.println("[ Demo 3 ] calcular(10) con servicio muy inestable (95% fallo)");
        RetryProxy<ServicioExterno> proxyInestable =
                new RetryProxy<>(new ServicioExterno(0.95), aspect);
        try {
            int res = proxyInestable.calcular(10);
            System.out.println("  Resultado: " + res);
        } catch (Exception e) {
            System.out.println("  Fallido definitivamente tras 4 intentos: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Backoff Exponencial ===");
        System.out.println("delay inicial: 50ms");
        long d = 50;
        for (int i = 1; i <= 4; i++) {
            System.out.printf("  intento %d -> espera antes del siguiente: %d ms%n", i, d);
            d = (long)(d * 2.0);
        }
        System.out.println("El backoff reduce la presion sobre el servicio en reintentos.");
        System.out.println("En Spring: @Retryable(maxAttempts=4, backoff=@Backoff(delay=50, multiplier=2))");
    }
}
