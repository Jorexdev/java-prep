import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @ConfigurationProperties binding internamente usa BeanWrapper + ConversionService.
// Este ejemplo simula el pipeline completo:
//   1. Relaxed binding  — db-url, dbUrl y DB_URL resuelven al mismo campo
//   2. Type conversion  — String → int, long, boolean vía ConversionService
//   3. Validación JSR-380 — @NotNull y @Min aplicados sobre el POJO ya populado
public class ExpConfigBinding {

    // ── Anotaciones de validación (simulan Jakarta Validation / JSR-380) ──────

    // @NotNull — el campo no puede ser null después del binding
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NotNull {
        String message() default "no puede ser null";
    }

    // @Min — el campo numérico debe ser >= value
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Min {
        int value();
        String message() default "debe ser >= {value}";
    }

    // ── POJO de configuración ─────────────────────────────────────────────────
    // En Spring: @ConfigurationProperties(prefix = "db") @Validated
    static class DbProperties {
        @NotNull
        String url;          // db.url / db-url / DB_URL

        @NotNull
        String username;     // db.username / DB_USERNAME

        @Min(1)
        int maxPoolSize = 10;  // db.max-pool-size / db.maxPoolSize / DB_MAX_POOL_SIZE

        boolean ssl = false;  // db.ssl / DB_SSL

        @Override
        public String toString() {
            return "DbProperties{url='" + url + "', username='" + username
                    + "', maxPoolSize=" + maxPoolSize + ", ssl=" + ssl + "}";
        }
    }

    // ── Relaxed binding: normaliza cualquier formato a camelCase ──────────────

    // Spring Boot acepta estas formas para el mismo campo:
    //   kebab-case     db.max-pool-size  → maxPoolSize
    //   camelCase      db.maxPoolSize    → maxPoolSize  (sin cambio)
    //   SCREAMING_SNAKE  DB_MAX_POOL_SIZE  → maxPoolSize
    //
    // Aquí replicamos esa lógica sin Spring.
    static class RelaxedBinder {

        // Normaliza una clave a camelCase eliminando prefijo, guiones y underscores
        static String normalize(String rawKey, String prefix) {
            // Eliminar prefijo (con separador "." o "_")
            String stripped = rawKey;
            String dotPrefix = prefix + ".";
            String underPrefix = prefix.toUpperCase().replace(".", "_") + "_";

            if (stripped.toLowerCase().startsWith(dotPrefix.toLowerCase())) {
                stripped = stripped.substring(dotPrefix.length());
            } else if (stripped.toUpperCase().startsWith(underPrefix)) {
                stripped = stripped.substring(underPrefix.length());
            }

            // Convertir a minúsculas y luego a camelCase
            stripped = stripped.toLowerCase();
            StringBuilder sb = new StringBuilder();
            boolean nextUpper = false;
            for (char c : stripped.toCharArray()) {
                if (c == '-' || c == '_' || c == '.') {
                    nextUpper = true;
                } else {
                    sb.append(nextUpper ? Character.toUpperCase(c) : c);
                    nextUpper = false;
                }
            }
            return sb.toString();
        }

        // Convierte String al tipo del campo — equivale a ConversionService de Spring
        static Object convert(String value, Class<?> targetType) {
            if (targetType == int.class || targetType == Integer.class)
                return Integer.parseInt(value);
            if (targetType == long.class || targetType == Long.class)
                return Long.parseLong(value);
            if (targetType == boolean.class || targetType == Boolean.class)
                return Boolean.parseBoolean(value);
            return value;  // String → String (sin conversión)
        }

        // Realiza el binding de source al POJO usando relaxed binding + type conversion
        static <T> T bind(Class<T> type, String prefix, Map<String, String> source)
                throws Exception {
            T instance = type.getDeclaredConstructor().newInstance();

            for (Map.Entry<String, String> entry : source.entrySet()) {
                String camelKey = normalize(entry.getKey(), prefix);
                if (camelKey.isEmpty()) continue;  // clave sin prefijo reconocido

                try {
                    Field field = type.getDeclaredField(camelKey);
                    field.setAccessible(true);
                    Object converted = convert(entry.getValue(), field.getType());
                    field.set(instance, converted);
                    System.out.printf("  binding: %-30s → %-15s = %s%n",
                            entry.getKey(), camelKey, entry.getValue());
                } catch (NoSuchFieldException ignored) {
                    // Propiedad sin campo correspondiente → Spring la ignora
                }
            }
            return instance;
        }
    }

    // ── Validador post-binding ────────────────────────────────────────────────

    static class BindingValidator {

        static List<String> validate(Object config) throws IllegalAccessException {
            List<String> violations = new ArrayList<>();
            for (Field field : config.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(config);
                String name  = field.getName();

                if (field.isAnnotationPresent(NotNull.class) && value == null) {
                    violations.add(name + ": " + field.getAnnotation(NotNull.class).message());
                }
                if (field.isAnnotationPresent(Min.class)) {
                    int minVal = field.getAnnotation(Min.class).value();
                    int actual = (value instanceof Number n) ? n.intValue() : 0;
                    if (actual < minVal) {
                        violations.add(name + ": debe ser >= " + minVal + " (actual: " + actual + ")");
                    }
                }
            }
            return violations;
        }
    }

