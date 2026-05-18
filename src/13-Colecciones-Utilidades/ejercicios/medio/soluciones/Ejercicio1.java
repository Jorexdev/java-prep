import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ejercicio1 {

    record Factura(int id, String cliente, double importe) {
        @Override public String toString() {
            return String.format("F%d %-10s %.2f€", id, cliente, importe);
        }
    }

    public static void main(String[] args) {
        List<Factura> facturas = new ArrayList<>(List.of(
            new Factura(3, "Zara",  1500.0),
            new Factura(1, "Ana",    300.0),
            new Factura(5, "Marcos", 900.0),
            new Factura(2, "Bea",   2100.0),
            new Factura(4, "Luis",   600.0)
        ));

        facturas.sort(Comparator.comparingDouble(Factura::importe));
        System.out.println("Por importe asc:");
        facturas.forEach(f -> System.out.println("  " + f));

        facturas.sort(Comparator.comparing(Factura::cliente));
        System.out.println("Por cliente alfa:");
        facturas.forEach(f -> System.out.println("  " + f));

        facturas.sort(Comparator.comparingInt(Factura::id).reversed());
        System.out.println("Por id desc:");
        facturas.forEach(f -> System.out.println("  " + f));
    }
}
