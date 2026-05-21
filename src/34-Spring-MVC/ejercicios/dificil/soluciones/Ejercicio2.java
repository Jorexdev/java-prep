import java.util.List;
import java.util.Map;

public class Ejercicio2 {

    record Request(String method, String path, Map<String, String> headers) {}

    record Response(int status, String body) {
        @Override
        public String toString() {
            return "Response{status=" + status + ", body='" + body + "'}";
        }
    }

    interface HandlerInterceptor {
        boolean preHandle(Request req);
        void postHandle(Request req, Response res);
        void afterCompletion(Request req, Response res, Exception ex);
    }

    interface Handler {
        Response handle(Request req);
    }

    static class InterceptorChain {
        private final List<HandlerInterceptor> interceptors;
        private final Handler target;

        InterceptorChain(List<HandlerInterceptor> interceptors, Handler target) {
            this.interceptors = interceptors;
            this.target = target;
        }

        Response execute(Request req) {
            for (HandlerInterceptor interceptor : interceptors) {
                if (!interceptor.preHandle(req)) {
                    Response rejected = new Response(401, "Rechazado por interceptor");
                    interceptor.afterCompletion(req, rejected, null);
                    return rejected;
                }
            }

            Response res = null;
            Exception caught = null;
            try {
                res = target.handle(req);
            } catch (Exception e) {
                caught = e;
                res = new Response(500, "Error interno: " + e.getMessage());
            }

            for (HandlerInterceptor interceptor : interceptors) {
                interceptor.postHandle(req, res);
            }
            for (HandlerInterceptor interceptor : interceptors) {
                interceptor.afterCompletion(req, res, caught);
            }
            return res;
        }
    }

    static class AuthInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(Request req) {
            String token = req.headers().get("Authorization");
            boolean ok = token != null && !token.isBlank();
            if (!ok) System.out.println("[AuthInterceptor] Sin token — acceso denegado");
            else System.out.println("[AuthInterceptor] Token válido: " + token);
            return ok;
        }

        @Override
        public void postHandle(Request req, Response res) {}

        @Override
        public void afterCompletion(Request req, Response res, Exception ex) {}
    }

    static class LoggingInterceptor implements HandlerInterceptor {
        private long start;

        @Override
        public boolean preHandle(Request req) {
            start = System.nanoTime();
            System.out.println("[LoggingInterceptor] -> " + req.method() + " " + req.path());
            return true;
        }

        @Override
        public void postHandle(Request req, Response res) {
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("[LoggingInterceptor] <- " + res.status() + " en " + ms + " ms");
        }

        @Override
        public void afterCompletion(Request req, Response res, Exception ex) {
            if (ex != null) System.out.println("[LoggingInterceptor] Excepción: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        Handler handler = req -> new Response(200, "Datos seguros para " + req.path());
        List<HandlerInterceptor> interceptors = List.of(new LoggingInterceptor(), new AuthInterceptor());
        InterceptorChain chain = new InterceptorChain(interceptors, handler);

        System.out.println("-- Request con token --");
        Response r1 = chain.execute(new Request("GET", "/api/datos", Map.of("Authorization", "Bearer abc123")));
        System.out.println("Resultado: " + r1);

        System.out.println("\n-- Request sin token --");
        Response r2 = chain.execute(new Request("GET", "/api/datos", Map.of()));
        System.out.println("Resultado: " + r2);
    }
}
