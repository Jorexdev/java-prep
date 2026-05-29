import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Ejercicio6 {

    // --- Resultado de la validación ---

    static class ValidationResult {
        final boolean valid;
        final String error;
        final Map<String, String> claims; // solo presente si valid=true

        private ValidationResult(boolean valid, String error, Map<String, String> claims) {
            this.valid  = valid;
            this.error  = error;
            this.claims = claims;
        }

        static ValidationResult ok(Map<String, String> claims) {
            return new ValidationResult(true, null, claims);
        }

        static ValidationResult fail(String error) {
            return new ValidationResult(false, error, null);
        }

        @Override
        public String toString() {
            return valid
                ? "VALID — claims=" + claims
                : "INVALID — " + error;
        }
    }

    // --- Token simulado (header.payload.signature) ---
    // payload: clave=valor separado por ','  (ej. sub=alice,roles=ADMIN,exp=9999)

    static Map<String, String> parsePayload(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) return Collections.emptyMap();
        Map<String, String> map = new HashMap<>();
        for (String pair : parts[1].split(",")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }

    // HMAC simulado: suma de caracteres de header+payload XOR secreto
    static String simulatedHmac(String headerDotPayload, String secret) {
        long hash = 0;
        for (char c : headerDotPayload.toCharArray()) hash = hash * 31 + c;
        for (char c : secret.toCharArray())           hash = hash * 17 + c;
        return Long.toHexString(Math.abs(hash));
    }

    // --- Interfaz de validador ---

    interface JwtValidator {
        ValidationResult validate(String token);
    }

    // --- Validador 1: Firma ---

    static class SignatureValidator implements JwtValidator {
        private final String secret;

        SignatureValidator(String secret) { this.secret = secret; }

        @Override
        public ValidationResult validate(String token) {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return ValidationResult.fail("Formato de token inválido: se esperan 3 partes");
            }
            String expected = simulatedHmac(parts[0] + "." + parts[1], secret);
            if (!expected.equals(parts[2])) {
                return ValidationResult.fail(
                    "Firma inválida — esperada: " + expected + ", recibida: " + parts[2]);
            }
            return ValidationResult.ok(parsePayload(token));
        }
    }

    // --- Validador 2: Expiración ---

    static class ExpirationValidator implements JwtValidator {
        private final AtomicLong clock; // tiempo simulado en segundos

        ExpirationValidator(AtomicLong clock) { this.clock = clock; }

        @Override
        public ValidationResult validate(String token) {
            Map<String, String> claims = parsePayload(token);
            String expStr = claims.get("exp");
            if (expStr == null) {
                return ValidationResult.fail("Claim 'exp' ausente");
            }
            long exp = Long.parseLong(expStr);
            long now = clock.get();
            if (now >= exp) {
                return ValidationResult.fail(
                    "Token expirado — exp=" + exp + ", now=" + now);
            }
            return ValidationResult.ok(claims);
        }
    }

    // --- Validador 3: Claims requeridos ---

    static class ClaimsValidator implements JwtValidator {
        private final List<String> required;

        ClaimsValidator(List<String> required) { this.required = required; }

        @Override
        public ValidationResult validate(String token) {
            Map<String, String> claims = parsePayload(token);
            for (String key : required) {
                if (!claims.containsKey(key)) {
                    return ValidationResult.fail("Claim requerido ausente: '" + key + "'");
                }
            }
            return ValidationResult.ok(claims);
        }
    }

    // --- ValidationChain ---

    static class ValidationChain {
        private final List<JwtValidator> validators;

        ValidationChain(List<JwtValidator> validators) {
            this.validators = validators;
        }

        ValidationResult validate(String token) {
            for (JwtValidator v : validators) {
                ValidationResult result = v.validate(token);
                if (!result.valid) return result;
            }
            // Retornar claims del último (garantizado válido)
            return ValidationResult.ok(parsePayload(token));
        }
    }

    // --- Helpers para construir tokens ---

    static String buildToken(String payload, String secret) {
        String header = "hs256";
        String sig    = simulatedHmac(header + "." + payload, secret);
        return header + "." + payload + "." + sig;
    }

    public static void main(String[] args) {
        String secret = "mi-secreto-256";
        AtomicLong clock = new AtomicLong(1000L); // t=1000s

        ValidationChain chain = new ValidationChain(List.of(
            new SignatureValidator(secret),
            new ExpirationValidator(clock),
            new ClaimsValidator(List.of("sub", "roles"))
        ));

        // Token 1: completamente válido
        String t1 = buildToken("sub=alice,roles=ADMIN,exp=9999", secret);

        // Token 2: firma alterada
        String t2 = buildToken("sub=bob,roles=USER,exp=9999", secret) + "xxx";

        // Token 3: expirado (exp < now=1000)
        String t3 = buildToken("sub=carol,roles=USER,exp=500", secret);

        // Token 4: sin claim 'roles'
        String t4 = buildToken("sub=dave,exp=9999", secret);

        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("Token 1 (válido)",          t1);
        tokens.put("Token 2 (firma alterada)",   t2);
        tokens.put("Token 3 (expirado)",         t3);
        tokens.put("Token 4 (sin claim roles)",  t4);

        System.out.println("Reloj simulado: t=" + clock.get() + "s\n");

        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            System.out.println("--- " + entry.getKey() + " ---");
            System.out.println(chain.validate(entry.getValue()));
            System.out.println();
        }
    }
}
