import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class Ejercicio1 {

    static class JwtException extends RuntimeException {
        JwtException(String msg) { super(msg); }
    }

    static class Claims {
        final String username;
        final List<String> roles;

        Claims(String username, List<String> roles) {
            this.username = username;
            this.roles = roles;
        }
    }

    static class JwtUtil {
        private final byte[] secret;

        JwtUtil(String secret) {
            this.secret = secret.getBytes(StandardCharsets.UTF_8);
        }

        public String generate(String username, List<String> roles, long expiryMs) throws Exception {
            long now = System.currentTimeMillis();
            long exp = now + expiryMs;

            String header = b64url("""
                    {"alg":"HS256","typ":"JWT"}""".strip());

            String rolesJson = "[" + String.join(",",
                    roles.stream().map(r -> "\"" + r + "\"").toList()) + "]";
            String payloadJson = "{\"sub\":\"" + username + "\",\"roles\":" + rolesJson
                    + ",\"exp\":" + exp + ",\"iat\":" + now + "}";
            String payload = b64url(payloadJson);

            String signingInput = header + "." + payload;
            String signature = b64url(hmacSha256(signingInput));

            return signingInput + "." + signature;
        }

        public Claims verify(String token) throws Exception {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new JwtException("Token mal formado");

            String signingInput = parts[0] + "." + parts[1];
            String expectedSig = b64url(hmacSha256(signingInput));
            if (!expectedSig.equals(parts[2])) throw new JwtException("Firma inválida");

            String payloadJson = new String(Base64.getUrlDecoder().decode(pad(parts[1])));

            long exp = Long.parseLong(extract(payloadJson, "exp"));
            if (System.currentTimeMillis() > exp) throw new JwtException("Token expirado");

            String sub = extract(payloadJson, "sub");
            List<String> roles = extractRoles(payloadJson);

            return new Claims(sub, roles);
        }

        private String b64url(String input) {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(input.getBytes(StandardCharsets.UTF_8));
        }

        private String b64url(byte[] input) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
        }

        private byte[] hmacSha256(String data) throws Exception {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        }

        private String pad(String b64) {
            int pad = b64.length() % 4;
            if (pad == 2) return b64 + "==";
            if (pad == 3) return b64 + "=";
            return b64;
        }

        private String extract(String json, String key) {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search) + search.length();
            int end = json.indexOf(',', start);
            if (end < 0) end = json.indexOf('}', start);
            return json.substring(start, end).replaceAll("\"", "").strip();
        }

        private List<String> extractRoles(String json) {
            int start = json.indexOf("\"roles\":[") + 9;
            int end = json.indexOf(']', start);
            String inner = json.substring(start, end).replaceAll("\"", "").strip();
            if (inner.isEmpty()) return List.of();
            return List.of(inner.split(","));
        }
    }

    public static void main(String[] args) throws Exception {
        JwtUtil jwt = new JwtUtil("mi-secreto-super-seguro");

        String token = jwt.generate("jorge", List.of("ADMIN", "USER"), 60_000);
        System.out.println("Token generado:");
        System.out.println(token);
        System.out.println();

        Claims claims = jwt.verify(token);
        System.out.println("Token válido — sub=" + claims.username + " roles=" + claims.roles);

        System.out.println();
        System.out.println("--- Token con firma alterada ---");
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "firma_falsa";
        try {
            jwt.verify(tampered);
        } catch (JwtException e) {
            System.out.println("Excepción esperada: " + e.getMessage());
        }
    }
}
