import java.util.concurrent.*;
import java.util.function.Supplier;

public class Ejercicio3 {

    static class TimeoutExecutor {
        private final ExecutorService executor = Executors.newCachedThreadPool();

        <T> T execute(Supplier<T> task, long timeoutMs) throws TimeoutException, Exception {
            Future<T> future = executor.submit(task::get);
            try {
                return future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new TimeoutException("Tarea excedió " + timeoutMs + "ms");
            } catch (ExecutionException e) {
                throw (Exception) e.getCause();
            }
        }

        void shutdown() { executor.shutdown(); }
    }

    public static void main(String[] args) throws Exception {
        TimeoutExecutor executor = new TimeoutExecutor();

        System.out.println("--- Tarea rápida ---");
        try {
            String result = executor.execute(() -> "Resultado inmediato", 500);
            System.out.println("Resultado: " + result);
        } catch (TimeoutException e) {
            System.out.println("Timeout: " + e.getMessage());
        }

        System.out.println("\n--- Tarea lenta ---");
        try {
            executor.execute(() -> {
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                return "Nunca llega";
            }, 300);
        } catch (TimeoutException e) {
            System.out.println("Capturado correctamente: " + e.getMessage());
        }

        executor.shutdown();
    }
}
