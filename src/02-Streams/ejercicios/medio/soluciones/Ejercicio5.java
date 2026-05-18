import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio5 {
    record Pedido(String cliente, double importe) {}

    public static void main(String[] args) {
        List<Pedido> pedidos = List.of(
            new Pedido("Ana",    150.0),
            new Pedido("Luis",   320.0),
            new Pedido("Marta",   80.0),
            new Pedido("Carlos", 450.0),
            new Pedido("Ana",    200.0)
        );

        long total    = pedidos.stream().collect(Collectors.counting());
        double suma   = pedidos.stream().collect(Collectors.summingDouble(Pedido::importe));
        double media  = pedidos.stream().collect(Collectors.averagingDouble(Pedido::importe));

        System.out.println("Total pedidos: " + total);
        System.out.printf("Suma importes: %.2f€%n", suma);
        System.out.printf("Media importe: %.2f€%n", media);
    }
}
