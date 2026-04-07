package base.streams.ejercicios.medio.soluciones;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Ejercicio9 {

    public static void main(String[] args) {
        List<Order> orders = List.of(
                new Order("Ana", 150.50),
                new Order("Luis", 200.00),
                new Order("Ana", 99.99),
                new Order("Pedro", 300.00),
                new Order("Luis", 50.00),
                new Order("Clara", 120.75),
                new Order("Pedro", 80.25)
        );


        // mi solucion inicial

        orders
                .stream()
                .collect(Collectors.groupingBy(
                        Order::getClient
                ))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry
                                .getValue()
                                .stream()
                                .map(Order::getAmount)
                                .reduce(Double::sum)
                ))
                .entrySet()
                .forEach(System.out::println);


        // chatgpt
        orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getClient,
                        Collectors.summingDouble(Order::getAmount)
                ))
                .entrySet()
                .forEach(System.out::println);



//         Demencia:
//        Consumer<String> consumer = s -> System.out.println("Procesando: " + s);
//
//        Function<String, Void> function = s -> {
//            consumer.accept(s);
//            return null;
//        };


    }

    static class Order {
        private final String client;
        private final double amount;

        public Order(String client, double amount) {
            this.client = client;
            this.amount = amount;
        }

        public String getClient() {
            return client;
        }

        public double getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return client + ": " + amount;
        }
    }

}
