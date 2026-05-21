import java.util.concurrent.ConcurrentHashMap;

public class Ejercicio4 {

    static class TokenBucket {
        private final int    capacity;
        private final double refillPerMs;
        private double tokens;
        private long   lastRefill;

        TokenBucket(int capacity, int refillPerSecond) {
            this.capacity     = capacity;
            this.refillPerMs  = refillPerSecond / 1000.0;
            this.tokens       = capacity;
            this.lastRefill   = 0L;
        }

        synchronized boolean tryConsume(long nowMs) {
            double elapsed = nowMs - lastRefill;
            tokens = Math.min(capacity, tokens + elapsed * refillPerMs);
            lastRefill = nowMs;
            if (tokens >= 1.0) { tokens--; return true; }
            return false;
        }
    }

    static class RateLimiter {
        private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
        private final int capacity;
        private final int refillPerSecond;

        RateLimiter(int capacity, int refillPerSecond) {
            this.capacity        = capacity;
            this.refillPerSecond = refillPerSecond;
        }

        boolean allow(String userId, long nowMs) {
            TokenBucket bucket = buckets.computeIfAbsent(userId,
                k -> new TokenBucket(capacity, refillPerSecond));
            boolean ok = bucket.tryConsume(nowMs);
            System.out.printf("[RateLimit] %s t=%dms → %s%n", userId, nowMs, ok ? "OK" : "RECHAZADO (429)");
            return ok;
        }
    }

    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter(5, 5);
        long t = 0L;

        System.out.println("=== 8 peticiones rápidas (límite=5) ===");
        for (int i = 1; i <= 8; i++) limiter.allow("user1", t + i);

        System.out.println("\n=== Esperar 1000ms (refill de 5 tokens) ===");
        t += 1000L;
        for (int i = 1; i <= 5; i++) limiter.allow("user1", t + i);

        System.out.println("\n=== user2 independiente (bucket propio) ===");
        limiter.allow("user2", t);
        limiter.allow("user2", t + 1);
        limiter.allow("user2", t + 2);

        System.out.println("\n=== user1 agotado, user2 aún tiene tokens ===");
        for (int i = 3; i <= 10; i++) limiter.allow("user1", t + i);
        for (int i = 3; i <= 5; i++)  limiter.allow("user2", t + i);
    }
}
