import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Ejercicio6 {

    static class ItemCarrito {
        final String producto;
        int cantidad;
        final double precio;

        ItemCarrito(String producto, int cantidad, double precio) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precio = precio;
        }

        double subtotal() {
            return cantidad * precio;
        }

        @Override
        public String toString() {
            return producto + " x" + cantidad + " @ " + precio + " = " + subtotal();
        }
    }

    static class Carrito {
        private final List<ItemCarrito> items = new ArrayList<>();

        void agregar(String producto, int cantidad, double precio) {
            if (cantidad < 1 || cantidad > 99) {
                throw new IllegalArgumentException("Cantidad debe estar entre 1 y 99");
            }

            Optional<ItemCarrito> existente = items.stream()
                .filter(i -> i.producto.equals(producto))
                .findFirst();

            if (existente.isPresent()) {
                int nuevaCantidad = existente.get().cantidad + cantidad;
                if (nuevaCantidad > 99) {
                    throw new IllegalArgumentException("Cantidad total no puede superar 99");
                }
                existente.get().cantidad = nuevaCantidad;
            } else {
                if (items.size() >= 10) {
                    throw new IllegalStateException("El carrito no puede tener más de 10 items distintos");
                }
                items.add(new ItemCarrito(producto, cantidad, precio));
            }

            double total = total();
            if (total > 10_000.0) {
                if (existente.isPresent()) {
                    existente.get().cantidad -= cantidad;
                } else {
                    items.removeLast();
                }
                throw new IllegalStateException("Total no puede superar 10 000 (sería " + total + ")");
            }
        }

        void eliminar(String producto) {
            items.removeIf(i -> i.producto.equals(producto));
        }

        void vaciar() {
            items.clear();
        }

        double total() {
            return items.stream().mapToDouble(ItemCarrito::subtotal).sum();
        }

        void imprimir() {
            items.forEach(i -> System.out.println("  " + i));
            System.out.println("  Total: " + total());
        }
    }

    public static void main(String[] args) {
        Carrito carrito = new Carrito();

        System.out.println("--- Casos válidos ---");
        carrito.agregar("Teclado", 2, 49.99);
        carrito.agregar("Ratón", 1, 29.99);
        carrito.agregar("Teclado", 1, 49.99);
        carrito.imprimir();

        System.out.println("\n--- Invariante: cantidad inválida ---");
        try {
            carrito.agregar("Monitor", 0, 299.99);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("\n--- Invariante: total > 10 000 ---");
        try {
            carrito.agregar("Servidor", 1, 9999.99);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("\n--- Invariante: más de 10 items distintos ---");
        Carrito carrito2 = new Carrito();
        for (int i = 1; i <= 10; i++) {
            carrito2.agregar("Producto" + i, 1, 1.0);
        }
        try {
            carrito2.agregar("ProductoExtra", 1, 1.0);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("\n--- Eliminar y vaciar ---");
        carrito.eliminar("Ratón");
        System.out.println("Tras eliminar Ratón:");
        carrito.imprimir();
        carrito.vaciar();
        System.out.println("Tras vaciar: total = " + carrito.total());
    }
}
