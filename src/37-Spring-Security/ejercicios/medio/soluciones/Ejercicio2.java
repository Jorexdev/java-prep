import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Ejercicio2 {

    static class TokenException extends RuntimeException {
        TokenException(String msg) { super(msg); }
    }

    record TokenPair(String accessToken, String refreshToken) {}

    static class JwtUtil {
        private final byte[] secret;

        JwtUtil(String secret) {
            this.secret = secret.getBytes(StandardCharsets.UTF_8);
        }

        String generate(String username, long expiryMs) throws Exception {
            long now = System.currentTimeMillis();
            String header = b64url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
            String payload = b64url("{\"sub\":\"" + username + "\",\"exp\":"
                    + (now + expiryMs) + ",\"iat\":" + now + ",\"jti\":\"" + UUID.randomUUID() + "\"}");
            String sig = b64url(hmac(header + "." + payload));
            return header + "." + payload + "." + sig;
        }

        String extractUsername(String token) {
            String[] parts = token.split("\\.");
            String json = new String(Base64.getUrlDecoder().decode(pad(parts[1])));
            return extractField(json, "sub");
        }

        void verifySignature(String token) throws Exception {
            String[] parts = token.split("\\.");
            String expected = b64url(hmac(parts[0] + "." + parts[1]));
            if (!expected.equals(parts[2])) throw new TokenException("Firma inválida");
        }

        boolean isExpired(String token) {
            String[] parts = token.split("\\.");
            String json = new String(Base64.getUrlDecoder().decode(pad(parts[1])));
            long exp = Long.parseLong(extractField(json, "exp"));
            return System.currentTimeMillis() > exp;
        }

        private String b64url(String s) {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(s.getBytes(StandardCharsets.UTF_8));
        }
        private String b64url(byte[] b) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
        }
        private byte[] hmac(String data) throws Exception {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        }
        private String pad(String s) {
            return switch (s.length() % 4) {
                case 2 -> s + "==";
                case 3 -> s + "=";
                default -> s;
            };
        }
        private String extractField(String json, String key) {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search);
            if (start >= 0) {
                start += search.length();
                return json.substring(start, json.indexOf('"', start));
            }
            search = "\"" + key + "\":";
            start = json.indexOf(search) + search.length();
            int end = json.indexOf(',', start);
            if (end < 0) end = json.indexOf('}', start);
            return json.substring(start, end).strip();
        }
    }

    static class TokenService {
        private static final long ACCESS_TTL  = 5  * 60 * 1000L;
        private static final long REFRESH_TTL = 7  * 24 * 60 * 60 * 1000L;

        private final JwtUtil jwt = new JwtUtil("refresh-secret-key");
        private final Set<String> blacklist = new HashSet<>();

        public TokenPair login(String username) throws Exception {
            String access  = jwt.generate(username, ACCESS_TTL);
            String refresh = jwt.generate(username, REFRESH_TTL);
            return new TokenPair(access, refresh);
        }

        public String refresh(String refreshToken) throws Exception {
            if (blacklist.contains(refreshToken)) throw new TokenException("Refresh token revocado");
            jwt.verifySignature(refreshToken);
            if (jwt.isExpired(refreshToken)) throw new TokenException("Refresh token expirado");
            String username = jwt.extractUsername(refreshToken);
            return jwt.generate(username, ACCESS_TTL);
        }

        public void logout(String refreshToken) {
            blacklist.add(refreshToken);
            System.out.println("Refresh token invalidado");
        }
    }

    public static void main(String[] args) throws Exception {
        TokenService service = new TokenService();

        System.out.println("--- Login ---");
        TokenPair pair = service.login("jorge");
        System.out.println("Access:  " + pair.accessToken().substring(0, 40) + "...");
        System.out.println("Refresh: " + pair.refreshToken().substring(0, 40) + "...");

        JwtUtil util = new JwtUtil("refresh-secret-key");
        System.out.println("Username del access: " + util.extractUsername(pair.accessToken()));

        System.out.println();
        System.out.println("--- Refresh (access caducó, renovamos) ---");
        String newAccess = service.refresh(pair.refreshToken());
        System.out.println("Nuevo access: " + newAccess.substring(0, 40) + "...");
        System.out.println("Username del nuevo access: " + util.extractUsername(newAccess));

        System.out.println();
        System.out.println("--- Logout ---");
        service.logout(pair.refreshToken());

        System.out.println();
        System.out.println("--- Intento de refresh tras logout ---");
        try {
            service.refresh(pair.refreshToken());
        } catch (TokenException e) {
            System.out.println("Excepción esperada: " + e.getMessage());
        }
    }
}
