import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Simula Spring @Async sin dependencias de Spring.
//
// Cómo funciona @Async en Spring:
//   1. @EnableAsync activa un BeanPostProcessor que crea un proxy CGLIB/JDK alrededor
//      del bean anotado con @Async.
//   2. Cuando se llama al método, el proxy intercepta la llamada y la delega a un
//      ThreadPoolTaskExecutor en lugar de ejecutarla en el hilo del llamador.
//   3. El hilo del llamador recibe inmediatamente un Future/CompletableFuture o void.
//   4. Si el método lanza una excepción y devuelve void, el AsyncUncaughtExceptionHandler
//      la recibe — sin él la excepción se pierde silenciosamente.
//
// Trampa clásica: llamar a un @Async desde el mismo bean no pasa por el proxy
// → la llamada es directa y el método se ejecuta sincrónicamente.
public class ExpSpringAsync {

    // ── ThreadPoolTaskExecutor simulado ───────────────────────────────────────

    // En Spring: @Bean ThreadPoolTaskExecutor taskExecutor() { ... }
    // Parámetros clave:
    //   corePoolSize    → hilos siempre vivos (incluso sin tareas)
    //   maxPoolSize     → máximo de hilos antes de rechazar o encolar
    //   queueCapacity   → tamaño de la cola; si se llena y hay maxPoolSize hilos → RejectedExecutionException
    //   keepAliveSeconds → tiempo que vive un hilo por encima de corePoolSize sin trabajo
    static class ThreadPoolTaskExecutor {
        private final ThreadPoolExecutor executor;
        private final String name;

        ThreadPoolTaskExecutor(String name, int core, int max, int queue) {
            this.name = name;
            // LinkedBlockingQueue con capacidad finita — evita memoria ilimitada
            this.executor = new ThreadPoolExecutor(core, max, 60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(queue),
                    r -> {
                        Thread t = new Thread(r, name + "-" + threadCounter.incrementAndGet());
                        t.setDaemon(true); // hilo daemon — no impide que la JVM termine
                        return t;
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy()); // cuando la cola está llena, el llamador ejecuta
        }

        private static final AtomicInteger threadCounter = new AtomicInteger(0);

        ExecutorService executor() { return executor; }

        void shutdown() {
            executor.shutdown();
            try { executor.awaitTermination(10, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        void printStatus() {
            System.out.printf("  [%s] active=%d pool=%d queue=%d completed=%d%n",
                    name, executor.getActiveCount(), executor.getPoolSize(),
                    executor.getQueue().size(), executor.getCompletedTaskCount());
        }
    }

    // ── AsyncUncaughtExceptionHandler ─────────────────────────────────────────

    // En Spring: implementar AsyncUncaughtExceptionHandler en @Configuration + @EnableAsync
    // Captura excepciones de métodos @Async que devuelven void — no hay Future para propagarlas.
    // Si devuelve Future/CompletableFuture, la excepción queda envuelta en el Future.
    interface AsyncUncaughtExceptionHandler {
        void handleUncaughtException(Throwable ex, String methodName, Object... params);
    }

    static class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable ex, String methodName, Object... params) {
            System.out.printf("  [AsyncExceptionHandler] Excepción en '%s'(%s): %s%n",
                    methodName, java.util.Arrays.toString(params), ex.getMessage());
        }
    }

    // ── Proxy async simulado ──────────────────────────────────────────────────

    // El proxy intercepta la llamada y la despacha al executor.
    // En Spring: el proxy es generado en runtime por CGLIB o JDK Dynamic Proxy.
    static class AsyncProxy {
        private final ExecutorService executor;
        private final AsyncUncaughtExceptionHandler exceptionHandler;

        AsyncProxy(ExecutorService executor, AsyncUncaughtExceptionHandler handler) {
            this.executor = executor;
            this.exceptionHandler = handler;
        }

        // Simula @Async en método void — despacha sin esperar resultado
        void invokeAsync(String methodName, Runnable task, Object... params) {
            executor.submit(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    // Sin Future que capture la excepción, va al handler
                    exceptionHandler.handleUncaughtException(e, methodName, params);
                }
            });
        }

        // Simula @Async en método CompletableFuture<T>
        <T> CompletableFuture<T> invokeAsyncFuture(Callable<T> task) {
            // supplyAsync delega al executor; la excepción queda dentro del Future
            return CompletableFuture.supplyAsync(() -> {
                try { return task.call(); }
                catch (Exception e) { throw new CompletionException(e); }
            }, executor);
        }
    }

