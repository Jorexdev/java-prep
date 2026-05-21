import java.util.List;

// @RestController
// @RequestMapping("/api")
public class Ejercicio1 {

    static class Producto {
        int id;
        String nombre;
        double precio;

        Producto(int id, String nombre, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }

        @Override
        public String toString() {
            return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + "}";
        }
    }

    static class ApiResponse<T> {
        int status;
        T body;

        ApiResponse(int status, T body) {
            this.status = status;
            this.body = body;
        }
    }

    // @GetMapping("/productos")
    static ApiResponse<List<Producto>> getProductos() {
        List<Producto> productos = List.of(
            new Producto(1, "Teclado", 49.99),
            new Producto(2, "Ratón", 29.99),
            new Producto(3, "Monitor", 299.99)
        );
        return new ApiResponse<>(200, productos);
    }

    public static void main(String[] args) {
        ApiResponse<List<Producto>> response = getProductos();

        System.out.println("Status: " + response.status);
        System.out.println("Body:");
        response.body.forEach(p -> System.out.println("  " + p));
    }
}
