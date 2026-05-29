import java.util.List;

public class Ejercicio4 {

    interface JsonSerializable {
        String toJson();
    }

    record Usuario(String nombre, String email) implements JsonSerializable {
        @Override
        public String toJson() {
            return """
                    {"nombre": "%s", "email": "%s"}""".formatted(nombre, email);
        }
    }

    record Producto(String nombre, double precio) implements JsonSerializable {
        @Override
        public String toJson() {
            return """
                    {"nombre": "%s", "precio": %.2f}""".formatted(nombre, precio);
        }
    }

    record Pedido(int id, Usuario usuario, Producto producto) implements JsonSerializable {
        @Override
        public String toJson() {
            // Reutiliza toJson() de componentes para JSON anidado
            return """
                    {
                      "id": %d,
                      "usuario": %s,
                      "producto": %s
                    }""".formatted(id, usuario.toJson(), producto.toJson());
        }
    }

    public static void main(String[] args) {
        Usuario u1 = new Usuario("Ana García", "ana@ejemplo.com");
        Usuario u2 = new Usuario("Carlos López", "carlos@ejemplo.com");
        Producto p1 = new Producto("Laptop Pro", 1299.99);
        Producto p2 = new Producto("Teclado Mecánico", 89.50);
        Pedido ped1 = new Pedido(101, u1, p1);
        Pedido ped2 = new Pedido(102, u2, p2);

        List<JsonSerializable> entidades = List.of(u1, p1, ped1, u2, p2, ped2);

        System.out.println("=== Records con comportamiento polimórfico ===\n");
        entidades.forEach(e -> {
            System.out.println("Tipo: " + e.getClass().getSimpleName());
            System.out.println(e.toJson());
            System.out.println();
        });

        // Demostrar polimorfismo: cualquier JsonSerializable puede serializarse
        System.out.println("--- Lista de JSON ---");
        String jsonArray = "[" +
            entidades.stream()
                .map(JsonSerializable::toJson)
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b) +
            "]";
        // Mostrar solo primeros 100 chars para brevedad
        System.out.println(jsonArray.substring(0, Math.min(jsonArray.length(), 120)) + "...");
    }
}
