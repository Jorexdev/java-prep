import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// Simula @Conditional de Spring: un bean se registra solo si su Condition se cumple.
// @ConditionalOnProperty, @ConditionalOnClass y @ConditionalOnMissingBean son variantes
// de Spring Boot construidas sobre esta misma interfaz.
public class ExpConditional {

    // ── Entorno simulated ─────────────────────────────────────────────────────

    // Equivale a org.springframework.context.annotation.ConditionContext
    static class Environment {
        private final Map<String, String> properties = new HashMap<>();

        void setProperty(String key, String value) { properties.put(key, value); }
        String getProperty(String key)              { return properties.get(key); }
        boolean hasProperty(String key)             { return properties.containsKey(key); }
    }

    // ── Interfaz Condition ────────────────────────────────────────────────────

    // Equivale a org.springframework.context.annotation.Condition
    interface Condition {
        boolean matches(Environment env);
    }

    // ── Condiciones concretas ─────────────────────────────────────────────────

    // @ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
    static class CacheEnabledCondition implements Condition {
        @Override
        public boolean matches(Environment env) {
            return "true".equals(env.getProperty("cache.enabled"));
        }
    }

    // @ConditionalOnProperty(name = "metrics.enabled", havingValue = "true")
    static class MetricsEnabledCondition implements Condition {
        @Override
        public boolean matches(Environment env) {
            return "true".equals(env.getProperty("metrics.enabled"));
        }
    }

    // ── Beans condicionales ────────────────────────────────────────────────────

    interface CacheService {
        void put(String key, String value);
    }

    static class RedisCache implements CacheService {
        @Override public void put(String key, String value) {
            System.out.println("  [Redis] " + key + " = " + value);
        }
    }

    interface MetricsService {
        void record(String metric, double value);
    }

    static class PrometheusMetrics implements MetricsService {
        @Override public void record(String metric, double value) {
            System.out.println("  [Prometheus] " + metric + " → " + value);
        }
    }

    // ── Registro condicional ──────────────────────────────────────────────────

    static class BeanRegistry {
        private final Map<String, Object> beans = new HashMap<>();

        // Registra el bean solo si la condición se cumple
        // @Conditional(condition) en Spring Boot
        <T> void registerIf(String name, Condition condition, Supplier<T> factory, Environment env) {
            if (condition.matches(env)) {
                Object bean = factory.get();
                beans.put(name, bean);
                System.out.println("  [Registry] Bean '" + name + "' registrado ✓");
            } else {
                System.out.println("  [Registry] Bean '" + name + "' OMITIDO (condición no cumplida)");
            }
        }

        boolean contains(String name) { return beans.containsKey(name); }

        @SuppressWarnings("unchecked")
        <T> T get(String name) {
            Object bean = beans.get(name);
            if (bean == null) throw new IllegalStateException("Bean '" + name + "' no disponible");
            return (T) bean;
        }
    }

    static void configurar(Environment env) {
        System.out.println("Propiedades activas: cache.enabled="
            + env.getProperty("cache.enabled") + ", metrics.enabled="
            + env.getProperty("metrics.enabled"));

        BeanRegistry registry = new BeanRegistry();
        registry.registerIf("cache",   new CacheEnabledCondition(),   RedisCache::new,       env);
        registry.registerIf("metrics", new MetricsEnabledCondition(), PrometheusMetrics::new, env);

        System.out.println("Beans disponibles:");
        if (registry.contains("cache")) {
            CacheService cache = registry.get("cache");
            cache.put("session:1", "activa");
        } else {
            System.out.println("  cache → no registrado");
        }
        if (registry.contains("metrics")) {
            MetricsService metrics = registry.get("metrics");
            metrics.record("http.requests", 42.0);
        } else {
            System.out.println("  metrics → no registrado");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Sin propiedades → ambos beans omitidos ===");
        configurar(new Environment());

        System.out.println("\n=== cache.enabled=true → solo cache registrado ===");
        Environment env2 = new Environment();
        env2.setProperty("cache.enabled", "true");
        configurar(env2);

        System.out.println("\n=== Ambas propiedades activas ===");
        Environment env3 = new Environment();
        env3.setProperty("cache.enabled",   "true");
        env3.setProperty("metrics.enabled", "true");
        configurar(env3);
    }
}
