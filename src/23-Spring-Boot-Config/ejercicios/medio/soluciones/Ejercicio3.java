import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// Ejercicio 3 (Medio) — Config validation
// @Min, @Max, @NotNull personalizadas + ConfigValidationException
public class Ejercicio3 {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NotNull {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Min {
        long value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Max {
        long value();
    }

    static class ConfigValidationException extends RuntimeException {
        private final List<String> errors;

        ConfigValidationException(List<String> errors) {
            super("Validación fallida: " + errors);
            this.errors = List.copyOf(errors);
        }

        public List<String> getErrors() { return errors; }
    }

    static class ConfigValidator {

        public static void validate(Object target) {
            List<String> errors = new ArrayList<>();

            for (Field field : target.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value;
                try {
                    value = field.get(target);
                } catch (IllegalAccessException e) {
                    continue;
                }

                String fieldName = field.getName();

                // @NotNull
                if (field.isAnnotationPresent(NotNull.class) && value == null) {
                    errors.add("@NotNull: campo '" + fieldName + "' no puede ser null");
                }

                // @Min / @Max solo aplican a tipos numéricos
                if (value instanceof Number num) {
                    long longVal = num.longValue();

                    if (field.isAnnotationPresent(Min.class)) {
                        long min = field.getAnnotation(Min.class).value();
                        if (longVal < min) {
                            errors.add("@Min(" + min + "): campo '" + fieldName
                                    + "' tiene valor " + longVal + " < " + min);
                        }
                    }
                    if (field.isAnnotationPresent(Max.class)) {
                        long max = field.getAnnotation(Max.class).value();
                        if (longVal > max) {
                            errors.add("@Max(" + max + "): campo '" + fieldName
                                    + "' tiene valor " + longVal + " > " + max);
                        }
                    }
                }
            }

            if (!errors.isEmpty()) {
                throw new ConfigValidationException(errors);
            }
        }
    }

    static class ServerConfig {
        // @NotNull
        @NotNull
        private String host;

        // @Min(1) @Max(65535)
        @Min(1)
        @Max(65535)
        private int port;

        ServerConfig(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString() {
            return "ServerConfig{host='" + host + "', port=" + port + "}";
        }
    }

    static void runValidation(String label, ServerConfig config) {
        System.out.println("--- " + label + " ---");
        System.out.println("Config: " + config);
        try {
            ConfigValidator.validate(config);
            System.out.println("Resultado: VALIDO");
        } catch (ConfigValidationException e) {
            System.out.println("Resultado: INVALIDO");
            e.getErrors().forEach(err -> System.out.println("  ERROR: " + err));
        }
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Config validation con anotaciones custom ===");
        System.out.println();

        runValidation("Config válida", new ServerConfig("localhost", 8080));
        runValidation("Port = 0 (viola @Min(1))", new ServerConfig("localhost", 0));
        runValidation("Port = 70000 (viola @Max(65535))", new ServerConfig("localhost", 70000));
        runValidation("Host null (viola @NotNull)", new ServerConfig(null, 8080));
        runValidation("Múltiples errores: host null + port inválido",
                new ServerConfig(null, -1));
    }
}
