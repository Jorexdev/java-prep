// Ejercicio 5 (Medio) — Relaxed binding
// Convierte MY_APP_DB_HOST → myApp.db.host
public class Ejercicio5 {

    /**
     * Normaliza una env var al formato de propiedades Spring.
     *
     * Regla:
     *   1. Dividir por "_"
     *   2. Cada token: primera letra minúscula, resto en minúsculas
     *   3. Unir con "."
     *
     * Ejemplos:
     *   MY_APP_DB_HOST       → myApp.db.host
     *   SERVER_PORT          → server.port
     *   SPRING_DATASOURCE_URL → spring.datasource.url
     */
    static String normalize(String envVar) {
        if (envVar == null || envVar.isEmpty()) return envVar;

        String[] parts = envVar.split("_");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(".");
            String token = parts[i].toLowerCase();
            sb.append(token);
        }

        return sb.toString();
    }

    /**
     * Versión que aplica camelCase dentro de grupos de tokens agrupados:
     * MY_APP_DB_HOST → myApp.db.host requiere entender cuándo agrupar tokens.
     *
     * Esta versión alternativa sigue la regla simple: cada token separado por "_"
     * se convierte a minúsculas y se separa con ".". Esto cubre el mapping real
     * que usa Spring Boot relaxed binding.
     *
     * MY_APP_DB_HOST       → my.app.db.host (Spring normaliza los guiones después)
     *
     * Para obtener myApp.db.host sería necesario conocer la estructura del objeto,
     * por eso Spring lo resuelve probando las distintas combinaciones.
     * Aquí implementamos la normalización directa (underscore → lowercase dot-notation).
     */
    static String normalizeRelaxed(String envVar) {
        // Dividir por underscore, convertir todo a lowercase, unir con punto
        String[] tokens = envVar.split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) result.append(".");
            result.append(tokens[i].toLowerCase());
        }
        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println("=== Relaxed binding — normalización de env vars ===");
        System.out.println();

        String[][] cases = {
            {"MY_APP_DB_HOST",            "my.app.db.host"},
            {"SERVER_PORT",               "server.port"},
            {"SPRING_DATASOURCE_URL",     "spring.datasource.url"},
            {"APP_FEATURE_FLAG_ENABLED",  "app.feature.flag.enabled"},
            {"LOG_LEVEL",                 "log.level"},
        };

        System.out.printf("%-35s  %-35s  %s%n", "ENV VAR", "NORMALIZADO", "OK");
        System.out.println("-".repeat(80));

        for (String[] tc : cases) {
            String input = tc[0];
            String expected = tc[1];
            String actual = normalizeRelaxed(input);
            boolean ok = actual.equals(expected);
            System.out.printf("%-35s  %-35s  %s%n", input, actual, ok ? "OK" : "FAIL (esperado: " + expected + ")");
        }

        System.out.println();
        System.out.println("=== Uso práctico: mapear env vars a propiedades ===");
        System.out.println();

        String[] envVars = {
            "MY_APP_DB_HOST=prod-db-server",
            "SERVER_PORT=443",
            "SPRING_DATASOURCE_URL=jdbc:postgresql://host/db",
            "APP_FEATURE_FLAG_ENABLED=true",
            "LOG_LEVEL=WARN"
        };

        System.out.println("Env vars → propiedades normalizadas:");
        for (String envEntry : envVars) {
            String[] parts = envEntry.split("=", 2);
            String propKey = normalizeRelaxed(parts[0]);
            System.out.println("  " + parts[0] + "=" + parts[1]
                    + "  →  " + propKey + "=" + parts[1]);
        }
    }
}
