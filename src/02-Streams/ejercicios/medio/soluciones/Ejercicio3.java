import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio3 {
    record Producto(String nombre, double precio) {}

    public static void main(String[] args) {
        List<Producto> productos = List.of(
            new Producto("Laptop",  999.99),
            new Producto("Raton",    24.99),
            new Producto("Teclado",  49.99),
            new Producto("Monitor", 299.99),
            new Producto("Laptop",  899.99)
        );

        Map<String, Double> mapa = productos.stream()
            .collect(Collectors.toMap(
                Producto::nombre,
                Producto::precio,
                (p1, p2) -> Math.min(p1, p2)
            ));
        mapa.forEach((k, v) -> System.out.printf("%-10s → %.2f€%n", k, v));
    }
}
