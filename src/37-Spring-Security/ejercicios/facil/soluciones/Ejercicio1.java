import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public class Ejercicio1 {

    static class PasswordEncoder {

        private static final SecureRandom RANDOM = new SecureRandom();

        public String encode(String raw) throws Exception {
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);
            byte[] hash = hash(salt, raw);
            return HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(hash);
        }

        public boolean matches(String raw, String encoded) throws Exception {
            String[] parts = encoded.split(":");
            byte[] salt = HexFormat.of().parseHex(parts[0]);
            byte[] expectedHash = HexFormat.of().parseHex(parts[1]);
            byte[] actualHash = hash(salt, raw);
            if (expectedHash.length != actualHash.length) return false;
            int diff = 0;
            for (int i = 0; i < expectedHash.length; i++) {
                diff |= expectedHash[i] ^ actualHash[i];
            }
            return diff == 0;
        }

        private byte[] hash(byte[] salt, String raw) throws Exception {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(raw.getBytes());
        }
    }

    public static void main(String[] args) throws Exception {
        PasswordEncoder encoder = new PasswordEncoder();

        String encoded = encoder.encode("secreto123");
        System.out.println("Encoded: " + encoded);

        System.out.println("matches('secreto123'): " + encoder.matches("secreto123", encoded));
        System.out.println("matches('otraPassword'): " + encoder.matches("otraPassword", encoded));
    }
}
