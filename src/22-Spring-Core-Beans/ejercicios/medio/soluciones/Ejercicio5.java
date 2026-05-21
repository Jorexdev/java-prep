import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ejercicio5 {

    static class PropertiesInjector {
        private final Map<String, String> props;
        private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

        PropertiesInjector(Map<String, String> props) {
            this.props = props;
        }

        String inject(String key, String defaultValue) {
            String raw = props.getOrDefault(key, defaultValue);
            return resolvePlaceholders(raw);
        }

        private String resolvePlaceholders(String value) {
            if (value == null) return null;
            Matcher m = PLACEHOLDER.matcher(value);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String ref = m.group(1);
                String resolved = props.getOrDefault(ref, "${" + ref + "}");
                m.appendReplacement(sb, Matcher.quoteReplacement(resolved));
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        Map<String, String> properties = new HashMap<>();
        properties.put("app.name", "MiAplicacion");
        properties.put("app.version", "2.1.0");
        properties.put("app.fullname", "${app.name}-${app.version}");
        properties.put("server.port", "8080");
        // "server.host" NO está definida → usará default

        PropertiesInjector injector = new PropertiesInjector(properties);

        System.out.println("=== @Value con defaults y resolución de placeholders ===\n");

        String[][] cases = {
            {"app.name",       "default-app",  "Propiedad presente"},
            {"app.version",    "0.0.1",        "Propiedad presente"},
            {"app.fullname",   "app-0.0.1",    "Placeholder compuesto"},
            {"server.port",    "80",           "Propiedad presente"},
            {"server.host",    "localhost",    "Propiedad AUSENTE → default"},
            {"db.url",         "jdbc:h2:mem",  "Propiedad AUSENTE → default"},
        };

        for (String[] c : cases) {
            String result = injector.inject(c[0], c[1]);
            System.out.printf("  @Value(\"%s\") default=\"%s\"%n    → \"%s\"  [%s]%n%n",
                c[0], c[1], result, c[2]);
        }
    }
}
