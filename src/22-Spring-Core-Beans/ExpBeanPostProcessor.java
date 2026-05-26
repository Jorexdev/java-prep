import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// Simula BeanPostProcessor: intercepta cada bean antes y después de su inicialización.
// Spring llama a todos los BeanPostProcessor registrados en orden para cada bean.
public class ExpBeanPostProcessor {

    // ── Marcador de validación ────────────────────────────────────────────────
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NotNull {}

    // ── Beans de ejemplo ─────────────────────────────────────────────────────

    static class ServicioEmail {
        @NotNull String smtpHost;
        @NotNull String fromAddress;

        ServicioEmail(String smtpHost, String fromAddress) {
            this.smtpHost    = smtpHost;
            this.fromAddress = fromAddress;
        }

        public void enviar(String dest) {
            System.out.println("  Email enviado a " + dest + " via " + smtpHost);
        }
    }

    static class ServicioInvalido {
        @NotNull String apiKey;   // null a propósito → ValidationPostProcessor lo detecta

        ServicioInvalido() { /* apiKey queda null */ }
    }

    // ── Interfaz BeanPostProcessor ────────────────────────────────────────────

    interface BeanPostProcessor {
        // Se ejecuta antes de init() del bean (p.ej., antes de @PostConstruct)
        default Object postProcessBeforeInit(Object bean, String name) { return bean; }
        // Se ejecuta después de init() del bean (p.ej., después de @PostConstruct)
        default Object postProcessAfterInit(Object bean, String name) { return bean; }
    }

    // ── Procesadores ──────────────────────────────────────────────────────────

    // Registra el ciclo de vida en consola — util para diagnosticar el orden de inicialización
    static class LoggingPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessBeforeInit(Object bean, String name) {
            System.out.println("  [LOG] BEFORE INIT → " + name + " (" + bean.getClass().getSimpleName() + ")");
            return bean;
        }

        @Override
        public Object postProcessAfterInit(Object bean, String name) {
            System.out.println("  [LOG] AFTER  INIT → " + name + " listo");
            return bean;
        }
    }

    // Valida que los campos @NotNull no sean null al finalizar la inicialización
    static class ValidationPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessAfterInit(Object bean, String name) {
            for (Field field : bean.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(NotNull.class)) {
                    field.setAccessible(true);
                    try {
                        if (field.get(bean) == null) {
                            throw new IllegalStateException(
                                "Bean '" + name + "': campo @NotNull '" + field.getName() + "' es null");
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            System.out.println("  [VALID] " + name + " pasó validación @NotNull");
            return bean;
        }
    }

    // ── Cadena de procesadores ────────────────────────────────────────────────

    // Spring aplica los BeanPostProcessor en el orden en que están registrados
    static class PostProcessorChain {
        private final List<BeanPostProcessor> processors = new ArrayList<>();

        void add(BeanPostProcessor processor) { processors.add(processor); }

        // Simula el ciclo completo: before → init() → after
        Object process(Object bean, String name) {
            for (BeanPostProcessor pp : processors) {
                bean = pp.postProcessBeforeInit(bean, name);
            }
            // Aquí Spring llama a @PostConstruct / InitializingBean.afterPropertiesSet()
            System.out.println("  [INIT] " + name + ".init()");
            for (BeanPostProcessor pp : processors) {
                bean = pp.postProcessAfterInit(bean, name);
            }
            return bean;
        }
    }

    public static void main(String[] args) {
        PostProcessorChain chain = new PostProcessorChain();
        // El orden de registro determina el orden de ejecución
        chain.add(new LoggingPostProcessor());
        chain.add(new ValidationPostProcessor());

        System.out.println("=== Bean válido ===");
        chain.process(new ServicioEmail("smtp.gmail.com", "noreply@example.com"), "servicioEmail");

        System.out.println("\n=== Bean con campo @NotNull null ===");
        try {
            chain.process(new ServicioInvalido(), "servicioInvalido");
        } catch (IllegalStateException e) {
            System.out.println("  Error detectado: " + e.getMessage());
        }

        System.out.println("\n=== Nota ===");
        System.out.println("El orden de los BeanPostProcessor importa:");
        System.out.println("LoggingPostProcessor primero → registra todo, incluso los fallos de validación.");
    }
}
