import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// Simula @Validated en @ConfigurationProperties.
// Spring Boot usa Hibernate Validator (JSR-380) para validar el POJO al arrancar.
// Si hay violaciones, lanza BindValidationException antes de que la app inicie.
public class ExpValidation {

    // ── Anotaciones de validación JSR-380 (simuladas) ─────────────────────────

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NotBlank {
        String message() default "no debe estar vacío";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Min {
        int value();
        String message() default "debe ser >= {value}";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Max {
        int value();
        String message() default "debe ser <= {value}";
    }

    // ── Excepción de binding ──────────────────────────────────────────────────

    static class BindException extends RuntimeException {
        private final List<String> violations;

        BindException(List<String> violations) {
            super("Configuración inválida: " + violations.size() + " violación(es)");
            this.violations = violations;
        }

        List<String> getViolations() { return violations; }
    }

    // ── POJO de configuración ─────────────────────────────────────────────────
    // @ConfigurationProperties(prefix = "server")
    // @Validated
    static class ServerProperties {
        @NotBlank
        String host;

        @Min(1) @Max(65535)
        int port;

        @NotBlank
        String contextPath;

        @Min(1) @Max(200)
        int maxConnections;

        ServerProperties(String host, int port, String contextPath, int maxConnections) {
            this.host           = host;
            this.port           = port;
            this.contextPath    = contextPath;
            this.maxConnections = maxConnections;
        }
    }

    // ── Validador ─────────────────────────────────────────────────────────────

    static class ConfigValidator {

        static List<String> validate(Object config) throws IllegalAccessException {
            List<String> violations = new ArrayList<>();
            for (Field field : config.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(config);
                String fieldName = field.getName();

                if (field.isAnnotationPresent(NotBlank.class)) {
                    if (value == null || value.toString().isBlank()) {
                        violations.add(fieldName + ": " + field.getAnnotation(NotBlank.class).message());
                    }
                }
                if (field.isAnnotationPresent(Min.class)) {
                    int min = field.getAnnotation(Min.class).value();
                    int actual = value instanceof Number ? ((Number) value).intValue() : 0;
                    if (actual < min) {
                        violations.add(fieldName + ": debe ser >= " + min + " (actual: " + actual + ")");
                    }
                }
                if (field.isAnnotationPresent(Max.class)) {
                    int max = field.getAnnotation(Max.class).value();
                    int actual = value instanceof Number ? ((Number) value).intValue() : 0;
                    if (actual > max) {
                        violations.add(fieldName + ": debe ser <= " + max + " (actual: " + actual + ")");
                    }
                }
            }
            return violations;
        }

        // Lanza BindException si hay violaciones — Spring lo hace al arrancar
        static void validateAndThrow(Object config) throws IllegalAccessException {
            List<String> violations = validate(config);
            if (!violations.isEmpty()) throw new BindException(violations);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Configuración válida ===");
        ServerProperties valid = new ServerProperties("localhost", 8080, "/api", 50);
        ConfigValidator.validateAndThrow(valid);
        System.out.println("  Sin violaciones — bean registrado correctamente");

        System.out.println("\n=== Configuración con múltiples violaciones ===");
        // port=0 viola @Min(1), contextPath="" viola @NotBlank, maxConnections=999 viola @Max(200)
        ServerProperties invalid = new ServerProperties("", 0, "", 999);
        try {
            ConfigValidator.validateAndThrow(invalid);
        } catch (BindException e) {
            System.out.println("  " + e.getMessage());
            e.getViolations().forEach(v -> System.out.println("    - " + v));
        }

        System.out.println("\n=== En Spring Boot ===");
        System.out.println("  @Validated + @ConfigurationProperties → todas las violaciones");
        System.out.println("  se reportan juntas antes del inicio, no una a una.");
        System.out.println("  Añadir spring-boot-starter-validation al pom.xml activa JSR-380.");
    }
}
