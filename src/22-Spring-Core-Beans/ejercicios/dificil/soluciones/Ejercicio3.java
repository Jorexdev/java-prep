import java.util.ArrayList;
import java.util.List;

public class Ejercicio3 {

    record PhaseRecord(String phase, long offsetMicros) {}

    static class BeanLifecycleTracker {
        private final List<PhaseRecord> phases = new ArrayList<>();
        private final long startNanos = System.nanoTime();
        private String beanName;
        private Object context;

        private void record(String phase) {
            long offset = (System.nanoTime() - startNanos) / 1_000;
            phases.add(new PhaseRecord(phase, offset));
        }

        // 1. Constructor
        BeanLifecycleTracker() {
            record("constructor");
            System.out.println("[1] constructor()");
        }

        // 2. BeanNameAware
        void setBeanName(String name) {
            this.beanName = name;
            record("setBeanName");
            System.out.println("[2] setBeanName(\"" + name + "\")");
        }

        // 3. ApplicationContextAware
        void setApplicationContext(Object ctx) {
            this.context = ctx;
            record("setApplicationContext");
            System.out.println("[3] setApplicationContext(ctx)");
        }

        // 4. @PostConstruct
        void init() {
            record("@PostConstruct");
            System.out.println("[4] @PostConstruct init()");
        }

        // 5. Uso
        void doWork(String task) {
            record("use:" + task);
            System.out.println("[5] uso: " + task);
        }

        // 6. @PreDestroy
        void destroy() {
            record("@PreDestroy");
            System.out.println("[6] @PreDestroy destroy()");
        }

        void printTimeline() {
            System.out.println("\n=== Timeline del ciclo de vida ===");
            System.out.printf("  %-28s  %s%n", "Fase", "Offset (µs)");
            System.out.println("  " + "-".repeat(45));
            for (PhaseRecord p : phases) {
                System.out.printf("  %-28s  %,d µs%n", p.phase(), p.offsetMicros());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Ciclo de vida completo de un Bean Spring ===\n");

        BeanLifecycleTracker bean = new BeanLifecycleTracker();

        // Simula lo que haría el ApplicationContext
        bean.setBeanName("miServicio");
        bean.setApplicationContext("AppContext[test]");
        bean.init();

        bean.doWork("procesar pedido #1");
        bean.doWork("enviar notificación");

        bean.destroy();

        bean.printTimeline();
    }
}
