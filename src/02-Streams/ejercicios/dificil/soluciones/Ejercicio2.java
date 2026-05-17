import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio2 {

    public static void main(String[] args) {

        // Ejercicio: agrupar ventas por cliente y obtener la de mayor monto con Collectors.reducing
        List<Sale> sales = List.of(
                new Sale("Ana",   150.50, LocalDate.of(2025, 1, 10)),
                new Sale("Luis",  200.00, LocalDate.of(2025, 1, 15)),
                new Sale("Ana",   99.99,  LocalDate.of(2025, 2, 5)),
                new Sale("Pedro", 300.00, LocalDate.of(2025, 2, 20)),
                new Sale("Luis",  50.00,  LocalDate.of(2025, 3, 1)),
                new Sale("Clara", 120.75, LocalDate.of(2025, 3, 10)),
                new Sale("Pedro", 80.25,  LocalDate.of(2025, 3, 15))
        );

        // Collectors.reducing: acumulador que conserva solo el elemento que gana la comparación
        sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::getClient,
                        Collectors.reducing((s1, s2) -> s1.getAmount() >= s2.getAmount() ? s1 : s2)
                ))
                .forEach((client, maxSale) -> System.out.println(client + " -> " + maxSale));
    }

    static class Sale {

        private final String client;
        private final double amount;
        private final LocalDate date;

        Sale(String client, double amount, LocalDate date) {
            this.client = client;
            this.amount = amount;
            this.date = date;
        }

        public String getClient() { return client; }
        public double getAmount() { return amount; }

        @Override
        public String toString() {
            return client + " – " + amount + " on " + date;
        }
    }
}
