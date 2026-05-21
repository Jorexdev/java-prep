import java.util.LinkedHashMap;
import java.util.Map;

// Ejercicio 6 (Fácil) — Default beans
// WebMvcAutoConfig registra beans solo si no existen ya
public class Ejercicio6 {

    // Tipos de beans que gestiona WebMvcAutoConfig
    static class ObjectMapper {
        private final String source;
        ObjectMapper(String source) { this.source = source; }
        @Override public String toString() { return "ObjectMapper[" + source + "]"; }
    }

    static class MessageConverter {
        private final String source;
        MessageConverter(String source) { this.source = source; }
        @Override public String toString() { return "MessageConverter[" + source + "]"; }
    }

    static class ExceptionResolver {
        private final String source;
        ExceptionResolver(String source) { this.source = source; }
        @Override public String toString() { return "ExceptionResolver[" + source + "]"; }
    }

    static class BeanContainer {
        private final Map<Class<?>, Object> beans = new LinkedHashMap<>();

        public <T> void register(Class<T> type, T bean) {
            beans.put(type, bean);
            System.out.println("[Container] Registrado manualmente: " + bean);
        }

        public boolean hasBeanOfType(Class<?> type) {
            return beans.containsKey(type);
        }

        @SuppressWarnings("unchecked")
        public <T> T getBean(Class<T> type) {
            return (T) beans.get(type);
        }
    }

    static class WebMvcAutoConfig {

        public void configure(BeanContainer container) {
            System.out.println("[WebMvcAutoConfig] Configurando beans por defecto...");

            // @ConditionalOnMissingBean(ObjectMapper.class)
            if (!container.hasBeanOfType(ObjectMapper.class)) {
                container.register(ObjectMapper.class, new ObjectMapper("default-auto-config"));
                System.out.println("  [OK] ObjectMapper registrado (default)");
            } else {
                System.out.println("  [SKIP] ObjectMapper ya existe → no sobreescribir");
            }

            // @ConditionalOnMissingBean(MessageConverter.class)
            if (!container.hasBeanOfType(MessageConverter.class)) {
                container.register(MessageConverter.class, new MessageConverter("default-auto-config"));
                System.out.println("  [OK] MessageConverter registrado (default)");
            } else {
                System.out.println("  [SKIP] MessageConverter ya existe → no sobreescribir");
            }

            // @ConditionalOnMissingBean(ExceptionResolver.class)
            if (!container.hasBeanOfType(ExceptionResolver.class)) {
                container.register(ExceptionResolver.class, new ExceptionResolver("default-auto-config"));
                System.out.println("  [OK] ExceptionResolver registrado (default)");
            } else {
                System.out.println("  [SKIP] ExceptionResolver ya existe → no sobreescribir");
            }
        }
    }

    static void printContainer(BeanContainer container) {
        System.out.println("  ObjectMapper     : "
                + container.getBean(ObjectMapper.class));
        System.out.println("  MessageConverter : "
                + container.getBean(MessageConverter.class));
        System.out.println("  ExceptionResolver: "
                + container.getBean(ExceptionResolver.class));
    }

    public static void main(String[] args) {
        WebMvcAutoConfig autoConfig = new WebMvcAutoConfig();

        System.out.println("=== Default beans (@ConditionalOnMissingBean) ===");
        System.out.println();

        // Escenario 1: contenedor vacío → registra los 3 defaults
        System.out.println("--- Escenario 1: contenedor vacío ---");
        BeanContainer container1 = new BeanContainer();
        autoConfig.configure(container1);
        System.out.println("Estado final:");
        printContainer(container1);

        System.out.println();

        // Escenario 2: ObjectMapper ya registrado → no sobreescribir ese
        System.out.println("--- Escenario 2: ObjectMapper propio ya registrado ---");
        BeanContainer container2 = new BeanContainer();
        container2.register(ObjectMapper.class, new ObjectMapper("user-custom"));
        System.out.println("--- Ejecutando WebMvcAutoConfig ---");
        autoConfig.configure(container2);
        System.out.println("Estado final:");
        printContainer(container2);

        System.out.println();

        // Escenario 3: los 3 ya registrados → ninguno sobreescrito
        System.out.println("--- Escenario 3: todos los beans ya registrados ---");
        BeanContainer container3 = new BeanContainer();
        container3.register(ObjectMapper.class, new ObjectMapper("user-1"));
        container3.register(MessageConverter.class, new MessageConverter("user-2"));
        container3.register(ExceptionResolver.class, new ExceptionResolver("user-3"));
        System.out.println("--- Ejecutando WebMvcAutoConfig ---");
        autoConfig.configure(container3);
        System.out.println("Estado final:");
        printContainer(container3);
    }
}