    // ── Secciones demostrativas ───────────────────────────────────────────────

    // ── 1. RELAXED BINDING: kebab-case ────────────────────────────────────────
    static void demoKebabCase() throws Exception {
        System.out.println("\n=== 1. Relaxed binding: kebab-case (application.properties) ===");
        Map<String, String> props = new HashMap<>();
        props.put("db.url",           "jdbc:postgresql://localhost/app");
        props.put("db.username",      "admin");
        props.put("db.max-pool-size", "25");   // kebab → maxPoolSize
        props.put("db.ssl",           "true");

        DbProperties db = RelaxedBinder.bind(DbProperties.class, "db", props);
        System.out.println("  Resultado: " + db);
    }

    // ── 2. RELAXED BINDING: camelCase + SCREAMING_SNAKE ───────────────────────
    static void demoOtrosFormatos() throws Exception {
        System.out.println("\n=== 2. Relaxed binding: camelCase y SCREAMING_SNAKE ===");
        // camelCase en application.yml
        Map<String, String> yamlStyle = new HashMap<>();
        yamlStyle.put("db.url",         "jdbc:h2:mem:test");
        yamlStyle.put("db.username",    "sa");
        yamlStyle.put("db.maxPoolSize", "5");   // camelCase directo → maxPoolSize
        System.out.println("  --- camelCase ---");
        DbProperties dbYaml = RelaxedBinder.bind(DbProperties.class, "db", yamlStyle);
        System.out.println("  Resultado: " + dbYaml);

        // SCREAMING_SNAKE en variables de entorno
        Map<String, String> envStyle = new HashMap<>();
        envStyle.put("DB_URL",           "jdbc:postgresql://prod/app");
        envStyle.put("DB_USERNAME",      "prod_user");
        envStyle.put("DB_MAX_POOL_SIZE", "50");   // SCREAMING_SNAKE → maxPoolSize
        envStyle.put("DB_SSL",           "true");
        System.out.println("  --- SCREAMING_SNAKE (env vars) ---");
        DbProperties dbEnv = RelaxedBinder.bind(DbProperties.class, "db", envStyle);
        System.out.println("  Resultado: " + dbEnv);
    }

    // ── 3. TYPE CONVERSION ────────────────────────────────────────────────────
    static void demoTypeConversion() {
        System.out.println("\n=== 3. Type conversion: String → int / boolean ===");
        System.out.println("  \"25\"   → int    : " + RelaxedBinder.convert("25",   int.class));
        System.out.println("  \"true\" → boolean: " + RelaxedBinder.convert("true", boolean.class));
        System.out.println("  \"5000\" → long   : " + RelaxedBinder.convert("5000", long.class));
        System.out.println("  Spring usa ConversionService con converters registrados");
        System.out.println("  (StringToNumberConverterFactory, etc.)");
    }

    // ── 4. VALIDACIÓN POST-BINDING ────────────────────────────────────────────
    static void demoValidacion() throws Exception {
        System.out.println("\n=== 4. Validación @NotNull y @Min post-binding ===");

        // Config válida
        Map<String, String> valid = new HashMap<>();
        valid.put("db.url",           "jdbc:postgresql://ok/app");
        valid.put("db.username",      "user");
        valid.put("db.max-pool-size", "5");
        DbProperties dbOk = RelaxedBinder.bind(DbProperties.class, "db", valid);
        List<String> v1 = BindingValidator.validate(dbOk);
        System.out.println("  Violaciones en config válida: " + v1.size());

        // Config inválida: url=null, maxPoolSize=0 viola @Min(1)
        System.out.println("  --- config inválida (url falta, maxPoolSize=0) ---");
        Map<String, String> invalid = new HashMap<>();
        invalid.put("db.username",      "user");
        invalid.put("db.max-pool-size", "0");   // viola @Min(1)
        // db.url no se pone → url queda null → viola @NotNull
        DbProperties dbBad = RelaxedBinder.bind(DbProperties.class, "db", invalid);
        List<String> v2 = BindingValidator.validate(dbBad);
        System.out.println("  Violaciones encontradas:");
        v2.forEach(msg -> System.out.println("    - " + msg));
        System.out.println("  En Spring Boot estas violaciones lanzan BindValidationException");
        System.out.println("  y detienen el arranque antes de que la app reciba tráfico.");
    }

    public static void main(String[] args) throws Exception {
        demoKebabCase();
        demoOtrosFormatos();
        demoTypeConversion();
        demoValidacion();

        System.out.println("\n=== Resumen ===");
        System.out.println("  @ConfigurationProperties + @EnableConfigurationProperties registra el POJO.");
        System.out.println("  Spring Boot acepta kebab-case, camelCase y SCREAMING_SNAKE para el mismo campo.");
        System.out.println("  La conversión de tipos la gestiona ConversionService, no el código del usuario.");
        System.out.println("  @Validated activa JSR-380 y falla el arranque si hay violaciones.");
    }
}
