import java.util.LinkedHashMap;
import java.util.Map;

// Ejercicio 2 (Fácil) — @ConditionalOnMissingBean
// Registra el fallback solo si no hay ya un bean del mismo tipo
public class Ejercicio2 {

    interface MessageService {
        String send(String message);
    }

    // Bean fallback (default auto-config)
    static class DefaultMessageService implements MessageService {
        @Override
        public String send(String message) {
            return "[DEFAULT] Enviado: " + message;
        }
    }

    // Bean personalizado del usuario
    static class CustomMessageService implements MessageService {
        private final String prefix;

        CustomMessageService(String prefix) { this.prefix = prefix; }

        @Override
        public String send(String message) {
            return "[" + prefix + "] Enviado: " + message;
        }
    }

    static class BeanContainer {
        private final Map<Class<?>, Object> beans = new LinkedHashMap<>();

        public <T> void register(Class<T> type, T bean) {
            beans.put(type, bean);
            System.out.println("[Container] Registrado bean: "
                    + bean.getClass().getSimpleName() + " para " + type.getSimpleName());
        }

        public boolean hasBeanOfType(Class<?> type) {
            return beans.containsKey(type);
        }

        @SuppressWarnings("unchecked")
        public <T> T getBean(Class<T> type) {
            return (T) beans.get(type);
        }

        // Simula @ConditionalOnMissingBean
        public <T> void registerIfMissing(Class<T> type, T fallback) {
            if (hasBeanOfType(type)) {
                System.out.println("[Condition] Bean de tipo " + type.getSimpleName()
                        + " ya existe → OMITIENDO fallback "
                        + fallback.getClass().getSimpleName());
            } else {
                System.out.println("[Condition] No hay bean de tipo " + type.getSimpleName()
                        + " → registrando fallback " + fallback.getClass().getSimpleName());
                register(type, fallback);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @ConditionalOnMissingBean ===");
        System.out.println();

        // Escenario 1: sin bean previo → registra fallback DefaultMessageService
        System.out.println("--- Escenario 1: sin bean previo ---");
        BeanContainer container1 = new BeanContainer();
        container1.registerIfMissing(MessageService.class, new DefaultMessageService());
        System.out.println("Bean activo: " + container1.getBean(MessageService.class).getClass().getSimpleName());
        System.out.println("Resultado: " + container1.getBean(MessageService.class).send("hola"));

        System.out.println();

        // Escenario 2: con bean previo → NO registra fallback
        System.out.println("--- Escenario 2: con CustomMessageService ya registrado ---");
        BeanContainer container2 = new BeanContainer();
        // El usuario registra su propio bean primero
        container2.register(MessageService.class, new CustomMessageService("SMTP"));
        // La auto-config intenta registrar el fallback → debe ser ignorado
        container2.registerIfMissing(MessageService.class, new DefaultMessageService());
        System.out.println("Bean activo: " + container2.getBean(MessageService.class).getClass().getSimpleName());
        System.out.println("Resultado: " + container2.getBean(MessageService.class).send("hola"));

        System.out.println();

        // Escenario 3: múltiples tipos de beans, fallback selectivo
        System.out.println("--- Escenario 3: fallback selectivo por tipo ---");
        BeanContainer container3 = new BeanContainer();
        container3.register(MessageService.class, new CustomMessageService("KAFKA"));
        container3.registerIfMissing(MessageService.class, new DefaultMessageService()); // omitido
        System.out.println("MessageService: " + container3.getBean(MessageService.class).getClass().getSimpleName());
    }
}
