import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Ejercicio7 {

    public static void main(String[] args) {

        // Ejercicio: lista única de productos comprados por todos los usuarios
        // estructura: User -> List<Order> -> List<Product>
        Product p1 = new Product("Laptop");
        Product p2 = new Product("Smartphone");
        Product p3 = new Product("Tablet");
        Product p4 = new Product("Auriculares");
        Product p5 = new Product("Teclado");

        List<User> users = List.of(
                new User("Alice",   List.of(new Order(List.of(p1, p2, p3)), new Order(List.of(p2, p4)))),
                new User("Bob",     List.of(new Order(List.of(p1, p5)))),
                new User("Charlie", List.of(new Order(List.of(p3, p4, p5))))
        );

        List<Product> uniqueProducts = users.stream()
                .flatMap(u -> u.getOrders().stream())         // aplana User -> Order
                .flatMap(o -> o.getProducts().stream())       // aplana Order -> Product
                .distinct()                                   // Product.equals compara por nombre
                .collect(Collectors.toList());

        uniqueProducts.forEach(System.out::println);
    }

    static class User {
        private final String name;
        private final List<Order> orders;
        User(String name, List<Order> orders) { this.name = name; this.orders = orders; }
        public List<Order> getOrders() { return orders; }
    }

    static class Order {
        private final List<Product> products;
        Order(List<Product> products) { this.products = products; }
        public List<Product> getProducts() { return products; }
    }

    static class Product {
        private final String name;
        Product(String name) { this.name = name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Product)) return false;
            return Objects.equals(name, ((Product) o).name); // distinct() usa equals para comparar
        }

        @Override
        public int hashCode() { return Objects.hash(name); }

        @Override
        public String toString() { return name; }
    }
}
