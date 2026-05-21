import java.util.*;

public class Ejercicio4 {

    // Simulación de "base64": invertir el string
    static String encode(String value) {
        return new StringBuilder(value).reverse().toString();
    }

    static String decode(String encoded) {
        return new StringBuilder(encoded).reverse().toString();
    }

    static class Secret {
        String name;
        Map<String, String> encodedData;

        Secret(String name, Map<String, String> plainData) {
            this.name        = name;
            this.encodedData = new LinkedHashMap<>();
            plainData.forEach((k, v) -> encodedData.put(k, encode(v)));
        }

        void printRaw() {
            System.out.printf("Secret '%s' (datos codificados):%n", name);
            encodedData.forEach((k, v) ->
                    System.out.printf("  %-20s = %s%n", k, v));
        }
    }

    static class Pod {
        String name;
        Map<String, String> mountedSecrets = new LinkedHashMap<>();

        Pod(String name) {
            this.name = name;
        }

        void mountSecret(Secret secret) {
            System.out.printf("  Montando secret '%s' en pod '%s':%n", secret.name, name);
            secret.encodedData.forEach((k, encoded) -> {
                String decoded = decode(encoded);
                mountedSecrets.put(k, decoded);
                System.out.printf("    %-20s  encoded='%s'  →  decoded='%s'%n",
                        k, encoded, decoded);
            });
        }

        void printMounted() {
            System.out.printf("%nPod '%s' — variables montadas (decodificadas):%n", name);
            mountedSecrets.forEach((k, v) ->
                    System.out.printf("  %-20s = %s%n", k, v));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Secrets Demo ===\n");

        Map<String, String> credentials = new LinkedHashMap<>();
        credentials.put("DB_USERNAME",  "admin");
        credentials.put("DB_PASSWORD",  "s3cr3tP@ssw0rd");
        credentials.put("API_TOKEN",    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");

        Secret dbSecret = new Secret("db-credentials", credentials);
        dbSecret.printRaw();

        System.out.println();
        Pod pod = new Pod("backend-pod");
        pod.mountSecret(dbSecret);
        pod.printMounted();

        System.out.println("\n--- Verificación encode/decode ---");
        String original = "MySuperSecretValue";
        String enc      = encode(original);
        String dec      = decode(enc);
        System.out.printf("  Original: %s%n  Encoded:  %s%n  Decoded:  %s  [match=%b]%n",
                original, enc, dec, original.equals(dec));
    }
}
