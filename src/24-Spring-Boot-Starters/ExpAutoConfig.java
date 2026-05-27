import java.util.HashMap;
import java.util.Map;

// @Conditional es el mecanismo central de la autoconfiguración de Spring Boot.
// @ConditionalOnClass, @ConditionalOnMissingBean, @ConditionalOnProperty son variantes
// de Spring Boot que internamente usan la misma idea: evaluar una condición antes de registrar.
public class ExpAutoConfig {

    interface Cache {
        void set(String key, String value);
        String get(String key);
    }

    // Simulación de Redis — activo si la propiedad de sistema "cache.type=redis"
    static class RedisCache implements Cache {
        @Override public void set(String key, String value) {
            System.out.println("[REDIS] SET " + key + " = " + value);
        }
        @Override public String get(String key) {
            return "[REDIS] GET " + key;
        }
    }

    // Fallback en memoria — activo si Redis no está configurado
    static class MemoryCache implements Cache {
        @Override public void set(String key, String value) {
            System.out.println("[MEMORY] map[" + key + "] = " + value);
        }
        @Override public String get(String key) {
            return "[MEMORY] map[" + key + "]";
        }
    }

    // Equivalente a la interfaz org.springframework.context.annotation.Condition
    @FunctionalInterface
    interface Condition {
        boolean matches(Map<String, String> properties);
    }

    // Registro condicional de beans — simula la autoconfiguración de Spring Boot
    static class ConditionalRegistry {
        private final Map<Class<?>, Object> beans       = new HashMap<>();
        private final Map<String, String>   properties;

        ConditionalRegistry(Map<String, String> properties) {
            this.properties = properties;
        }

        // Registra el bean solo si la condición se cumple.
        // Equivale a anotar un @Bean con @ConditionalOnProperty / @ConditionalOnMissingBean.
        <T> void registerIf(Class<T> type, Condition condition, java.util.function.Supplier<T> factory) {
            if (condition.matches(properties)) {
                // Solo registra si el tipo aún no tiene un bean — @ConditionalOnMissingBean
                beans.putIfAbsent(type, factory.get());
            }
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type) {
            Object bean = beans.get(type);
            if (bean == null) throw new IllegalStateException("Bean no encontrado: " + type.getSimpleName());
            return (T) bean;
        }
    }

    // Autoconfiguración del cache — equivalente a una clase @Configuration de Spring Boot
    static class CacheAutoConfig {

        static void configure(ConditionalRegistry registry) {
            // Solo se registra si cache.type=redis — como @ConditionalOnProperty(name="cache.type", havingValue="redis")
            registry.registerIf(
                Cache.class,
                props -> "redis".equals(props.get("cache.type")),  // @ConditionalOnProperty
                RedisCache::new
            );

            // Fallback — como @ConditionalOnMissingBean: activo si no hay Redis ya registrado
            registry.registerIf(
                Cache.class,
                props -> !"redis".equals(props.get("cache.type")),  // @ConditionalOnMissingBean
                MemoryCache::new
            );
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Sin configuración → Memory cache ===");
        {
            Map<String, String> props = new HashMap<>();
            ConditionalRegistry registry = new ConditionalRegistry(props);
            CacheAutoConfig.configure(registry);
            Cache cache = registry.getBean(Cache.class);
            cache.set("usuario:1", "jorex");
            System.out.println(cache.get("usuario:1"));
        }

        System.out.println("\n=== Con cache.type=redis → Redis cache ===");
        {
            Map<String, String> props = new HashMap<>();
            props.put("cache.type", "redis");
            ConditionalRegistry registry = new ConditionalRegistry(props);
            CacheAutoConfig.configure(registry);
            Cache cache = registry.getBean(Cache.class);
            cache.set("usuario:1", "jorex");
            System.out.println(cache.get("usuario:1"));
        }
    }
}
