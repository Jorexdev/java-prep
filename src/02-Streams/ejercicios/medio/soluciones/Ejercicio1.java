import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio1 {

    public static void main(String[] args) {

        // Ejercicio: agrupar productos por categoría y calcular precio promedio de cada una
        List<Product> products = List.of(
                new Product("Laptop",      "Electrónica",  1200L),
                new Product("Smartphone",  "Electrónica",  800L),
                new Product("Cámara",      "Electrónica",  500L),
                new Product("Silla",       "Muebles",      150L),
                new Product("Mesa",        "Muebles",      300L),
                new Product("Lámpara",     "Muebles",      90L),
                new Product("Libro A",     "Libros",       20L),
                new Product("Libro B",     "Libros",       25L),
                new Product("Auriculares", "Electrónica",  150L),
                new Product("Alfombra",    "Muebles",      200L)
        );

        products
                .stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,              // clave: categoría
                        Collectors.averagingLong(Product::getPrice) // valor: media de precios
                ))
                .forEach((cat, avg) -> System.out.println(cat + " -> " + avg));
    }

    static class Product {

        private final String name;
        private final String category;
        private final Long price;

        Product(String name, String category, Long price) {
            this.name = name;
            this.category = category;
            this.price = price;
        }

        public String getCategory() {
            return category;
        }

        public Long getPrice() {
            return price;
        }
    }
}
