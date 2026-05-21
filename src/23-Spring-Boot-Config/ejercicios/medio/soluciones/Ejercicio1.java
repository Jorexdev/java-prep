import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

// Ejercicio 1 (Medio) — @ConfigurationProperties nested
// Binding anidado desde Map<String, String> con prefijos usando reflection
public class Ejercicio1 {

    // Simula @ConfigurationProperties(prefix="app.db")
    static class DatabaseConfig {
        private String url;
        private int poolSize;

        @Override
        public String toString() {
            return "DatabaseConfig{url='" + url + "', poolSize=" + poolSize + "}";
        }
    }

    // Simula @ConfigurationProperties(prefix="app.cache")
    static class CacheConfig {
        private boolean enabled;
        private long ttlSeconds;

        @Override
        public String toString() {
            return "CacheConfig{enabled=" + enabled + ", ttlSeconds=" + ttlSeconds + "}";
        }
    }

    // Simula @ConfigurationProperties(prefix="app")
    static class AppConfig {
        private DatabaseConfig db;
        private CacheConfig cache;

        public AppConfig() {
            this.db = new DatabaseConfig();
            this.cache = new CacheConfig();
        }

        @Override
        public String toString() {
            return "AppConfig{\n  db=" + db + ",\n  cache=" + cache + "\n}";
        }
    }

    /**
     * Binder recursivo que soporta objetos anidados.
     * Para cada campo del target intenta encontrar sub-objetos si no es un tipo primitivo/String.
     */
    static class NestedBinder {

        public static void bind(String prefix, Object target, Map<String, String> props)
                throws Exception {
            for (Field field : target.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String key = prefix + "." + field.getName();
                Class<?> type = field.getType();

                if (isSimpleType(type)) {
                    String rawValue = props.get(key);
                    if (rawValue != null) {
                        setSimpleField(field, target, rawValue.trim());
                        System.out.println("  " + key + " = " + rawValue.trim());
                    }
                } else {
                    // Tipo complejo → binding recursivo sobre la instancia existente
                    Object nested = field.get(target);
                    if (nested == null) {
                        nested = type.getDeclaredConstructor().newInstance();
                        field.set(target, nested);
                    }
                    bind(key, nested, props);
                }
            }
        }

        private static boolean isSimpleType(Class<?> t) {
            return t.isPrimitive() || t == String.class || t == Integer.class
                    || t == Long.class || t == Boolean.class || t == Double.class;
        }

        private static void setSimpleField(Field field, Object target, String value)
                throws IllegalAccessException {
            Class<?> t = field.getType();
            if (t == int.class || t == Integer.class) field.set(target, Integer.parseInt(value));
            else if (t == long.class || t == Long.class) field.set(target, Long.parseLong(value));
            else if (t == boolean.class || t == Boolean.class) field.set(target, Boolean.parseBoolean(value));
            else if (t == double.class || t == Double.class) field.set(target, Double.parseDouble(value));
            else field.set(target, value);
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> props = new HashMap<>();
        props.put("app.db.url", "jdbc:postgresql://localhost:5432/mydb");
        props.put("app.db.poolSize", "20");
        props.put("app.cache.enabled", "true");
        props.put("app.cache.ttlSeconds", "3600");

        System.out.println("=== @ConfigurationProperties nested ===");
        System.out.println("Propiedades de entrada:");
        props.forEach((k, v) -> System.out.println("  " + k + " = " + v));
        System.out.println();

        AppConfig config = new AppConfig();
        System.out.println("Binding recursivo:");
        NestedBinder.bind("app", config, props);

        System.out.println();
        System.out.println("Resultado: " + config);

        System.out.println();
        System.out.println("=== Acceso individual ===");
        System.out.println("config.db.url        = " + config.db.url);
        System.out.println("config.db.poolSize   = " + config.db.poolSize);
        System.out.println("config.cache.enabled = " + config.cache.enabled);
        System.out.println("config.cache.ttlSecs = " + config.cache.ttlSeconds);
    }
}
