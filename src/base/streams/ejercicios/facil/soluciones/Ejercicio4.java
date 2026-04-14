package base.streams.ejercicios.facil.soluciones;

import java.util.List;

public class Ejercicio4 {

    public static void main(String[] args) {

        // Ejercicio: obtener los nombres de productos con precio mayor a 100
        List<Product> products = List.of(
                new Product("Lejia",     20L),
                new Product("Chocolate", 12L),
                new Product("Barbacoa",  120L),
                new Product("Coche",     2000L)
        );

        products
                .stream()
                .filter(x -> x.getPrice() >= 100)  // descarta los baratos
                .forEach(System.out::println);      // para obtener solo nombres: .map(Product::getName)
    }

    static class Product {

        private final String name;
        private final Long price;

        Product(String name, Long price) {
            this.name = name;
            this.price = price;
        }

        public Long getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return "Product{name='" + name + "', price=" + price + '}';
        }
    }
}
