import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

public class Ejercicio4 {

    // Excepcion de rate limiting
    static class RateLimitException extends RuntimeException {
        RateLimitException(String message) { super(message); }
    }

    // Anotacion @RateLimit
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface RateLimit {
        int maxCalls();
        long windowMs();
    }

    // Estado de rate limit por metodo: ventana deslizante de timestamps
    static class RateLimiter {
        private final ConcurrentHashMap<String, Deque<Long>> ventanas = new ConcurrentHashMap<>();

        // Retorna true si la llamada esta permitida, false si supera el limite
        synchronized boolean intentar(String metodo, int maxCalls, long windowMs) {
            long now = System.currentTimeMillis();
            Deque<Long> timestamps = ventanas.computeIfAbsent(metodo, k -> new ArrayDeque<>());

            // Eliminar timestamps fuera de la ventana
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= maxCalls) {
                return false; // limite superado
            }

            timestamps.addLast(now);
            return true;
        }

        int llamadasEnVentana(String metodo) {
            Deque<Long> d = ventanas.get(metodo);
            return d == null ? 0 : d.size();
        }
    }

    interface ApiServicio {
        String buscar(String query);
        String publicar(String contenido); // sin rate limit
    }

    static class ApiServicioReal implements ApiServicio {
        @Override
        @RateLimit(maxCalls = 3, windowMs = 1000)
        public String buscar(String query) {
            return "resultado: " + query;
        }

        @Override
        public String publicar(String contenido) {
            return "publicado: " + contenido;
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T wrapRateLimit(T target) {
        Class<?> realClass = target.getClass();
        RateLimiter limiter = new RateLimiter();

        return (T) Proxy.newProxyInstance(
            realClass.getClassLoader(),
            realClass.getInterfaces(),
            (proxy, method, args) -> {
                // Buscar @RateLimit en la clase real
                Method realMethod;
                try {
                    realMethod = realClass.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    realMethod = method;
                }

                RateLimit rl = realMethod.getAnnotation(RateLimit.class);
                if (rl != null) {
                    boolean permitido = limiter.intentar(method.getName(), rl.maxCalls(), rl.windowMs());
                    if (!permitido) {
                        throw new RateLimitException(
                            "Rate limit superado: " + method.getName() +
                            " maxCalls=" + rl.maxCalls() + " en " + rl.windowMs() + "ms"
                        );
                    }
                    System.out.println("  [RATE_LIMIT] " + method.getName() +
                                       " -> llamada " + limiter.llamadasEnVentana(method.getName()) +
                                       "/" + rl.maxCalls() + " en ventana de " + rl.windowMs() + "ms");
                }

                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        );
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== @RateLimit aspect ===");
        System.out.println("buscar: maxCalls=3, windowMs=1000");
        System.out.println("publicar: sin rate limit\n");

        ApiServicio api = wrapRateLimit(new ApiServicioReal());

        System.out.println("--- 5 llamadas rapidas a buscar (3 deben pasar, 2 fallar) ---");
        int exitos = 0, fallos = 0;
        for (int i = 1; i <= 5; i++) {
            try {
                String r = api.buscar("query-" + i);
                System.out.println("  Llamada " + i + " -> OK: " + r);
                exitos++;
            } catch (RateLimitException e) {
                System.out.println("  Llamada " + i + " -> BLOQUEADA: " + e.getMessage());
                fallos++;
            }
        }

        System.out.println();
        System.out.println("Exitos: " + exitos + " (esperado: 3)");
        System.out.println("Fallos: " + fallos + " (esperado: 2)");

        System.out.println();
        System.out.println("--- publicar (sin rate limit): llamadas libres ---");
        for (int i = 1; i <= 5; i++) {
            String r = api.publicar("post-" + i);
            System.out.println("  " + r);
        }

        System.out.println();
        System.out.println("--- Esperando 1100ms para que la ventana expire ---");
        Thread.sleep(1100);
        System.out.println("--- 2 llamadas mas a buscar (deben pasar) ---");
        for (int i = 1; i <= 2; i++) {
            try {
                String r = api.buscar("new-query-" + i);
                System.out.println("  Llamada -> OK: " + r);
            } catch (RateLimitException e) {
                System.out.println("  ERROR inesperado: " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("En Spring/Resilience4j: @RateLimiter(name=\"api\") con");
        System.out.println("configuracion en application.yml: limitForPeriod, limitRefreshPeriod.");
        System.out.println("Para distribuido: usar Redis con Lua scripts o Bucket4j.");
    }
}
