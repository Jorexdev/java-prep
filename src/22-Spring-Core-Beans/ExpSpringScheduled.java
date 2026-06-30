import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// Simula Spring @Scheduled sin dependencias de Spring.
//
// Cómo funciona @Scheduled en Spring:
//   1. @EnableScheduling registra un ScheduledAnnotationBeanPostProcessor.
//   2. El postprocessor escanea beans en busca de métodos @Scheduled y los registra
//      en un TaskScheduler (por defecto un ThreadPoolTaskScheduler con 1 hilo).
//   3. fixedRate  → el período cuenta desde el INICIO de la ejecución anterior.
//   4. fixedDelay → el período cuenta desde el FIN de la ejecución anterior.
//   5. cron       → expresión "s m h d M dow" evaluada contra el reloj del sistema.
//
// Diferencia clave fixedRate vs fixedDelay:
//   - fixedRate 1000ms + tarea de 1500ms → la siguiente ejecución arranca inmediatamente
//     al terminar (no se solapan por defecto si el pool tiene 1 hilo). Con varios hilos
//     SÍ pueden solaparse — usar @Scheduled(fixedDelay) o synchronized para evitarlo.
//   - fixedDelay 1000ms + tarea de 1500ms → siguiente ejecución a los 2500ms del inicio.
//     fixedDelay GARANTIZA que la tarea anterior terminó antes de lanzar la siguiente.
public class ExpSpringScheduled {

    // ── TaskScheduler simulado ────────────────────────────────────────────────

    // En Spring: TaskScheduler / ThreadPoolTaskScheduler
    // El scheduler tiene su propio pool de hilos — separado del async executor.
    static class TaskScheduler {
        private final ScheduledExecutorService executor;

        TaskScheduler(int poolSize) {
            AtomicInteger counter = new AtomicInteger(0);
            this.executor = Executors.newScheduledThreadPool(poolSize,
                    r -> {
                        Thread t = new Thread(r, "scheduler-" + counter.incrementAndGet());
                        t.setDaemon(true);
                        return t;
                    });
        }

        // ── fixedRate ────────────────────────────────────────────────────────
        // En Spring: @Scheduled(fixedRate = 1000)
        // scheduleAtFixedRate: el delay siguiente se calcula desde el INICIO anterior.
        // Si una ejecución tarda más que el período, la siguiente arranca inmediatamente
        // al terminar (sin solapamiento cuando pool=1).
        ScheduledFuture<?> scheduleAtFixedRate(String name, long periodMs, Runnable task) {
            System.out.printf("[Scheduler] Registrado '%s' — fixedRate=%dms%n", name, periodMs);
            return executor.scheduleAtFixedRate(
                    () -> {
                        System.out.printf("[%s] START fixedRate hilo=%s%n",
                                name, Thread.currentThread().getName());
                        task.run();
                        System.out.printf("[%s] END%n", name);
                    },
                    periodMs, periodMs, TimeUnit.MILLISECONDS);
        }

        // ── fixedDelay ───────────────────────────────────────────────────────
        // En Spring: @Scheduled(fixedDelay = 1000)
        // scheduleWithFixedDelay: el delay siguiente se calcula desde el FIN anterior.
        // Garantiza que no hay solapamiento — la siguiente ejecución espera al final de la anterior.
        ScheduledFuture<?> scheduleWithFixedDelay(String name, long delayMs, Runnable task) {
            System.out.printf("[Scheduler] Registrado '%s' — fixedDelay=%dms%n", name, delayMs);
            return executor.scheduleWithFixedDelay(
                    () -> {
                        System.out.printf("[%s] START fixedDelay hilo=%s%n",
                                name, Thread.currentThread().getName());
                        task.run();
                        System.out.printf("[%s] END%n", name);
                    },
                    delayMs, delayMs, TimeUnit.MILLISECONDS);
        }

