import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Ejercicio 2 (Difícil) — Encrypted properties
// Valores ENC(...) se desencriptan con ROT13; el resto pasan tal cual
public class Ejercicio2 {

    static class Rot13 {

        static String apply(String input) {
            StringBuilder sb = new StringBuilder(input.length());
            for (char c : input.toCharArray()) {
                if (c >= 'a' && c <= 'z') {
                    sb.append((char) ('a' + (c - 'a' + 13) % 26));
                } else if (c >= 'A' && c <= 'Z') {
                    sb.append((char) ('A' + (c - 'A' + 13) % 26));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        static String encrypt(String plain) {
            return "ENC(" + apply(plain) + ")";
        }

        static String decrypt(String enc) {
            if (!enc.startsWith("ENC(") || !enc.endsWith(")")) {
                throw new IllegalArgumentException("No es un valor ENC: " + enc);
            }
            String inner = enc.substring(4, enc.length() - 1);
            return apply(inner); // ROT13 es su propio inverso
        }
    }

    static class EncryptedPropertySource {
        private final Map<String, String> rawProps;

        EncryptedPropertySource(Map<String, String> rawProps) {
            this.rawProps = Map.copyOf(rawProps);
        }

        /**
         * Devuelve el valor de la propiedad, desencriptando si empieza con ENC(...)
         */
        public Optional<String> get(String key) {
            String raw = rawProps.get(key);
            if (raw == null) return Optional.empty();

            if (raw.startsWith("ENC(") && raw.endsWith(")")) {
                return Optional.of(Rot13.decrypt(raw));
            }
            return Optional.of(raw);
        }

        /** Devuelve el valor crudo (sin desencriptar) */
        public Optional<String> getRaw(String key) {
            return Optional.ofNullable(rawProps.get(key));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Encrypted properties con ROT13 ===");
        System.out.println();

        // Paso 1: Encriptar para mostrar cómo se generan los valores ENC(...)
        System.out.println("--- Proceso de encriptación ---");
        String[] plainValues = {
            "super-secret-password",
            "sk-live-abc123xyz789",
        };
        for (String plain : plainValues) {
            String encrypted = Rot13.encrypt(plain);
            System.out.println("  plain    : " + plain);
            System.out.println("  ENC(...)  : " + encrypted);
            System.out.println();
        }

        // Paso 2: Construir las propiedades tal como estarían en el fichero
        Map<String, String> rawProps = new HashMap<>();
        rawProps.put("app.name", "java-prep-app");                           // plain
        rawProps.put("db.password", Rot13.encrypt("super-secret-password")); // encriptada
        rawProps.put("api.key", Rot13.encrypt("sk-live-abc123xyz789"));       // encriptada

        System.out.println("--- Propiedades en el fichero (raw) ---");
        rawProps.forEach((k, v) -> System.out.println("  " + k + " = " + v));
        System.out.println();

        EncryptedPropertySource source = new EncryptedPropertySource(rawProps);

        // Paso 3: Leer las propiedades (desencriptando automáticamente)
        System.out.println("--- Propiedades desencriptadas ---");
        String[] keys = {"app.name", "db.password", "api.key"};
        for (String key : keys) {
            String raw = source.getRaw(key).orElse("N/A");
            String decrypted = source.get(key).orElse("N/A");
            boolean isEncrypted = raw.startsWith("ENC(");
            System.out.printf("  %-15s | raw: %-45s | decrypted: %-30s | encrypted: %b%n",
                    key, raw, decrypted, isEncrypted);
        }

        System.out.println();
        System.out.println("--- Verificación ROT13 (aplicar dos veces = original) ---");
        String original = "HelloWorld123!";
        String once = Rot13.apply(original);
        String twice = Rot13.apply(once);
        System.out.println("  original  : " + original);
        System.out.println("  ROT13 x1  : " + once);
        System.out.println("  ROT13 x2  : " + twice);
        System.out.println("  igual?    : " + original.equals(twice));
    }
}
