import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Ejercicio3 {

    static class LoginAttemptTracker {
        private static final int    MAX_FAILURES = 5;
        private static final long   WINDOW_MS    = 10 * 60 * 1000L;

        private final ConcurrentHashMap<String, Deque<Long>> attempts = new ConcurrentHashMap<>();
        private long clock = 1_000_000L;

        void advanceClock(long ms) { clock += ms; }

        boolean isBlocked(String ip) {
            return recentCount(ip) >= MAX_FAILURES;
        }

        void recordFailure(String ip) {
            attempts.computeIfAbsent(ip, k -> new ArrayDeque<>()).addLast(clock);
        }

        int recentCount(String ip) {
            long windowStart = clock - WINDOW_MS;
            return (int) attempts.getOrDefault(ip, new ArrayDeque<>())
                .stream().filter(t -> t > windowStart).count();
        }
    }

    static class AuthService {
        private final Map<String, String> credentials = Map.of("jorge", "password123");
        private final LoginAttemptTracker tracker;

        AuthService(LoginAttemptTracker t) { this.tracker = t; }

        String login(String ip, String username, String password) {
            if (tracker.isBlocked(ip))
                return "BLOQUEADO (ip=" + ip + ", intentos=" + tracker.recentCount(ip) + "/" + 5 + ")";
            if (credentials.getOrDefault(username, "").equals(password))
                return "OK — Bienvenido " + username;
            tracker.recordFailure(ip);
            return "FALLIDO (" + tracker.recentCount(ip) + "/5 intentos)";
        }
    }

    public static void main(String[] args) {
        LoginAttemptTracker tracker = new LoginAttemptTracker();
        AuthService         auth    = new AuthService(tracker);

        System.out.println("=== 6 intentos fallidos desde 192.168.1.1 ===");
        for (int i = 1; i <= 6; i++) {
            System.out.println("Intento " + i + ": " + auth.login("192.168.1.1", "jorge", "mal"));
        }

        System.out.println("\n=== Otra IP no afectada ===");
        System.out.println(auth.login("10.0.0.5", "jorge", "password123"));

        System.out.println("\n=== Avanzar reloj 11 minutos (ventana expirada) ===");
        tracker.advanceClock(11 * 60 * 1000L);
        System.out.println("Intentos recientes tras expirar: " + tracker.recentCount("192.168.1.1"));
        System.out.println(auth.login("192.168.1.1", "jorge", "password123"));
    }
}