        // ── Cron ─────────────────────────────────────────────────────────────
        // En Spring: @Scheduled(cron = "0 * * * * *") — cada minuto en el segundo 0
        // Aquí simulamos cron parseando una expresión simplificada "cada N segundos".
        // En producción usa Spring Expression "s m h d M dow" (6 campos con Spring, 5 con Quartz).
        ScheduledFuture<?> scheduleCron(String name, String cronExpression, Runnable task) {
            long intervalMs = parseCronToMs(cronExpression);
            System.out.printf("[Scheduler] Registrado '%s' — cron='%s' → cada %dms%n",
                    name, cronExpression, intervalMs);
            // Cron alinea con el reloj del sistema (no con el tiempo de arranque del bean).
            // scheduleAtFixedRate aquí es una aproximación; Spring usa CronTrigger real.
            return executor.scheduleAtFixedRate(
                    () -> {
                        System.out.printf("[%s] START cron='%s' hilo=%s%n",
                                name, cronExpression, Thread.currentThread().getName());
                        task.run();
                        System.out.printf("[%s] END%n", name);
                    },
                    intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        }

        // Parseo simplificado — solo cubre "cada N segundos" para la demo
        private long parseCronToMs(String cron) {
            // Soporte de expresión "*/N * * * * *" (cada N segundos)
            if (cron.startsWith("*/")) {
                int secs = Integer.parseInt(cron.split(" ")[0].substring(2));
                return secs * 1000L;
            }
            return 5000; // fallback
        }

        void shutdown() {
            executor.shutdown();
            try { executor.awaitTermination(5, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    // ── Tarea con potencial solapamiento ─────────────────────────────────────

    // Demuestra por qué fixedDelay es más seguro que fixedRate para tareas largas.
    static class SyncTask {
        // AtomicBoolean como "mutex ligero" para detectar solapamiento
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final AtomicInteger executionCount = new AtomicInteger(0);
        private final String name;
        private final long durationMs;

        SyncTask(String name, long durationMs) {
            this.name = name;
            this.durationMs = durationMs;
        }

        void run() {
            if (!running.compareAndSet(false, true)) {
                // con fixedRate y pool>1 esto podría ocurrir — con fixedDelay nunca
                System.out.printf("  [%s] ⚠ SOLAPAMIENTO detectado — ejecución anterior no terminó%n", name);
                return;
            }
            try {
                int exec = executionCount.incrementAndGet();
                System.out.printf("  [%s] Ejecución #%d (durará %dms)%n", name, exec, durationMs);
                busyWaitMs(durationMs);
            } finally {
                running.set(false);
            }
        }

        int count() { return executionCount.get(); }
    }

    // ── Tareas de negocio ─────────────────────────────────────────────────────

    static class MetricsFlusher {
        void flush() {
            System.out.println("  [Metrics] Flushing métricas a Prometheus...");
            busyWaitMs(30);
        }
    }

    static class CacheEviction {
        private final AtomicInteger evicted = new AtomicInteger(0);
        void evict() {
            int n = (int)(Math.random() * 10 + 1);
            evicted.addAndGet(n);
            System.out.printf("  [Cache] Evicted %d entradas expiradas (total=%d)%n", n, evicted.get());
            busyWaitMs(20);
        }
    }

    static void busyWaitMs(long ms) {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { /* spin */ }
    }

    // ── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        System.out.println("═".repeat(62));
        System.out.println("  SPRING @SCHEDULED — fixedRate / fixedDelay / cron");
        System.out.println("═".repeat(62));

        // En Spring: @Bean TaskScheduler taskScheduler() — si no defines uno,
        // Spring Boot crea un ThreadPoolTaskScheduler con 1 hilo (spring.task.scheduling.pool.size)
        TaskScheduler scheduler = new TaskScheduler(2);

        MetricsFlusher metrics  = new MetricsFlusher();
        CacheEviction  cache    = new CacheEviction();

        // ── fixedRate: el período empieza cuando comienza la tarea ───────────
        // @Scheduled(fixedRate = 300)
        scheduler.scheduleAtFixedRate("metrics-flush", 300, metrics::flush);

        // ── fixedDelay: el período empieza cuando termina la tarea ──────────
        // @Scheduled(fixedDelay = 200) — garantiza que no se solapa
        scheduler.scheduleWithFixedDelay("cache-evict", 200, cache::evict);

        // ── cron: expresión evaluada contra el reloj ─────────────────────────
        // @Scheduled(cron = "*/1 * * * * *") — cada 1 segundo (Spring usa 6 campos con segundos)
        scheduler.scheduleCron("heartbeat", "*/1 * * * * *",
                () -> System.out.println("  [Heartbeat] ping — " + LocalTime.now()));

        // ── fixedRate con tarea lenta: demostrar comportamiento sin solapamiento ─
        // pool=1 → la siguiente ejecución espera aunque el período haya vencido
        System.out.println("\n── Tarea lenta con fixedRate (tarea=400ms, rate=200ms) ──────");
        SyncTask slowTask = new SyncTask("slow-task", 400);
        scheduler.scheduleAtFixedRate("slow-task", 200, slowTask::run);

        // Dejar correr 2 segundos para observar el comportamiento
        Thread.sleep(2000);

        scheduler.shutdown();

        System.out.println("\n── Resumen ─────────────────────────────────────────────────");
        System.out.println("  fixedRate   → período desde INICIO; riesgo de solapamiento");
        System.out.println("  fixedDelay  → período desde FIN; garantiza no solapamiento");
        System.out.println("  cron        → expresión '0 */5 * * * *' = cada 5 min en seg 0");
        System.out.println("  @EnableScheduling → activa el BeanPostProcessor de scheduling");
        System.out.println("  Pool default → 1 hilo; cambiar con spring.task.scheduling.pool.size");
        System.out.println("  initialDelay → añade delay antes del primer disparo");
        System.out.printf("  Tarea slow-task ejecutada %d veces en 2s (sin solapamiento)%n",
                slowTask.count());
    }
}
