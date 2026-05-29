import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

// Binding de configuracion jerarquica con validacion de constraints

public class Ejercicio6 {

    // ====== Anotaciones de validacion ======

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    @interface NotNull { String message() default "no puede ser nulo"; }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    @interface NotBlank { String message() default "no puede estar vacio"; }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    @interface Min { int value(); String message() default ""; }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    @interface Max { int value(); String message() default ""; }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    @interface Pattern { String regexp(); String message() default "no coincide con patron"; }

    // ====== Config objects (jerarquicos) ======

    static class TimeoutsConfig {
        @Min(value = 100) int connectMs;
        @Min(value = 100) @Max(value = 30000) int readMs;
        @Min(value = 100) int writeMs;

        @Override public String toString() {
            return String.format("Timeouts{connect=%dms, read=%dms, write=%dms}",
                    connectMs, readMs, writeMs);
        }
    }

    static class DatabaseConfig {
        @NotNull @NotBlank String host;
        @Min(value = 1) @Max(value = 65535) int port;
        @NotNull @NotBlank String name;
        @Min(value = 1) @Max(value = 100) int poolSize;
        TimeoutsConfig timeouts;

        @Override public String toString() {
            return String.format("Database{host=%s, port=%d, db=%s, pool=%d, %s}",
                    host, port, name, poolSize, timeouts);
        }
    }

    static class CacheConfig {
        boolean enabled;
        @Min(value = 1) @Max(value = 86400) int ttlSeconds;
        @Min(value = 10) @Max(value = 100000) int maxEntries;
        @NotBlank String evictionPolicy;

        @Override public String toString() {
            return String.format("Cache{enabled=%b, ttl=%ds, maxEntries=%d, eviction=%s}",
                    enabled, ttlSeconds, maxEntries, evictionPolicy);
        }
    }

    static class AppConfig {
        @NotNull @NotBlank String name;
        @Min(value = 1) @Max(value = 65535) int serverPort;
        @Pattern(regexp = "dev|staging|prod") String environment;
        DatabaseConfig database;
        CacheConfig cache;

        @Override public String toString() {
            return String.format("App{name=%s, port=%d, env=%s}%n  %s%n  %s",
                    name, serverPort, environment, database, cache);
        }
    }

    // ====== Validador ======

    static class ConfigValidationException extends RuntimeException {
        private final List<String> errors;
        ConfigValidationException(List<String> errors) {
            super("Errores de configuracion: " + errors);
            this.errors = errors;
        }
        List<String> getErrors() { return errors; }
    }

    static class HierarchicalValidator {
        List<String> validate(Object config) throws IllegalAccessException {
            List<String> errors = new ArrayList<>();
            validate(config, "", errors);
            return errors;
        }

