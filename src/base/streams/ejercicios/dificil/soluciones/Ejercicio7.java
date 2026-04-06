package base.streams.ejemplosercicios.dificil.soluciones;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Ejercicio7 {

    public static void main(String[] args) {

        Product p1 = new Product("Laptop");
        Product p2 = new Product("Smartphone");
        Product p3 = new Product("Tablet");
        Product p4 = new Product("Auriculares");
        Product p5 = new Product("Teclado");

        Order o1 = new Order(List.of(p1, p2, p3));
        Order o2 = new Order(List.of(p2, p4));
        Order o3 = new Order(List.of(p1, p5));
        Order o4 = new Order(List.of(p3, p4, p5));

        List<User> users = List.of(
                new User("Alice", List.of(o1, o2)),
                new User("Bob", List.of(o3)),
                new User("Charlie", List.of(o4))
        );

        List<Product> uniqueProducts = users.stream()
                .flatMap(u -> u.getOrders().stream())
                .flatMap(o -> o.getProducts().stream())
                .distinct()
                .collect(Collectors.toList());

        uniqueProducts.forEach(System.out::println);
    }

    static class User {
        private final String name;
        private final List<Order> orders;

        public User(String name, List<Order> orders) {
            this.name = name;
            this.orders = orders;
        }

        public List<Order> getOrders() {
            return orders;
        }
    }

    static class Order {
        private final List<Product> products;

        public Order(List<Product> products) {
            this.products = products;
        }

        public List<Product> getProducts() {
            return products;
        }
    }

    static class Product {
        private final String name;

        public Product(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Product)) return false;
            Product product = (Product) o;
            return Objects.equals(name, product.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
