import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

// Ejercicio 6 — Prefix binding
// PrefixBinder.bind(prefix, target, props) inyecta por reflection
// los campos de target buscando claves "prefix.fieldName" en el mapa
public class Ejercicio6 {

    // Clase de configuración de servidor
    static class ServerConfig {
        private String host;
        private int port;
        private int timeout;

        @Override
        public String toString() {
            return "ServerConfig{host='" + host + "', port=" + port + ", timeout=" + timeout + "}";
        }
    }

    static class PrefixBinder {

        /**
         * Busca en props las claves con formato "prefix.fieldName" e inyecta
         * los valores en los campos correspondientes de target usando reflection.
         * Soporta conversión de String a int automáticamente.
         */
        public static void bind(String prefix, Object target, Map<String, String> props)
                throws IllegalAccessException {
            for (Field field : target.getClass().getDeclaredFields()) {
                String key = prefix + "." + field.getName();
                String rawValue = props.get(key);

                if (rawValue == null) {
                    System.out.println("  AVISO: no hay valor para '" + key + "', campo omitido");
                    continue;
                }

                field.setAccessible(true);
                Class<?> type = field.getType();

                if (type == int.class || type == Integer.class) {
                    field.set(target, Integer.parseInt(rawValue.trim()));
                } else if (type == long.class || type == Long.class) {
                    field.set(target, Long.parseLong(rawValue.trim()));
                } else if (type == boolean.class || type == Boolean.class) {
                    field.set(target, Boolean.parseBoolean(rawValue.trim()));
                } else {
                    field.set(target, rawValue.trim());
                }

                System.out.println("  Bindeado: " + field.getName() + " (" + type.getSimpleName()
                        + ") = " + field.get(target));
            }
        }
    }

    public static void main(String[] args) throws IllegalAccessException {
        System.out.println("=== Prefix binding ===");
        System.out.println();

        Map<String, String> props = new HashMap<>();
        props.put("server.host", "192.168.1.10");
        props.put("server.port", "8443");
        props.put("server.timeout", "30");
        // propiedad extra que NO aplica a este prefijo
        props.put("db.host", "db-server");
        props.put("db.port", "5432");

        ServerConfig config = new ServerConfig();
        System.out.println("Antes del binding: " + config);
        System.out.println();

        System.out.println("Ejecutando PrefixBinder.bind(\"server\", config, props):");
        PrefixBinder.bind("server", config, props);
        System.out.println();

        System.out.println("Después del binding: " + config);

        System.out.println();
        System.out.println("=== Binding con campo faltante ===");
        Map<String, String> partial = new HashMap<>();
        partial.put("server.host", "localhost");
        // server.port y server.timeout ausentes → aviso
        ServerConfig config2 = new ServerConfig();
        PrefixBinder.bind("server", config2, partial);
        System.out.println("Resultado: " + config2);
    }
}
