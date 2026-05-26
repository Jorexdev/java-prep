import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Simula el mecanismo de spring.factories / META-INF/spring/AutoConfiguration.imports.
// Spring Boot 2.x usaba spring.factories; 3.x usa AutoConfiguration.imports.
// Ambos definen qué clases de auto-configuración cargar al arrancar.
public class ExpSpringFactories {

    // ── Interfaz base de auto-configuración ───────────────────────────────────

    interface AutoConfig {
        // @ConditionalOnProperty — devuelve false para ser omitido
        default boolean condition(Map<String, String> env) { return true; }
        void configure(Map<String, Object> beanRegistry, Map<String, String> env);
        String name();
    }

    // ── Auto-configuraciones concretas ────────────────────────────────────────

    static class DataSourceAutoConfig implements AutoConfig {
        @Override public String name() { return "DataSourceAutoConfig"; }

        @Override
        public boolean condition(Map<String, String> env) {
            // @ConditionalOnProperty(name="spring.datasource.url")
            return env.containsKey("spring.datasource.url");
        }

        @Override
        public void configure(Map<String, Object> beans, Map<String, String> env) {
            String url = env.get("spring.datasource.url");
            beans.put("dataSource", "DataSource(" + url + ")");
            System.out.println("  [DataSourceAutoConfig] dataSource registrado → " + url);
        }
    }

    static class TransactionManagerAutoConfig implements AutoConfig {
        @Override public String name() { return "TransactionManagerAutoConfig"; }

        @Override
        public boolean condition(Map<String, String> env) {
            // Solo activo si hay un dataSource
            return env.containsKey("spring.datasource.url");
        }

        @Override
        public void configure(Map<String, Object> beans, Map<String, String> env) {
            beans.put("transactionManager", "DataSourceTransactionManager");
            System.out.println("  [TransactionManagerAutoConfig] transactionManager registrado");
        }
    }

    static class CacheAutoConfig implements AutoConfig {
        @Override public String name() { return "CacheAutoConfig"; }

        @Override
        public boolean condition(Map<String, String> env) {
            // @ConditionalOnProperty(name="spring.cache.type")
            return env.containsKey("spring.cache.type");
        }

        @Override
        public void configure(Map<String, Object> beans, Map<String, String> env) {
            String type = env.get("spring.cache.type");
            beans.put("cacheManager", type.toUpperCase() + "CacheManager");
            System.out.println("  [CacheAutoConfig] cacheManager → " + type);
        }
    }

    // ── FactoriesLoader ───────────────────────────────────────────────────────

    // Simula SpringFactoriesLoader / ImportCandidates (Spring Boot 3)
    static class FactoriesLoader {
        // Simula META-INF/spring/AutoConfiguration.imports:
        //   com.example.DataSourceAutoConfig
        //   com.example.TransactionManagerAutoConfig
        //   com.example.CacheAutoConfig
        private static final String FACTORIES_CONTENT =
            "org.springframework.boot.autoconfigure.EnableAutoConfiguration=" +
            "DataSourceAutoConfig,TransactionManagerAutoConfig,CacheAutoConfig";

        // Parsea el fichero de factories y devuelve las clases registradas
        static List<String> loadClassNames(String factoriesContent) {
            String[] parts = factoriesContent.split("=", 2);
            if (parts.length < 2) return List.of();
            return Arrays.asList(parts[1].split(","));
        }

        // Instancia cada clase por nombre usando el mapa de clases disponibles
        static List<AutoConfig> instantiate(List<String> classNames,
                                            Map<String, AutoConfig> available) {
            List<AutoConfig> result = new ArrayList<>();
            for (String name : classNames) {
                AutoConfig config = available.get(name.trim());
                if (config != null) {
                    result.add(config);
                } else {
                    System.out.println("  [FactoriesLoader] Clase no encontrada: " + name);
                }
            }
            return result;
        }
    }

    public static void main(String[] args) {
        // Registro de clases disponibles (en producción Spring usa el classpath)
        Map<String, AutoConfig> available = new HashMap<>();
        available.put("DataSourceAutoConfig",        new DataSourceAutoConfig());
        available.put("TransactionManagerAutoConfig", new TransactionManagerAutoConfig());
        available.put("CacheAutoConfig",              new CacheAutoConfig());

        // Carga de factories — Spring hace esto con ClassPathResource en el arranque
        List<String> classNames = FactoriesLoader.loadClassNames(
            FactoriesLoader.FACTORIES_CONTENT);
        System.out.println("=== Clases en AutoConfiguration.imports ===");
        classNames.forEach(c -> System.out.println("  " + c));

        List<AutoConfig> autoConfigs = FactoriesLoader.instantiate(classNames, available);

        System.out.println("\n=== Ejecución sin base de datos ===");
        Map<String, String> envSinDB  = new HashMap<>();
        envSinDB.put("spring.cache.type", "redis");
        Map<String, Object> beans1 = new HashMap<>();
        for (AutoConfig cfg : autoConfigs) {
            if (cfg.condition(envSinDB)) {
                cfg.configure(beans1, envSinDB);
            } else {
                System.out.println("  [SKIP] " + cfg.name() + " (condición no cumplida)");
            }
        }

        System.out.println("\n=== Ejecución con base de datos y cache ===");
        Map<String, String> envCompleto = new HashMap<>();
        envCompleto.put("spring.datasource.url", "jdbc:postgresql://localhost/app");
        envCompleto.put("spring.cache.type",     "caffeine");
        Map<String, Object> beans2 = new HashMap<>();
        for (AutoConfig cfg : autoConfigs) {
            if (cfg.condition(envCompleto)) {
                cfg.configure(beans2, envCompleto);
            } else {
                System.out.println("  [SKIP] " + cfg.name());
            }
        }
        System.out.println("Beans registrados: " + beans2.keySet());
    }
}
