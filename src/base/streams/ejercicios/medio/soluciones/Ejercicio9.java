package base.streams.ejercicios.medio.soluciones;

import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio9 {

    public static void main(String[] args) {

        // Ejercicio: mapa cliente -> total acumulado de sus pedidos
        List<Order> orders = List.of(
                new Order("Ana",   150.50),
                new Order("Luis",  200.00),
                new Order("Ana",   99.99),
                new Order("Pedro", 300.00),
                new Order("Luis",  50.00),
                new Order("Clara", 120.75),
                new Order("Pedro", 80.25)
        );

        // forma concisa con summingDouble — un solo collect en lugar de dos
        orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getClient,
                        Collectors.summingDouble(Order::getAmount) // acumula montos del mismo cliente
                ))
                .forEach((client, total) -> System.out.println(client + " -> " + total));
    }

    static class Order {

        private final String client;
        private final double amount;

        Order(String client, double amount) {
            this.client = client;
            this.amount = amount;
        }

        public String getClient() {
            return client;
        }

        public double getAmount() {
            return amount;
        }
    }
}
