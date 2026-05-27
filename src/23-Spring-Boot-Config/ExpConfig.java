import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// En Spring Boot, @Value y @ConfigurationProperties leen propiedades del entorno.
// Aquí se replica la misma lógica con java.util.Properties y un PropertyResolver propio.
public class ExpConfig {

    // Equivalente a @ConfigurationProperties(prefix = "app")
    // Agrupa propiedades relacionadas bajo un mismo prefijo
    static class AppConfig {
        final String nombre;
        final int timeout;
        final int reintentos;

        AppConfig(String nombre, int timeout, int reintentos) {
            this.nombre     = nombre;
            this.timeout    = timeout;
            this.reintentos = reintentos;
        }
    }

    // Resuelve propiedades con soporte de default — equivalente al mecanismo de @Value
    static class PropertyResolver {
        private final Properties props;

        PropertyResolver(Map<String, String> source) {
            this.props = new Properties();
            this.props.putAll(source);
        }

        // Equivalente a @Value("${key}") — lanza si no existe
        String require(String key) {
            String val = props.getProperty(key);
            if (val == null) throw new IllegalStateException("Propiedad requerida no encontrada: " + key);
            return val;
        }

        // Equivalente a @Value("${key:default}") — usa defaultValue si no existe
        String get(String key, String defaultValue) {
            return props.getProperty(key, defaultValue);
        }

        int getInt(String key, int defaultValue) {
            String val = props.getProperty(key);
            return val != null ? Integer.parseInt(val) : defaultValue;
        }
    }

    // Simula el contenedor / fábrica de beans equivalente a @Configuration + @Bean
    static class AppContext {
        private final PropertyResolver env;
        private final Map<Class<?>, Object> beans = new HashMap<>();

        AppContext(Map<String, String> properties) {
            this.env = new PropertyResolver(properties);
        }

        void refresh() {
            // Equivalente al método @Bean que lee @Value("${...}")
            // @Value("${app.nombre}")        → require("app.nombre")
            // @Value("${app.timeout:30}")    → getInt("app.timeout", 30)
            // @Value("${app.reintentos:3}")  → getInt("app.reintentos", 3)
            AppConfig config = new AppConfig(
                env.require("app.nombre"),
                env.getInt("app.timeout", 30),
                env.getInt("app.reintentos", 3)
            );
            beans.put(AppConfig.class, config);
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type) {
            Object bean = beans.get(type);
            if (bean == null) throw new IllegalStateException("Bean no encontrado: " + type.getSimpleName());
            return (T) bean;
        }

        PropertyResolver getEnvironment() {
            return env;
        }
    }

    public static void main(String[] args) {
        // Simula application.properties cargado en el contexto.
        // En Spring Boot esto lo hace automáticamente al arrancar.
        Map<String, String> applicationProperties = new HashMap<>();
        applicationProperties.put("app.nombre",  "java-prep");
        applicationProperties.put("app.timeout", "45");
        // app.reintentos no definida → usará el default 3

        AppContext ctx = new AppContext(applicationProperties);
        ctx.refresh();

        AppConfig config = ctx.getBean(AppConfig.class);
        System.out.println("Nombre:     " + config.nombre);
        System.out.println("Timeout:    " + config.timeout + "s");
        System.out.println("Reintentos: " + config.reintentos);  // 3 (default)

        // Acceso directo al Environment — útil para leer propiedades de forma programática
        PropertyResolver env = ctx.getEnvironment();
        System.out.println("Via env:    " + env.require("app.nombre"));
        System.out.println("Missing:    " + env.get("app.debug", "false"));
    }
}
