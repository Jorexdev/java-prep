import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

public class Ejercicio2 {

    static class RequestScopeContext {
        private static final ThreadLocal<Map<String, Object>> requestScope = new ThreadLocal<>();

        static void beginRequest() {
            requestScope.set(new HashMap<>());
        }

        static void endRequest() {
            requestScope.remove();
        }

        static <T> T getBean(String name, Supplier<T> factory) {
            Map<String, Object> scope = requestScope.get();
            if (scope == null) throw new IllegalStateException("No hay request activo en este thread");

            @SuppressWarnings("unchecked")
            T bean = (T) scope.computeIfAbsent(name, k -> factory.get());
            return bean;
        }
    }

    // Bean con estado por request
    static class SesionUsuario {
        final String threadName;
        final int sessionId;
        private static int counter = 0;

        SesionUsuario(String threadName) {
            this.threadName = threadName;
            this.sessionId = ++counter;
            System.out.println("  [" + threadName + "] Nueva SesionUsuario#" + sessionId + " creada");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(3);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 1; i <= 3; i++) {
            final String name = "Thread-" + i;
            Thread.ofPlatform().name(name).start(() -> {
                RequestScopeContext.beginRequest();
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                // Primera llamada → crea la sesión
                SesionUsuario s1 = RequestScopeContext.getBean("sesion",
                    () -> new SesionUsuario(Thread.currentThread().getName()));
                // Segunda llamada → devuelve la misma del request
                SesionUsuario s2 = RequestScopeContext.getBean("sesion",
                    () -> new SesionUsuario(Thread.currentThread().getName()));

                System.out.println("  [" + name + "] s1==" + s1.sessionId + ", s2==" + s2.sessionId
                    + ", misma instancia: " + (s1 == s2));
                RequestScopeContext.endRequest();
            });
        }

        ready.await();
        System.out.println("=== Inicio simultáneo de 3 requests ===");
        start.countDown();
        Thread.sleep(200);
        System.out.println("\nCada thread tiene su propio bean de sesión.");
    }
}
