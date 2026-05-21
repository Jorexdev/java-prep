import java.util.Map;

public class Ejercicio4 {

    interface SecurityFilter {
        boolean doFilter(Request req, Response res);
    }

    static class Request {
        final Map<String, String> headers;
        final String path;

        Request(Map<String, String> headers, String path) {
            this.headers = Map.copyOf(headers);
            this.path = path;
        }
    }

    static class Response {
        int status;
        String body;

        Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    static class AuthHeaderFilter implements SecurityFilter {

        @Override
        public boolean doFilter(Request req, Response res) {
            String auth = req.headers.get("Authorization");
            if (auth != null && auth.startsWith("Bearer ") && auth.length() > 7) {
                System.out.println("[AuthHeaderFilter] Token presente: " + auth);
                return true;
            }
            res.status = 401;
            res.body = "Unauthorized: Bearer token requerido";
            System.out.println("[AuthHeaderFilter] Bloqueado — sin token válido");
            return false;
        }
    }

    public static void main(String[] args) {
        SecurityFilter filter = new AuthHeaderFilter();

        System.out.println("--- Request con token ---");
        Request req1 = new Request(Map.of("Authorization", "Bearer abc123"), "/api/datos");
        Response res1 = new Response(200, "OK");
        boolean pass1 = filter.doFilter(req1, res1);
        System.out.println("Pasó: " + pass1 + " | Status: " + res1.status);

        System.out.println();

        System.out.println("--- Request sin token ---");
        Request req2 = new Request(Map.of("Content-Type", "application/json"), "/api/datos");
        Response res2 = new Response(200, "OK");
        boolean pass2 = filter.doFilter(req2, res2);
        System.out.println("Pasó: " + pass2 + " | Status: " + res2.status + " | Body: " + res2.body);
    }
}
