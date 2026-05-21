import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Ejercicio 1 (Medio) — Spring factories simulation
// Simula META-INF/spring.factories: carga auto-configs por nombre y las ejecuta
public class Ejercicio1 {

    // Interfaz que deben implementar todas las auto-configs
    interface AutoConfiguration {
        void configure();
        String getName();
    }

    // Auto-configs de ejemplo (en producción estas son clases separadas)
    static class WebAutoConfig implements AutoConfiguration {
        @Override public String getName() { return "WebAutoConfig"; }
        @Override public void configure() {
            System.out.println("  [WebAutoConfig] Registrando DispatcherServlet, HandlerMapping, ViewResolver");
        }
    }

    static class DataSourceAutoConfig implements AutoConfiguration {
        @Override public String getName() { return "DataSourceAutoConfig"; }
        @Override public void configure() {
            System.out.println("  [DataSourceAutoConfig] Registrando HikariCP DataSource, JdbcTemplate");
        }
    }

    static class SecurityAutoConfig implements AutoConfiguration {
        @Override public String getName() { return "SecurityAutoConfig"; }
        @Override public void configure() {
            System.out.println("  [SecurityAutoConfig] Registrando SecurityFilterChain, UserDetailsService");
        }
    }

    static class JacksonAutoConfig implements AutoConfiguration {
        @Override public String getName() { return "JacksonAutoConfig"; }
        @Override public void configure() {
            System.out.println("  [JacksonAutoConfig] Registrando ObjectMapper con módulos por defecto");
        }
    }

    /**
     * Simula la carga de spring.factories.
     * Clave: tipo de auto-config (ej. EnableAutoConfiguration)
     * Valor: lista de nombres de clase completamente cualificados
     */
    static class SpringFactoriesLoader {

        private final Map<String, List<String>> factories;

        SpringFactoriesLoader(Map<String, List<String>> factories) {
            this.factories = factories;
        }

        /**
         * Instancia todas las auto-configs registradas bajo la clave dada
         * usando reflection y las ejecuta.
         */
        public void load(String key, ClassRegistry registry) {
            List<String> classNames = factories.getOrDefault(key, List.of());
            System.out.println("spring.factories[" + key + "] → " + classNames.size() + " entries");
            System.out.println();

            List<AutoConfiguration> loaded = new ArrayList<>();

            for (String className : classNames) {
                AutoConfiguration ac = registry.instantiate(className);
                if (ac != null) {
                    loaded.add(ac);
                    System.out.println("  Cargado: " + className);
                } else {
                    System.out.println("  ERROR: no se pudo instanciar " + className);
                }
            }

            System.out.println();
            System.out.println("Ejecutando " + loaded.size() + " auto-configuraciones:");
            for (AutoConfiguration ac : loaded) {
                System.out.println("[" + ac.getName() + "]");
                ac.configure();
            }
        }
    }

    // Simula un registry de clases (evita hardcodear Class.forName con nombres largos)
    static class ClassRegistry {
        private final Map<String, AutoConfiguration> registry = new LinkedHashMap<>();

        public void register(String name, AutoConfiguration instance) {
            registry.put(name, instance);
        }

        public AutoConfiguration instantiate(String name) {
            return registry.get(name);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Spring factories simulation ===");
        System.out.println();

        // Simula META-INF/spring.factories
        Map<String, List<String>> factories = new LinkedHashMap<>();
        factories.put("EnableAutoConfiguration", List.of(
            "WebAutoConfig",
            "DataSourceAutoConfig",
            "SecurityAutoConfig",
            "JacksonAutoConfig"
        ));

        System.out.println("Contenido de spring.factories:");
        factories.forEach((k, v) -> {
            System.out.println("  " + k + "=\\");
            v.forEach(c -> System.out.println("    " + c + ",\\"));
        });
        System.out.println();

        // Registro de instancias (en producción serían Class.forName + newInstance)
        ClassRegistry registry = new ClassRegistry();
        registry.register("WebAutoConfig", new WebAutoConfig());
        registry.register("DataSourceAutoConfig", new DataSourceAutoConfig());
        registry.register("SecurityAutoConfig", new SecurityAutoConfig());
        registry.register("JacksonAutoConfig", new JacksonAutoConfig());

        SpringFactoriesLoader loader = new SpringFactoriesLoader(factories);
        loader.load("EnableAutoConfiguration", registry);
    }
}
