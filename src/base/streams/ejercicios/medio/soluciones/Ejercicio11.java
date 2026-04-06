package base.streams.ejercicios.medio.soluciones;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Ejercicio11 {

    public static void main(String[] args) {
        List<Product> products = List.of(
                new Product("Laptop", "Electronics", 1200.00),
                new Product("Smartphone", "Electronics", 800.00),
                new Product("Tablet", "Electronics", 500.00),
                new Product("Jeans", "Clothing", 40.00),
                new Product("T-Shirt", "Clothing", 15.00),
                new Product("Blender", "Home Appliances", 100.00),
                new Product("Microwave", "Home Appliances", 150.00)
        );

        products
                .stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.maxBy(Comparator.comparing(Product::getPrice))
                ))
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(System.out::println);

    }


    static class Product {
        private final String name;
        private final String category;
        private final double price;

        public Product(String name, String category, double price) {
            this.name = name;
            this.category = category;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public double getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return name + " (" + category + ") - $" + price;
        }
    }

}
