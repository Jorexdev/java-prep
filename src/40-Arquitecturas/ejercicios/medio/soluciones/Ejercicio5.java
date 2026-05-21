import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio5 {

    interface Specification<T> {
        boolean isSatisfiedBy(T candidate);

        default Specification<T> and(Specification<T> other) {
            return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
        }

        default Specification<T> or(Specification<T> other) {
            return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
        }

        default Specification<T> not() {
            return candidate -> !this.isSatisfiedBy(candidate);
        }
    }

    static class Producto {
        final String id;
        final String categoria;
        final double precio;
        final int stock;
        final boolean activo;

        Producto(String id, String categoria, double precio, int stock, boolean activo) {
            this.id = id;
            this.categoria = categoria;
            this.precio = precio;
            this.stock = stock;
            this.activo = activo;
        }

        @Override
        public String toString() {
            return "Producto{id='" + id + "', categoria='" + categoria
                + "', precio=" + precio + ", stock=" + stock + ", activo=" + activo + "}";
        }
    }

    static class PrecioEntre implements Specification<Producto> {
        private final double min;
        private final double max;

        PrecioEntre(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean isSatisfiedBy(Producto p) {
            return p.precio >= min && p.precio <= max;
        }
    }

    static class CategoriaDe implements Specification<Producto> {
        private final String categoria;

        CategoriaDe(String categoria) {
            this.categoria = categoria;
        }

        @Override
        public boolean isSatisfiedBy(Producto p) {
            return categoria.equalsIgnoreCase(p.categoria);
        }
    }

    static class EnStock implements Specification<Producto> {
        @Override
        public boolean isSatisfiedBy(Producto p) {
            return p.stock > 0;
        }
    }

    static class Activo implements Specification<Producto> {
        @Override
        public boolean isSatisfiedBy(Producto p) {
            return p.activo;
        }
    }

    static class ProductoCatalogo {
        private final List<Producto> productos;

        ProductoCatalogo(List<Producto> productos) {
            this.productos = productos;
        }

        List<Producto> buscar(Specification<Producto> spec) {
            return productos.stream()
                .filter(spec::isSatisfiedBy)
                .collect(Collectors.toList());
        }
    }

    public static void main(String[] args) {
        List<Producto> catalogo = List.of(
            new Producto("p1", "electrónica", 50.0, 5, true),
            new Producto("p2", "electrónica", 250.0, 3, true),
            new Producto("p3", "ropa", 30.0, 0, true),
            new Producto("p4", "ropa", 45.0, 10, true),
            new Producto("p5", "libros", 15.0, 8, false),
            new Producto("p6", "libros", 20.0, 4, true),
            new Producto("p7", "electrónica", 80.0, 0, true)
        );

        ProductoCatalogo repo = new ProductoCatalogo(catalogo);

        Specification<Producto> spec =
            new PrecioEntre(10, 100)
                .and(new EnStock())
                .and(new CategoriaDe("electrónica").not());

        System.out.println("Spec: precio entre 10-100, en stock, NO electrónica");
        System.out.println("Resultados:");
        repo.buscar(spec).forEach(p -> System.out.println("  " + p));

        System.out.println("\nSpec: electrónica OR (ropa con stock)");
        Specification<Producto> spec2 =
            new CategoriaDe("electrónica")
                .or(new CategoriaDe("ropa").and(new EnStock()));
        repo.buscar(spec2).forEach(p -> System.out.println("  " + p));

        System.out.println("\nSpec: activos con precio < 25");
        Specification<Producto> spec3 =
            new Activo().and(new PrecioEntre(0, 25));
        repo.buscar(spec3).forEach(p -> System.out.println("  " + p));
    }
}
