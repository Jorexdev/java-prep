import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// Ejercicio 4 (Difícil) — Deep merge YAML
// Map<String, Object> anidado: deepMerge(base, override)
// Los Maps se fusionan recursivamente; los valores escalares se sobreescriben
@SuppressWarnings("unchecked")
public class Ejercicio4 {

    /**
     * Fusión profunda de dos estructuras YAML representadas como Map<String, Object>.
     *
     * - Si una clave existe solo en base → se hereda en el resultado.
     * - Si una clave existe solo en override → se añade al resultado.
     * - Si ambas tienen la clave:
     *     - Si ambos valores son Map → fusión recursiva
     *     - En otro caso → el override gana
     */
    static Map<String, Object> deepMerge(Map<String, Object> base, Map<String, Object> override) {
        Map<String, Object> result = new LinkedHashMap<>(base); // copia base

        for (Map.Entry<String, Object> entry : override.entrySet()) {
            String key = entry.getKey();
            Object overrideVal = entry.getValue();
            Object baseVal = result.get(key);

            if (baseVal instanceof Map && overrideVal instanceof Map) {
                // Ambos son mapas → fusión recursiva
                result.put(key, deepMerge(
                        (Map<String, Object>) baseVal,
                        (Map<String, Object>) overrideVal));
            } else {
                // Override gana (escalar o tipos distintos)
                result.put(key, overrideVal);
            }
        }

        return result;
    }

    /** Imprime el mapa con indentación para visualizarlo como YAML */
    static void print(Map<String, Object> map, int indent) {
        String pad = " ".repeat(indent * 2);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getValue() instanceof Map) {
                System.out.println(pad + e.getKey() + ":");
                print((Map<String, Object>) e.getValue(), indent + 1);
            } else {
                System.out.println(pad + e.getKey() + ": " + e.getValue());
            }
        }
    }

    // Helper para construir mapas de forma concisa
    static Map<String, Object> map(Object... kvPairs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kvPairs.length; i += 2) {
            m.put((String) kvPairs[i], kvPairs[i + 1]);
        }
        return m;
    }

    public static void main(String[] args) {
        System.out.println("=== Deep merge YAML (3 niveles) ===");
        System.out.println();

        // Base: application.yaml
        Map<String, Object> base = map(
            "app", map(
                "name", "mi-app",
                "version", "1.0.0"
            ),
            "server", map(
                "port", "8080",
                "ssl", map(
                    "enabled", "false",
                    "cert", "/etc/certs/dev.pem"
                )
            ),
            "db", map(
                "url", "jdbc:h2:mem:testdb",
                "pool", map(
                    "min", "2",
                    "max", "5",
                    "timeout", "30s"
                )
            ),
            "log", map(
                "level", "DEBUG",
                "file", "app.log"
            )
        );

        // Override: application-prod.yaml (solo lo que cambia)
        Map<String, Object> override = map(
            "server", map(
                "port", "443",         // sobreescribe
                "ssl", map(
                    "enabled", "true", // sobreescribe
                    "cert", "/etc/certs/prod.pem" // sobreescribe
                    // host-header heredado de base (si lo hubiera)
                )
            ),
            "db", map(
                "url", "jdbc:postgresql://prod-host/mydb", // sobreescribe
                "pool", map(
                    "max", "50"       // sobreescribe; min y timeout heredados de base
                )
            ),
            "log", map(
                "level", "WARN"       // sobreescribe; file heredado de base
            )
        );

        System.out.println("=== BASE ===");
        print(base, 0);

        System.out.println();
        System.out.println("=== OVERRIDE (prod) ===");
        print(override, 0);

        System.out.println();
        Map<String, Object> merged = deepMerge(base, override);
        System.out.println("=== RESULTADO deepMerge(base, prod) ===");
        print(merged, 0);

        System.out.println();
        System.out.println("=== Verificaciones clave ===");

        Map<String, Object> serverMerged = (Map<String, Object>) merged.get("server");
        Map<String, Object> sslMerged = (Map<String, Object>) serverMerged.get("ssl");
        Map<String, Object> dbMerged = (Map<String, Object>) merged.get("db");
        Map<String, Object> poolMerged = (Map<String, Object>) dbMerged.get("pool");
        Map<String, Object> logMerged = (Map<String, Object>) merged.get("log");

        System.out.println("server.port          = " + serverMerged.get("port")
                + " (override ganó, era 8080)");
        System.out.println("server.ssl.enabled   = " + sslMerged.get("enabled")
                + " (override ganó, era false)");
        System.out.println("server.ssl.cert      = " + sslMerged.get("cert")
                + " (override ganó)");
        System.out.println("db.url               = " + dbMerged.get("url")
                + " (override ganó)");
        System.out.println("db.pool.max          = " + poolMerged.get("max")
                + " (override ganó, era 5)");
        System.out.println("db.pool.min          = " + poolMerged.get("min")
                + " (heredado de base)");
        System.out.println("db.pool.timeout      = " + poolMerged.get("timeout")
                + " (heredado de base)");
        System.out.println("log.level            = " + logMerged.get("level")
                + " (override ganó, era DEBUG)");
        System.out.println("log.file             = " + logMerged.get("file")
                + " (heredado de base)");
        System.out.println("app.name             = "
                + ((Map<String, Object>) merged.get("app")).get("name")
                + " (heredado de base)");
    }
}
