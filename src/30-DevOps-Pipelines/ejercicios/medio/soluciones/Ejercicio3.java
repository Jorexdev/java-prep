import java.util.*;

public class Ejercicio3 {

    // ROT13: cifrado simétrico (cifrar == descifrar)
    static String rot13(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if      (c >= 'A' && c <= 'Z') sb.append((char) ('A' + (c - 'A' + 13) % 26));
            else if (c >= 'a' && c <= 'z') sb.append((char) ('a' + (c - 'a' + 13) % 26));
            else sb.append(c);
        }
        return sb.toString();
    }

    static class SecretStore {
        private Map<String, String> store = new LinkedHashMap<>();
        private Set<String> knownSecrets  = new HashSet<>();

        void addSecret(String key, String plainValue) {
            store.put(key, rot13(plainValue));
            knownSecrets.add(key);
        }

        String get(String key) {
            String encoded = store.get(key);
            if (encoded == null) throw new NoSuchElementException("Secret not found: " + key);
            return rot13(encoded); // ROT13 es simétrico
        }

        // Versión enmascarada para logs
        String masked(String key) {
            return store.containsKey(key) ? "***" : "<missing>";
        }

        boolean isMasked(String value) {
            return knownSecrets.stream()
                    .map(k -> rot13(store.getOrDefault(k, "")))
                    .anyMatch(v -> v.equals(value));
        }
    }

    static class PipelineLogger {
        SecretStore secrets;

        PipelineLogger(SecretStore secrets) {
            this.secrets = secrets;
        }

        void log(String stage, String message) {
            // Enmascarar cualquier valor de secret que aparezca en el mensaje
            String safe = message;
            for (String key : secrets.store.keySet()) {
                String realValue = secrets.get(key);
                safe = safe.replace(realValue, "***");
            }
            System.out.printf("  [%-14s] %s%n", stage, safe);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Secrets Management Demo ===\n");

        SecretStore store = new SecretStore();
        store.addSecret("DB_PASS",     "SuperSecret123!");
        store.addSecret("API_KEY",     "sk-abc123xyz789");
        store.addSecret("JWT_SECRET",  "my-jwt-signing-key");

        System.out.println("Secrets almacenados (ROT13):");
        store.store.forEach((k, v) -> System.out.printf("  %-14s = %s%n", k, v));

        System.out.println("\nDesencriptando para uso interno:");
        store.store.keySet().forEach(k ->
                System.out.printf("  %-14s → %s%n", k, store.get(k)));

        PipelineLogger logger = new PipelineLogger(store);

        System.out.println("\nEjecución de stages (con enmascarado de secrets en logs):");

        // Stage 1: usa DB_PASS
        String dbPass = store.get("DB_PASS");
        logger.log("db-migrate",
                "Conectando a postgres con password=" + dbPass);

        // Stage 2: usa API_KEY
        String apiKey = store.get("API_KEY");
        logger.log("api-deploy",
                "Desplegando con API_KEY=" + apiKey + " en el header Authorization");

        // Stage 3: usa JWT_SECRET
        String jwt = store.get("JWT_SECRET");
        logger.log("jwt-config",
                "Configurando signing key=" + jwt + " para el servicio auth");

        // Acceso a secret inexistente
        System.out.println("\n--- Acceso a secret inexistente ---");
        try {
            store.get("MISSING_SECRET");
        } catch (NoSuchElementException e) {
            System.out.println("  Error: " + e.getMessage());
        }

        System.out.printf("%n  Masked en logs: DB_PASS=%s  API_KEY=%s  JWT_SECRET=%s%n",
                store.masked("DB_PASS"), store.masked("API_KEY"), store.masked("JWT_SECRET"));
    }
}
