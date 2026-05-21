import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    record Request(String method, String path, Map<String, String> params) {}

    record Response(int status, String body) {
        @Override
        public String toString() {
            return "Response{status=" + status + ", body='" + body + "'}";
        }
    }

    interface HandlerMethod {
        Response handle(Request req);
    }

    static class RouteEntry {
        String method;
        String pattern;
        HandlerMethod handler;

        RouteEntry(String method, String pattern, HandlerMethod handler) {
            this.method = method;
            this.pattern = pattern;
            this.handler = handler;
        }

        boolean matches(String method, String path) {
            if (!this.method.equalsIgnoreCase(method)) return false;
            String[] patternParts = this.pattern.split("/");
            String[] pathParts = path.split("/");
            if (patternParts.length != pathParts.length) return false;
            for (int i = 0; i < patternParts.length; i++) {
                if (!patternParts[i].startsWith("{") && !patternParts[i].equals(pathParts[i])) return false;
            }
            return true;
        }

        Map<String, String> extractPathVars(String path) {
            Map<String, String> vars = new HashMap<>();
            String[] patternParts = this.pattern.split("/");
            String[] pathParts = path.split("/");
            for (int i = 0; i < patternParts.length; i++) {
                if (patternParts[i].startsWith("{")) {
                    String varName = patternParts[i].substring(1, patternParts[i].length() - 1);
                    vars.put(varName, pathParts[i]);
                }
            }
            return vars;
        }
    }

    static class DispatcherServlet {
        private final List<RouteEntry> routes = new ArrayList<>();
        private final List<Map<Integer, String>> productoStore = new ArrayList<>();
        private int nextId = 1;

        void register(String method, String path, HandlerMethod handler) {
            routes.add(new RouteEntry(method, path, handler));
        }

        Response dispatch(Request req) {
            for (RouteEntry entry : routes) {
                if (entry.matches(req.method(), req.path())) {
                    Map<String, String> pathVars = entry.extractPathVars(req.path());
                    Map<String, String> allParams = new HashMap<>(req.params());
                    allParams.putAll(pathVars);
                    return entry.handler.handle(new Request(req.method(), req.path(), allParams));
                }
            }
            return new Response(404, "No handler found for " + req.method() + " " + req.path());
        }
    }

    public static void main(String[] args) {
        Map<Integer, String> productos = new HashMap<>();
        int[] idGen = {1};

        DispatcherServlet servlet = new DispatcherServlet();

        // GET /productos
        servlet.register("GET", "/productos", req ->
            new Response(200, "Productos: " + productos)
        );

        // POST /productos
        servlet.register("POST", "/productos", req -> {
            String nombre = req.params().getOrDefault("nombre", "Nuevo Producto");
            productos.put(idGen[0]++, nombre);
            return new Response(201, "Creado: " + nombre);
        });

        // GET /productos/{id}
        servlet.register("GET", "/productos/{id}", req -> {
            int id = Integer.parseInt(req.params().get("id"));
            String nombre = productos.get(id);
            if (nombre == null) return new Response(404, "Producto no encontrado: " + id);
            return new Response(200, "Producto: " + id + " -> " + nombre);
        });

        System.out.println("-- POST /productos --");
        System.out.println(servlet.dispatch(new Request("POST", "/productos", Map.of("nombre", "Teclado"))));

        System.out.println("\n-- POST /productos --");
        System.out.println(servlet.dispatch(new Request("POST", "/productos", Map.of("nombre", "Ratón"))));

        System.out.println("\n-- GET /productos --");
        System.out.println(servlet.dispatch(new Request("GET", "/productos", Map.of())));

        System.out.println("\n-- GET /productos/1 --");
        System.out.println(servlet.dispatch(new Request("GET", "/productos/1", Map.of())));

        System.out.println("\n-- GET /productos/99 --");
        System.out.println(servlet.dispatch(new Request("GET", "/productos/99", Map.of())));
    }
}
