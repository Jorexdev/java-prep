import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Ejercicio 4 — Property placeholder
// resolvePlaceholders sustituye ${key} y soporta referencias anidadas
public class Ejercicio4 {

    static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * Resuelve todos los ${key} en el template usando el mapa de propiedades.
     * Si el valor de una propiedad también contiene ${...}, se resuelve recursivamente.
     */
    static String resolvePlaceholders(String template, Map<String, String> props) {
        return resolvePlaceholders(template, props, 0);
    }

    private static String resolvePlaceholders(String template, Map<String, String> props, int depth) {
        if (depth > 10) throw new IllegalStateException("Ciclo detectado en placeholders");

        String result = template;
        Matcher matcher = PLACEHOLDER.matcher(result);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = props.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Placeholder '${" + key + "}' no resuelto");
            }
            // El valor puede contener más placeholders → resolver recursivamente
            String resolved = resolvePlaceholders(value, props, depth + 1);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("=== Property placeholder ===");
        System.out.println();

        // Demo básico
        Map<String, String> props1 = new HashMap<>();
        props1.put("app.name", "mi-app");
        props1.put("app.version", "1.0");

        String template1 = "${app.name}-${app.version}";
        System.out.println("Template : " + template1);
        System.out.println("Resultado: " + resolvePlaceholders(template1, props1));

        System.out.println();

        // Demo con referencia encadenada: url referencia a host y port
        Map<String, String> props2 = new HashMap<>();
        props2.put("server.host", "localhost");
        props2.put("server.port", "8080");
        props2.put("server.base", "${server.host}:${server.port}");
        props2.put("app.url", "http://${server.base}/api");

        System.out.println("--- Referencia encadenada ---");
        System.out.println("server.base resolves to: " + resolvePlaceholders("${server.base}", props2));
        System.out.println("app.url resolves to    : " + resolvePlaceholders("${app.url}", props2));

        System.out.println();

        // Demo con mensaje complejo
        Map<String, String> props3 = new HashMap<>();
        props3.put("app.name", "java-prep");
        props3.put("app.env", "production");
        props3.put("app.version", "2.3.1");
        props3.put("log.prefix", "[${app.name}@${app.env}]");

        String template3 = "${log.prefix} v${app.version} iniciado";
        System.out.println("--- Template complejo ---");
        System.out.println("Template : " + template3);
        System.out.println("Resultado: " + resolvePlaceholders(template3, props3));

        System.out.println();

        // Demo con placeholder desconocido (lanza excepción)
        System.out.println("--- Placeholder desconocido ---");
        try {
            resolvePlaceholders("hola ${unknown.key}", props1);
        } catch (IllegalArgumentException e) {
            System.out.println("Excepción: " + e.getMessage());
        }
    }
}
