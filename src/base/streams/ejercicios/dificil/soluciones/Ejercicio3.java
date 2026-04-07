package base.streams.ejercicios.dificil.soluciones;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Ejercicio3 {

    public static void main(String[] args) {

        List<Product> products = List.of(
                new Product("Laptop", "Electronics", List.of("Portable", "High-end", "Work")),
                new Product("Smartphone", "Electronics", List.of("Portable", "Touchscreen")),
                new Product("Tablet", "Electronics", List.of("Portable", "Budget")),
                new Product("Jeans", "Clothing", List.of("Denim", "Casual")),
                new Product("T-Shirt", "Clothing", List.of("Cotton", "Casual")),
                new Product("Blender", "Home Appliances", List.of("Kitchen", "Electric")),
                new Product("Microwave", "Home Appliances", List.of("Kitchen", "Electric")),
                new Product("Refrigerator", "Home Appliances", List.of("Kitchen", "Large")),
                new Product("Sofa", "Furniture", List.of("Living Room", "Comfortable")),
                new Product("Desk Chair", "Furniture", List.of("Office", "Ergonomic"))
        );

        products
                . stream()
                .collect(Collectors.toMap(Function.identity(), Product::getTags))
                .values()
                .stream()
                .flatMap(List::stream)
                .forEach(System.out::println);


    }

    static
    class Product {
        private final String name;
        private final String category;
        private final List<String> tags;

        public Product(String name, String category, List<String> tags) {
            this.name = name;
            this.category = category;
            this.tags = tags;
        }

        public String getCategory() {
            return category;
        }

        public List<String> getTags() {
            return tags;
        }

        @Override
        public String toString() {
            return name + " (" + category + ")";
        }
    }

}
