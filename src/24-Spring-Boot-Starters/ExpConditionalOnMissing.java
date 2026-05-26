import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// Simula @ConditionalOnMissingBean: la autoconfiguración solo actúa si el usuario
// no ha registrado ya su propio bean del mismo tipo.
// Regla de oro: el bean del usuario siempre tiene prioridad sobre el auto-configurado.
public class ExpConditionalOnMissing {

    // ── Tipos de beans ────────────────────────────────────────────────────────

    interface ObjectMapper {
        String serialize(Object obj);
    }

    // Bean auto-configurado por Spring Boot (Jackson por defecto)
    static class DefaultObjectMapper implements ObjectMapper {
        @Override
        public String serialize(Object obj) {
            return "{\"default\":\"" + obj + "\"}";
        }
    }

    // Bean personalizado del usuario (sobreescribe el de autoconfiguración)
    static class CustomObjectMapper implements ObjectMapper {
        private final String prefix;

        CustomObjectMapper(String prefix) { this.prefix = prefix; }

        @Override
        public String serialize(Object obj) {
            return prefix + "{\"custom\":\"" + obj + "\"}";
        }
    }

    // ── Registro con soporte @ConditionalOnMissingBean ────────────────────────

    static class AutoConfigRegistry {
        private final Map<Class<?>, Object> beans = new HashMap<>();

        // Registra un bean sin condición — bean del usuario
        <T> void register(Class<T> type, T bean) {
            beans.put(type, bean);
            System.out.println("  [UserConfig] Registrado: " + bean.getClass().getSimpleName());
        }

        // Registra solo si no hay ya un bean del mismo tipo — @ConditionalOnMissingBean
        <T> void registerIfMissing(Class<T> type, Supplier<T> factory) {
            if (beans.containsKey(type)) {
                System.out.println("  [AutoConfig] " + type.getSimpleName()
                    + " ya existe → OMITIENDO autoconfiguración");
            } else {
                T bean = factory.get();
                beans.put(type, bean);
                System.out.println("  [AutoConfig] Registrado por defecto: "
                    + bean.getClass().getSimpleName());
            }
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type) {
            T bean = (T) beans.get(type);
            if (bean == null) throw new IllegalStateException("No hay bean de tipo: " + type.getSimpleName());
            return bean;
        }
    }

    static void ejecutar(boolean conBeanPersonalizado) {
        AutoConfigRegistry registry = new AutoConfigRegistry();

        // Los beans del usuario se registran primero (en @Configuration del usuario)
        if (conBeanPersonalizado) {
            registry.register(ObjectMapper.class, new CustomObjectMapper(">>"));
        }

        // La autoconfiguración viene después y respeta lo que ya existe
        registry.registerIfMissing(ObjectMapper.class, DefaultObjectMapper::new);

        ObjectMapper mapper = registry.getBean(ObjectMapper.class);
        System.out.println("  Resultado: " + mapper.serialize("Hola"));
    }

    public static void main(String[] args) {
        System.out.println("=== Sin bean de usuario → DefaultObjectMapper auto-configurado ===");
        ejecutar(false);

        System.out.println("\n=== Con bean de usuario → autoconfiguración omitida ===");
        ejecutar(true);

        System.out.println("\n=== Principio ===");
        System.out.println("@ConditionalOnMissingBean garantiza que la autoconfiguración");
        System.out.println("nunca pisa un bean que el usuario haya definido explícitamente.");
    }
}
