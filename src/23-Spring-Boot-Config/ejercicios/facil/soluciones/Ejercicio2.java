import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

// Ejercicio 2 — @Value simulado
// Annotation personalizada + reflection para inyectar valores de un Map
public class Ejercicio2 {

    // Simula @Value de Spring Boot
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Value {
        String value(); // la clave de la propiedad, ej: "${app.name}"
    }

    // Clase de configuración de la aplicación con 4 campos anotados
    static class AppConfig {
        // @Value("${app.name}")
        @Value("${app.name}")
        private String appName;

        // @Value("${app.version}")
        @Value("${app.version}")
        private String appVersion;

        // @Value("${server.port}")
        @Value("${server.port}")
        private String serverPort;

        // @Value("${db.url}")
        @Value("${db.url}")
        private String dbUrl;

        @Override
        public String toString() {
            return "AppConfig{appName='" + appName + "', appVersion='" + appVersion
                    + "', serverPort='" + serverPort + "', dbUrl='" + dbUrl + "'}";
        }
    }

    static class ValueInjector {
        private final Map<String, String> props;

        public ValueInjector(Map<String, String> props) {
            this.props = props;
        }

        public void inject(Object target) throws IllegalAccessException {
            for (Field field : target.getClass().getDeclaredFields()) {
                Value annotation = field.getAnnotation(Value.class);
                if (annotation == null) continue;

                // Extraer la clave de "${app.name}" → "app.name"
                String expression = annotation.value();
                String key = expression.replaceAll("^\\$\\{(.+)}$", "$1");

                if (!props.containsKey(key)) {
                    System.out.println("  AVISO: propiedad '" + key + "' no encontrada, campo omitido");
                    continue;
                }

                field.setAccessible(true);
                field.set(target, props.get(key));
                System.out.println("  Inyectado: " + field.getName() + " = " + props.get(key));
            }
        }
    }

    public static void main(String[] args) throws IllegalAccessException {
        Map<String, String> properties = new HashMap<>();
        properties.put("app.name", "java-prep-app");
        properties.put("app.version", "1.0.0");
        properties.put("server.port", "9090");
        properties.put("db.url", "jdbc:h2:mem:testdb");

        ValueInjector injector = new ValueInjector(properties);
        AppConfig config = new AppConfig();

        System.out.println("=== @Value simulado con reflection ===");
        System.out.println("Antes de inyección: " + config);
        System.out.println();
        System.out.println("Inyectando campos...");
        injector.inject(config);
        System.out.println();
        System.out.println("Después de inyección: " + config);

        System.out.println();
        System.out.println("=== Campo con clave ausente ===");
        AppConfig config2 = new AppConfig();
        // Usamos un map sin db.url para demostrar el aviso
        Map<String, String> partial = new HashMap<>();
        partial.put("app.name", "otro-app");
        partial.put("app.version", "2.0.0");
        partial.put("server.port", "8080");
        new ValueInjector(partial).inject(config2);
        System.out.println("Resultado: " + config2);
    }
}