        private void validate(Object obj, String prefix, List<String> errors)
                throws IllegalAccessException {
            if (obj == null) return;
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(obj);
                String path = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();

                // Recursion en objetos anidados (no primitivos ni String)
                if (value != null && !field.getType().isPrimitive()
                        && !field.getType().equals(String.class)
                        && !field.getType().equals(Boolean.class)) {
                    validate(value, path, errors);
                }

                // @NotNull
                if (field.isAnnotationPresent(NotNull.class) && value == null) {
                    errors.add(path + ": " + field.getAnnotation(NotNull.class).message());
                }

                // @NotBlank
                if (field.isAnnotationPresent(NotBlank.class)
                        && (value == null || value.toString().isBlank())) {
                    errors.add(path + ": " + field.getAnnotation(NotBlank.class).message());
                }

                // @Min
                if (field.isAnnotationPresent(Min.class) && value instanceof Number n) {
                    int min = field.getAnnotation(Min.class).value();
                    if (n.intValue() < min)
                        errors.add(path + ": debe ser >= " + min + " (valor: " + n.intValue() + ")");
                }

                // @Max
                if (field.isAnnotationPresent(Max.class) && value instanceof Number n) {
                    int max = field.getAnnotation(Max.class).value();
                    if (n.intValue() > max)
                        errors.add(path + ": debe ser <= " + max + " (valor: " + n.intValue() + ")");
                }

                // @Pattern
                if (field.isAnnotationPresent(Pattern.class) && value instanceof String s) {
                    String regexp = field.getAnnotation(Pattern.class).regexp();
                    if (!s.matches(regexp))
                        errors.add(path + ": " + field.getAnnotation(Pattern.class).message()
                                + " (valor: " + s + ", patron: " + regexp + ")");
                }
            }
        }
    }

    // ====== Binder jerarquico ======

    static class HierarchicalBinder {
        private final Map<String, String> props;

        HierarchicalBinder(Map<String, String> props) {
            this.props = props;
        }

        AppConfig bindAppConfig() throws Exception {
            AppConfig app = new AppConfig();
            app.name        = get("app.name");
            app.serverPort  = getInt("app.serverPort", 8080);
            app.environment = get("app.environment");

            app.database = new DatabaseConfig();
            app.database.host     = get("app.database.host");
            app.database.port     = getInt("app.database.port", 5432);
            app.database.name     = get("app.database.name");
            app.database.poolSize = getInt("app.database.poolSize", 10);

            app.database.timeouts = new TimeoutsConfig();
            app.database.timeouts.connectMs = getInt("app.database.timeouts.connectMs", 500);
            app.database.timeouts.readMs    = getInt("app.database.timeouts.readMs", 5000);
            app.database.timeouts.writeMs   = getInt("app.database.timeouts.writeMs", 3000);

            app.cache = new CacheConfig();
            app.cache.enabled        = getBoolean("app.cache.enabled", true);
            app.cache.ttlSeconds     = getInt("app.cache.ttlSeconds", 300);
            app.cache.maxEntries     = getInt("app.cache.maxEntries", 1000);
            app.cache.evictionPolicy = get("app.cache.evictionPolicy");

            return app;
        }

        private String get(String key) {
            return props.get(key); // puede ser null
        }

        private int getInt(String key, int def) {
            String v = props.get(key);
            return v == null ? def : Integer.parseInt(v);
        }

        private boolean getBoolean(String key, boolean def) {
            String v = props.get(key);
            return v == null ? def : Boolean.parseBoolean(v);
        }
    }

    // ====== DEMO ======

    public static void main(String[] args) throws Exception {
        System.out.println("=== Binding jerarquico con validacion de constraints ===");
        System.out.println();

        HierarchicalValidator validator = new HierarchicalValidator();

        // --- Config valida ---
        System.out.println("[ Config VALIDA ]");
        Map<String, String> validProps = new LinkedHashMap<>();
        validProps.put("app.name", "mi-servicio");
        validProps.put("app.serverPort", "8080");
        validProps.put("app.environment", "prod");
        validProps.put("app.database.host", "db.prod.internal");
        validProps.put("app.database.port", "5432");
        validProps.put("app.database.name", "ordenes");
        validProps.put("app.database.poolSize", "20");
        validProps.put("app.database.timeouts.connectMs", "300");
        validProps.put("app.database.timeouts.readMs", "10000");
        validProps.put("app.database.timeouts.writeMs", "5000");
        validProps.put("app.cache.enabled", "true");
        validProps.put("app.cache.ttlSeconds", "600");
        validProps.put("app.cache.maxEntries", "5000");
        validProps.put("app.cache.evictionPolicy", "LRU");

        AppConfig validConfig = new HierarchicalBinder(validProps).bindAppConfig();
        List<String> errors = validator.validate(validConfig);
        if (errors.isEmpty()) {
            System.out.println("  Validacion: OK");
            System.out.println("  Config: " + validConfig);
        }
        System.out.println();

        // --- Config invalida (multiple errores) ---
        System.out.println("[ Config INVALIDA (multiples errores) ]");
        Map<String, String> invalidProps = new LinkedHashMap<>(validProps);
        invalidProps.put("app.name", "");              // @NotBlank
        invalidProps.put("app.serverPort", "99999");   // @Max(65535)
        invalidProps.put("app.environment", "local");  // @Pattern(dev|staging|prod)
        invalidProps.put("app.database.host", null);   // null -> @NotNull
        invalidProps.put("app.database.poolSize", "0");// @Min(1)
        invalidProps.put("app.database.timeouts.readMs", "50000"); // @Max(30000)
        invalidProps.put("app.cache.ttlSeconds", "0"); // @Min(1)
        invalidProps.remove("app.database.host");       // eliminar -> null

        AppConfig invalidConfig = new HierarchicalBinder(invalidProps).bindAppConfig();
        List<String> invalids = validator.validate(invalidConfig);
        System.out.printf("  Errores encontrados: %d%n", invalids.size());
        invalids.forEach(e -> System.out.println("    [ERROR] " + e));
        System.out.println();

        // --- Uso con excepcion ---
        System.out.println("[ Lanzando ConfigValidationException ]");
        if (!invalids.isEmpty()) {
            try {
                throw new ConfigValidationException(invalids);
            } catch (ConfigValidationException e) {
                System.out.println("  Capturada: " + e.getClass().getSimpleName());
                System.out.println("  Total errores: " + e.getErrors().size());
            }
        }

        System.out.println();
        System.out.println("=== Conclusion ===");
        System.out.println("El validador recorre recursivamente el arbol de config.");
        System.out.println("Cada constraint se verifica via reflection sobre cada campo.");
        System.out.println("En Spring Boot: @ConfigurationProperties + @Validated + @Min/@Max/@NotNull.");
    }
}
