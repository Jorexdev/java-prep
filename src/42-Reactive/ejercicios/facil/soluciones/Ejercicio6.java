import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Timeout con valor por defecto: si el publisher no emite en X ms, usar fallback
public class Ejercicio6 {

    // Simula una operación asíncrona que emite un valor tras sleepMs milisegundos
    static <T> CompletableFuture<T> publisherLento(long sleepMs, T value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return value;
        });
    }

    // withTimeout: espera limitMs; si se supera, devuelve defaultValue
    static <T> T withTimeout(CompletableFuture<T> future, long limitMs, T defaultValue) {
        try {
            return future.get(limitMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println("  [Timeout] Límite de " + limitMs + "ms superado → usando valor por defecto");
            future.cancel(true); // cancelar la operación en curso
            return defaultValue;
        } catch (Exception e) {
            System.out.println("  [Error] " + e.getMessage() + " → usando valor por defecto");
            return defaultValue;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Timeout reactivo ===\n");

        // Caso 1: emite a 50ms, timeout en 100ms → debe recibir el valor real
        System.out.println("Caso 1: sleep=50ms, timeout=100ms");
        String r1 = withTimeout(publisherLento(50, "respuesta-rapida"), 100, "valor-por-defecto");
        System.out.println("  Resultado: " + r1);
        System.out.println();

        // Caso 2: emite a 200ms, timeout en 100ms → debe devolver fallback
        System.out.println("Caso 2: sleep=200ms, timeout=100ms");
        String r2 = withTimeout(publisherLento(200, "respuesta-lenta"), 100, "valor-por-defecto");
        System.out.println("  Resultado: " + r2);
        System.out.println();

        // Caso 3: timeout exacto en el límite
        System.out.println("Caso 3: sleep=100ms, timeout=100ms (caso límite)");
        String r3 = withTimeout(publisherLento(100, "respuesta-limite"), 100, "fallback-limite");
        System.out.println("  Resultado: " + r3 + " (puede ser cualquiera dependiendo del scheduling)");

        System.out.println();
        System.out.println("=== En Project Reactor ===");
        System.out.println("Mono.just(valor)");
        System.out.println("     .delayElement(Duration.ofMillis(50))");
        System.out.println("     .timeout(Duration.ofMillis(100))");
        System.out.println("     .onErrorReturn(\"fallback\")");
        System.out.println("     .subscribe(System.out::println);");
        System.out.println();
        System.out.println("El operador .timeout() emite TimeoutException si el upstream");
        System.out.println("no emite en el tiempo dado. Con onErrorReturn se recupera.");
    }
}
