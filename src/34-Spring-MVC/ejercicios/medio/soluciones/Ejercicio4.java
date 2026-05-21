import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Ejercicio4 {

    static class Producto {
        int id;
        String nombre;

        Producto(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return "Producto{id=" + id + ", nombre='" + nombre + "'}";
        }
    }

    static class ProductoV2 {
        int id;
        String nombre;
        String categoria;
        int stock;

        ProductoV2(int id, String nombre, String categoria, int stock) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.stock = stock;
        }

        @Override
        public String toString() {
            return "ProductoV2{id=" + id + ", nombre='" + nombre
                + "', categoria='" + categoria + "', stock=" + stock + "}";
        }
    }

    static class ApiRouter {
        private final Map<String, Function<Integer, Object>> routes = new HashMap<>();

        void register(String version, Function<Integer, Object> handler) {
            routes.put(version, handler);
        }

        Object route(String version, int id) {
            Function<Integer, Object> handler = routes.get(version);
            if (handler == null) throw new RuntimeException("Versión no soportada: " + version);
            return handler.apply(id);
        }
    }

    public static void main(String[] args) {
        ApiRouter router = new ApiRouter();

        router.register("v1", id -> new Producto(id, "Teclado"));
        router.register("v2", id -> new ProductoV2(id, "Teclado", "Periféricos", 42));

        System.out.println("-- v1 --");
        System.out.println(router.route("v1", 1));

        System.out.println("\n-- v2 --");
        System.out.println(router.route("v2", 1));
    }
}
