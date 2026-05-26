import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

// Simula cómo un starter compone auto-configuraciones.
// spring-boot-starter-web, por ejemplo, encadena Tomcat + Jackson + DispatcherServlet.
// Cada auto-config puede depender de que otra ya haya corrido (@AutoConfigureAfter).
public class ExpStarterComposition {

    // ── Entorno de contexto ───────────────────────────────────────────────────

    static class AppContext {
        private final Map<String, Object> beans = new HashMap<>();
        private final Map<String, String> props = new HashMap<>();

        void setProperty(String key, String value) { props.put(key, value); }
        String getProperty(String key) { return props.getOrDefault(key, ""); }

        void registerBean(String name, Object bean) {
            beans.put(name, bean);
            System.out.println("    bean registrado: " + name);
        }

        boolean hasBean(String name) { return beans.containsKey(name); }
    }

    // ── Interfaz de auto-configuración ────────────────────────────────────────

    // Equivale a una clase anotada con @AutoConfiguration en Spring Boot
    interface AutoConfiguration {
        String name();
        // Condición de activación — @ConditionalOnProperty, @ConditionalOnClass, etc.
        boolean shouldApply(AppContext ctx);
        void apply(AppContext ctx);
    }

    // ── Auto-configuraciones del starter web ─────────────────────────────────

    // @AutoConfiguration — registra el servidor Tomcat embebido
    static class TomcatAutoConfig implements AutoConfiguration {
        @Override public String name() { return "TomcatAutoConfig"; }
        @Override public boolean shouldApply(AppContext ctx) { return true; }
        @Override public void apply(AppContext ctx) {
            ctx.registerBean("embeddedTomcat", "Tomcat:8080");
        }
    }

    // @AutoConfiguration @AutoConfigureAfter(TomcatAutoConfig)
    // @ConditionalOnBean("embeddedTomcat")
    static class DispatcherServletAutoConfig implements AutoConfiguration {
        @Override public String name() { return "DispatcherServletAutoConfig"; }
        @Override public boolean shouldApply(AppContext ctx) {
            // Solo se aplica si Tomcat ya fue registrado
            return ctx.hasBean("embeddedTomcat");
        }
        @Override public void apply(AppContext ctx) {
            ctx.registerBean("dispatcherServlet", "DispatcherServlet → /");
        }
    }

    // @AutoConfiguration @ConditionalOnClass("com.fasterxml.jackson.databind.ObjectMapper")
    static class JacksonAutoConfig implements AutoConfiguration {
        @Override public String name() { return "JacksonAutoConfig"; }
        @Override public boolean shouldApply(AppContext ctx) {
            // Simula @ConditionalOnClass — en prod verificaría si Jackson está en el classpath
            return "true".equals(ctx.getProperty("jackson.enabled"));
        }
        @Override public void apply(AppContext ctx) {
            if (!ctx.hasBean("objectMapper")) {
                // @ConditionalOnMissingBean — no sobreescribe el del usuario
                ctx.registerBean("objectMapper", "JacksonObjectMapper (default)");
            }
        }
    }

    // ── StarterRegistry — simula spring.factories / AutoConfiguration.imports ─

    static class StarterRegistry {
        // Mapa: nombre del starter → lista de auto-configs que compone
        private final Map<String, List<AutoConfiguration>> starters = new LinkedHashMap<>();

        void registerStarter(String name, List<AutoConfiguration> configs) {
            starters.put(name, configs);
        }

        void run(String starterName, AppContext ctx) {
            List<AutoConfiguration> configs = starters.getOrDefault(starterName, List.of());
            System.out.println("  Cargando " + configs.size() + " auto-config(s) de " + starterName + ":");
            for (AutoConfiguration config : configs) {
                if (config.shouldApply(ctx)) {
                    System.out.println("  [APPLY] " + config.name());
                    config.apply(ctx);
                } else {
                    System.out.println("  [SKIP]  " + config.name() + " (condición no cumplida)");
                }
            }
        }
    }

    public static void main(String[] args) {
        StarterRegistry registry = new StarterRegistry();

        // spring-boot-starter-web compone estas auto-configs en orden
        registry.registerStarter("spring-boot-starter-web", List.of(
            new TomcatAutoConfig(),
            new JacksonAutoConfig(),       // Jackson puede correr antes o después — no depende de Tomcat
            new DispatcherServletAutoConfig()
        ));

        System.out.println("=== Sin Jackson en classpath ===");
        AppContext ctx1 = new AppContext();
        // jackson.enabled no está → JacksonAutoConfig omitido
        registry.run("spring-boot-starter-web", ctx1);

        System.out.println("\n=== Con Jackson disponible ===");
        AppContext ctx2 = new AppContext();
        ctx2.setProperty("jackson.enabled", "true");
        registry.run("spring-boot-starter-web", ctx2);

        System.out.println("\n=== Con bean objectMapper del usuario (no se sobreescribe) ===");
        AppContext ctx3 = new AppContext();
        ctx3.setProperty("jackson.enabled", "true");
        ctx3.registerBean("objectMapper", "MyCustomObjectMapper");  // registrado antes del starter
        registry.run("spring-boot-starter-web", ctx3);
    }
}
