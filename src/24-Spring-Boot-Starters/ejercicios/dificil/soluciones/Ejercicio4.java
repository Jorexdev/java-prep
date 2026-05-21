import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Ejercicio 4 (Difícil) — Test auto-config subset
// TestContextFactory: solo activa las auto-configs de la lista permitida
// Simula @ImportAutoConfiguration
public class Ejercicio4 {

    interface AutoConfig {
        String getName();
        void configure(Map<String, Object> beans);
    }

    // Las 8 auto-configs disponibles en la aplicación
    static class WebAutoConfig implements AutoConfig {
        @Override public String getName() { return "WebAutoConfig"; }
        @Override public void configure(Map<String, Object> beans) {
            beans.put("dispatcherServlet", "DispatcherServlet");
            beans.put("handlerMapping", "RequestMappingHandlerMapping");
        }
    }

    static class JacksonAutoConfig implements AutoConfig {
        @Override public String getName() { return "JacksonAutoConfig"; }
        @Override public void configure(Map<String, Object> beans) {
            beans.put("objectMapper", "ObjectMapper[default]");
        }
    }

    static class DataSourceAutoConfig implements AutoConfig {
        @Override public String getName() { return "DataSourceAutoConfig"; }
        @Override public void configure(Map<String, Object> beans) {
            beans.put("dataSource", "HikariDataSource");
            beans.put("jdbcTemplate", "JdbcTemplate");
        }
    }

    static class JpaAutoConfig implements AutoConfig {
        @Override public String getName() { return "JpaAutoConfig"; }
        @Override public void configure(Map<String, Object> beans) {
            beans.put("entityManagerFactory", "LocalContainerEntityManagerFactory");
            beans.put("transactionManager", "JpaTransactionManager");
        }
    }

    static class SecurityAutoConfig implements AutoConfig {
        @Override public String getName() { return "SecurityAutoConfig"; }
        @Override public void configure(Map<String, Object> beans) {
            beans.put("securityFilterChain", "SecurityFilterChain");
            beans.put("userDetailsService", "InMemoryUserDetailsManager");
        }
    }

    static class CacheAutoConfig implements AutoConfig {
        @Override public String getName() { return "CacheAutoConfig"; }
        @Override public void configure(Map<String, Object> beans) {
            beans.put("cacheManager", "ConcurrentMapCacheManager");
        }
    }

    static class ActuatorAutoConfig implements AutoConfig {
        @Override public String getName() { return "ActuatorAutoConfig"; }
        @Override public void configure(Map<String, Object> beans) {
            beans.put("healthEndpoint", "/actuator/health");
            beans.put("infoEndpoint", "/actuator/info");
        }
    }

    static class MetricsAutoConfig implements AutoConfig {
        @Override public String getName() { return "MetricsAutoConfig"; }
        @Override public void configure(Map<String, Object> beans) {
            beans.put("metricsRegistry", "SimpleMeterRegistry");
            beans.put("prometheusEndpoint", "/actuator/prometheus");
        }
    }

    // Contexto de aplicación completo
    static class ApplicationContext {
        final Map<String, Object> beans = new LinkedHashMap<>();
        final List<String> appliedConfigs = new ArrayList<>();

        void apply(AutoConfig ac) {
            ac.configure(beans);
            appliedConfigs.add(ac.getName());
        }

        void printSummary(String label) {
            System.out.println("--- " + label + " ---");
            System.out.println("Auto-configs aplicadas (" + appliedConfigs.size() + "): "
                    + appliedConfigs);
            System.out.println("Beans registrados (" + beans.size() + "):");
            beans.forEach((k, v) -> System.out.println("  " + k + " = " + v));
        }
    }

    // TestContextFactory: activa solo un subset de auto-configs
    static class TestContextFactory {
        private final Set<String> allowed;
        private final List<AutoConfig> allAutoConfigs;

        TestContextFactory(List<String> allowed, List<AutoConfig> allAutoConfigs) {
            this.allowed = Set.copyOf(allowed);
            this.allAutoConfigs = allAutoConfigs;
        }

        /**
         * Crea un contexto que solo aplica las auto-configs permitidas.
         * Simula @ImportAutoConfiguration({JacksonAutoConfig.class, WebAutoConfig.class, ...})
         */
        public ApplicationContext createTestContext() {
            ApplicationContext ctx = new ApplicationContext();
            System.out.println("[TestContextFactory] Auto-configs permitidas: " + allowed);
            for (AutoConfig ac : allAutoConfigs) {
                if (allowed.contains(ac.getName())) {
                    System.out.println("  [OK]   Aplicando: " + ac.getName());
                    ctx.apply(ac);
                } else {
                    System.out.println("  [SKIP] Omitiendo: " + ac.getName());
                }
            }
            return ctx;
        }

        /** Crea el contexto completo sin filtros */
        public ApplicationContext createFullContext() {
            ApplicationContext ctx = new ApplicationContext();
            for (AutoConfig ac : allAutoConfigs) {
                ctx.apply(ac);
            }
            return ctx;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Test auto-config subset (@ImportAutoConfiguration) ===");
        System.out.println();

        List<AutoConfig> allConfigs = List.of(
            new WebAutoConfig(),
            new JacksonAutoConfig(),
            new DataSourceAutoConfig(),
            new JpaAutoConfig(),
            new SecurityAutoConfig(),
            new CacheAutoConfig(),
            new ActuatorAutoConfig(),
            new MetricsAutoConfig()
        );

        System.out.println("Auto-configs disponibles (" + allConfigs.size() + "):");
        allConfigs.forEach(ac -> System.out.println("  - " + ac.getName()));
        System.out.println();

        // Para tests de un controlador REST solo necesitamos Web + Jackson + Security
        List<String> testSubset = List.of("WebAutoConfig", "JacksonAutoConfig", "SecurityAutoConfig");

        TestContextFactory factory = new TestContextFactory(testSubset, allConfigs);

        // Contexto de test con subset
        System.out.println("=== Contexto de TEST ===");
        ApplicationContext testCtx = factory.createTestContext();
        System.out.println();
        testCtx.printSummary("Test context (@ImportAutoConfiguration)");

        System.out.println();

        // Contexto completo (producción)
        System.out.println("=== Contexto COMPLETO (producción) ===");
        ApplicationContext fullCtx = factory.createFullContext();
        fullCtx.printSummary("Full context");

        System.out.println();
        System.out.println("=== Comparación ===");
        System.out.println("Test: " + testCtx.beans.size() + " beans, "
                + testCtx.appliedConfigs.size() + " auto-configs");
        System.out.println("Full: " + fullCtx.beans.size() + " beans, "
                + fullCtx.appliedConfigs.size() + " auto-configs");
        System.out.println("Reducción: " + (fullCtx.beans.size() - testCtx.beans.size())
                + " beans menos en test → inicio más rápido");
    }
}
