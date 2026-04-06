package base.streams.ejemplosercicios.facil.soluciones;

import java.util.List;

public class Ejercicio4 {

    public static void main(String[] args) {

        List<Product> productList = List.of(
                new Product("Lejia", 20L),
                new Product("Chocolate", 12L),
                new Product("Barbacoa", 120L),
                new Product("Coche", 2000L)
        );

        productList
                .stream()
                .filter(x -> x.getPrice() >= 100)
                .forEach(System.out::println);

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
            return "Product{" +
                    "name='" + name + '\'' +
                    ", price=" + price +
                    '}';
        }
    }
}
