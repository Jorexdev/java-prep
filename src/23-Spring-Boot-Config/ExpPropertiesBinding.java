import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

// Simula @ConfigurationProperties: binding de un Map<String,String> a un POJO.
// Spring Boot hace esto automáticamente con relaxed binding al arrancar.
public class ExpPropertiesBinding {

    // ── POJO de configuración ─────────────────────────────────────────────────
    // @ConfigurationProperties(prefix = "db")
    static class DatabaseProperties {
        String url;
        String username;
        int maxPoolSize = 10;   // defaults
        long timeout    = 30_000;

        @Override
        public String toString() {
            return "DatabaseProperties{url='" + url + "', username='" + username
                + "', maxPoolSize=" + maxPoolSize + ", timeout=" + timeout + "ms}";
        }
    }

    // ── Binder con relaxed binding ────────────────────────────────────────────

    // Spring Boot convierte kebab-case, snake_case y SCREAMING_SNAKE a camelCase
    static class PropertiesBinder {

        // Normaliza la clave a camelCase para comparar con el nombre del campo
        static String normalize(String key) {
            // Elimina prefijo si viene con punto (ej. "db.max-pool-size" → "max-pool-size")
            if (key.contains(".")) key = key.substring(key.lastIndexOf('.') + 1);
            StringBuilder sb = new StringBuilder();
            boolean nextUpper = false;
            for (char c : key.toCharArray()) {
                if (c == '-' || c == '_') {
                    nextUpper = true;
                } else {
                    sb.append(nextUpper ? Character.toUpperCase(c) : c);
                    nextUpper = false;
                }
            }
            return sb.toString();
        }

        static <T> T bind(Class<T> type, String prefix, Map<String, String> source) throws Exception {
            T instance = type.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, String> entry : source.entrySet()) {
                String rawKey = entry.getKey();
                // Solo procesa claves con el prefijo correcto
                if (!rawKey.startsWith(prefix + ".")) continue;

                String camelKey = normalize(rawKey);
                String value    = entry.getValue();

                try {
                    Field field = type.getDeclaredField(camelKey);
                    field.setAccessible(true);
                    // Conversión de tipo básica (Spring usa ConversionService internamente)
                    if (field.getType() == int.class)  field.set(instance, Integer.parseInt(value));
                    else if (field.getType() == long.class) field.set(instance, Long.parseLong(value));
                    else field.set(instance, value);
                } catch (NoSuchFieldException ignored) {
                    // Propiedad sin campo correspondiente → Spring la ignora por defecto
                }
            }
            return instance;
        }
    }

    public static void main(String[] args) throws Exception {
        // Simula application.properties cargado por Spring Boot al arrancar
        Map<String, String> props = new HashMap<>();
        props.put("db.url",            "jdbc:postgresql://localhost:5432/mydb");
        props.put("db.username",       "admin");
        props.put("db.max-pool-size",  "25");          // kebab-case → maxPoolSize
        props.put("db.timeout",        "60000");
        props.put("other.setting",     "ignorado");    // prefijo diferente → ignorado

        System.out.println("=== Relaxed binding: kebab-case → camelCase ===");
        System.out.println("  'db.max-pool-size' → campo 'maxPoolSize'");
        System.out.println();

        DatabaseProperties db = PropertiesBinder.bind(DatabaseProperties.class, "db", props);
        System.out.println("Resultado: " + db);

        System.out.println("\n=== Defaults cuando la propiedad no está en el fichero ===");
        Map<String, String> minimal = new HashMap<>();
        minimal.put("db.url",      "jdbc:h2:mem:test");
        minimal.put("db.username", "sa");
        // maxPoolSize y timeout no definidas → conservan los valores del campo

        DatabaseProperties dbMinimal = PropertiesBinder.bind(DatabaseProperties.class, "db", minimal);
        System.out.println("maxPoolSize (default): " + dbMinimal.maxPoolSize);
        System.out.println("timeout     (default): " + dbMinimal.timeout + "ms");

        System.out.println("\n=== En Spring Boot ===");
        System.out.println("@ConfigurationProperties(prefix=\"db\") + @EnableConfigurationProperties");
        System.out.println("hace todo esto automáticamente con validación de tipos y JSR-380.");
    }
}
