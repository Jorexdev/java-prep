import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

// @Conditional es el mecanismo central de la autoconfiguración de Spring Boot.
// @ConditionalOnClass, @ConditionalOnMissingBean, @ConditionalOnProperty son variantes
// de Spring Boot que internamente usan esta misma interfaz Condition.
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

    // Conditions — equivalen a @ConditionalOnProperty en Spring Boot
    static class ConditionRedis implements Condition {
        @Override
        public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata meta) {
            return "redis".equals(System.getProperty("cache.type"));
        }
    }

    static class ConditionMemory implements Condition {
        @Override
        public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata meta) {
            return !"redis".equals(System.getProperty("cache.type"));
        }
    }

    @Configuration
    static class CacheAutoConfig {

        // Solo se registra si cache.type=redis — como @ConditionalOnProperty
        @Bean
        @Conditional(ConditionRedis.class)
        Cache redisCache() { return new RedisCache(); }

        // Fallback — como @ConditionalOnMissingBean: activo si no hay Redis
        @Bean
        @Conditional(ConditionMemory.class)
        Cache memoryCache() { return new MemoryCache(); }
    }

    public static void main(String[] args) {
        System.out.println("=== Sin configuración → Memory cache ===");
        try (var ctx = new AnnotationConfigApplicationContext(CacheAutoConfig.class)) {
            Cache cache = ctx.getBean(Cache.class);
            cache.set("usuario:1", "jorex");
            System.out.println(cache.get("usuario:1"));
        }

        System.out.println("\n=== Con cache.type=redis → Redis cache ===");
        System.setProperty("cache.type", "redis");
        try (var ctx = new AnnotationConfigApplicationContext(CacheAutoConfig.class)) {
            Cache cache = ctx.getBean(Cache.class);
            cache.set("usuario:1", "jorex");
            System.out.println(cache.get("usuario:1"));
        }
        System.clearProperty("cache.type");
    }
}