    // ── Servicio de negocio ───────────────────────────────────────────────────

    static class NotificationService {
        private final AsyncProxy proxy;

        NotificationService(AsyncProxy proxy) { this.proxy = proxy; }

        // @Async — void: el llamador no espera, las excepciones van al handler
        void sendEmail(String to, String subject) {
            proxy.invokeAsync("sendEmail", () -> {
                busyWaitMs(80);
                System.out.printf("  [Email] Enviado a %s: \"%s\"%n", to, subject);
            }, to, subject);
        }

        // @Async — void que falla: la excepción va al AsyncUncaughtExceptionHandler
        void sendPushNotification(String userId) {
            proxy.invokeAsync("sendPushNotification", () -> {
                busyWaitMs(30);
                throw new RuntimeException("Dispositivo no registrado: " + userId);
            }, userId);
        }

        // @Async — CompletableFuture<String>: el llamador puede esperar si quiere
        CompletableFuture<String> generateReport(String reportId) {
            return proxy.invokeAsyncFuture(() -> {
                busyWaitMs(120);
                return "report-" + reportId + "-" + Thread.currentThread().getName();
            });
        }
    }

    static void busyWaitMs(long ms) {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { /* spin */ }
    }

    // ── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        System.out.println("═".repeat(60));
        System.out.println("  SPRING @ASYNC — ThreadPool + Future + ExceptionHandler");
        System.out.println("═".repeat(60));

        // Construir executor equivalente al @Bean de Spring
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor(
                "async-executor", 2, 4, 10);

        AsyncProxy proxy = new AsyncProxy(
                taskExecutor.executor(),
                new LoggingAsyncExceptionHandler());

        NotificationService service = new NotificationService(proxy);

        // ── Demo 1: void async (fire-and-forget) ────────────────────────────
        System.out.println("\n=== @Async void — fire-and-forget ===");
        long start = System.currentTimeMillis();
        service.sendEmail("jorge@example.com", "Bienvenido");
        service.sendEmail("ana@example.com",   "Factura disponible");
        System.out.printf("  Hilo principal continúa inmediatamente (%.0fms)%n",
                (double)(System.currentTimeMillis() - start));

        // ── Demo 2: excepción en @Async void → AsyncUncaughtExceptionHandler ─
        System.out.println("\n=== Excepción en @Async void → handler ===");
        service.sendPushNotification("user-999");
        busyWaitMs(150); // esperar que el async termine antes de imprimir estado

        // ── Demo 3: @Async CompletableFuture — el llamador puede unirse ──────
        System.out.println("\n=== @Async CompletableFuture — esperar resultado ===");
        List<CompletableFuture<String>> futures = List.of(
                service.generateReport("R1"),
                service.generateReport("R2"),
                service.generateReport("R3"));

        // Esperar todos en paralelo — sin bloquear en cada uno individualmente
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        futures.forEach(f -> {
            try { System.out.println("  Report: " + f.get()); }
            catch (Exception e) { System.out.println("  Error: " + e.getMessage()); }
        });

        // ── Demo 4: excepción en CompletableFuture ───────────────────────────
        System.out.println("\n=== Excepción en @Async CompletableFuture ===");
        CompletableFuture<String> failedFuture = proxy.invokeAsyncFuture(() -> {
            busyWaitMs(20);
            throw new RuntimeException("Servicio de reportes caído");
        });
        // exceptionally: gestión inline de errores sin try/catch
        String fallback = failedFuture
                .exceptionally(ex -> "fallback-report (error: " + ex.getCause().getMessage() + ")")
                .get();
        System.out.println("  Resultado: " + fallback);

        taskExecutor.printStatus();

        System.out.println("\n── Resumen ─────────────────────────────────────────────");
        System.out.println("  @Async void          → fire-and-forget, errores al handler");
        System.out.println("  @Async Future<T>     → bloquea en .get() si se necesita");
        System.out.println("  @Async CompletableFuture<T> → pipeline non-blocking");
        System.out.println("  AsyncUncaughtExceptionHandler → solo para métodos void");
        System.out.println("  Self-invocation      → no pasa por proxy → sync (trampa)");
        System.out.println("  @EnableAsync         → activa el BeanPostProcessor que crea el proxy");

        taskExecutor.shutdown();
    }
}
