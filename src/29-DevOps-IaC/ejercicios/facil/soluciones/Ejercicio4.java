import java.util.*;
import java.util.regex.*;

public class Ejercicio4 {

    static class TerraformConfig {
        private final Map<String, String> variables;
        private final Map<String, String> rawConfig;

        TerraformConfig(Map<String, String> variables, Map<String, String> rawConfig) {
            this.variables  = new LinkedHashMap<>(variables);
            this.rawConfig  = new LinkedHashMap<>(rawConfig);
        }

        // Resolve ${var.name} references in all config values
        Map<String, String> resolve() {
            Pattern pattern = Pattern.compile("\\$\\{var\\.([^}]+)\\}");
            Map<String, String> resolved = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : rawConfig.entrySet()) {
                String value = entry.getValue();
                Matcher m = pattern.matcher(value);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    String varName = m.group(1);
                    String replacement = variables.getOrDefault(varName, "<undefined:" + varName + ">");
                    m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
                m.appendTail(sb);
                resolved.put(entry.getKey(), sb.toString());
            }
            return resolved;
        }
    }

    public static void main(String[] args) {
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("region",        "us-east-1");
        vars.put("env",           "production");
        vars.put("instance_type", "t3.medium");
        vars.put("ami",           "ami-0abcd1234");

        Map<String, String> rawConfig = new LinkedHashMap<>();
        rawConfig.put("region",        "${var.region}");
        rawConfig.put("ami",           "${var.ami}");
        rawConfig.put("instance_type", "${var.instance_type}");
        rawConfig.put("tags",          "env=${var.env},region=${var.region}");
        rawConfig.put("name",          "app-server-${var.env}");
        rawConfig.put("static_value",  "no-variable-here");
        rawConfig.put("missing_ref",   "${var.undefined_var}");

        TerraformConfig config = new TerraformConfig(vars, rawConfig);

        System.out.println("Variables:");
        vars.forEach((k, v) -> System.out.printf("  var.%-20s = %s%n", k, v));
        System.out.println();

        System.out.println("Raw config:");
        rawConfig.forEach((k, v) -> System.out.printf("  %-20s = %s%n", k, v));
        System.out.println();

        System.out.println("Resolved config:");
        Map<String, String> resolved = config.resolve();
        resolved.forEach((k, v) -> System.out.printf("  %-20s = %s%n", k, v));
    }
}
