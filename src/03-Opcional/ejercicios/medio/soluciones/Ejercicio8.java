import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Ejercicio8 {

    interface Repositorio<T> {
        Optional<T> findByNombre(String nombre);
    }

    static class Producto {
        private final String nombre;
        private final double precio;

        Producto(String nombre, double precio) {
            this.nombre = nombre;
            this.precio = precio;
        }

        String getNombre()  { return nombre; }
        double getPrecio()  { return precio; }

        @Override
        public String toString() {
            return nombre + " (" + precio + "€)";
        }
    }

    static class RepositorioProductos implements Repositorio<Producto> {
        private final List<Producto> productos = new ArrayList<>();

        RepositorioProductos() {
            productos.add(new Producto("Teclado",  79.99));
            productos.add(new Producto("Monitor", 349.00));
            productos.add(new Producto("Ratón",    29.50));
        }

        @Override
        public Optional<Producto> findByNombre(String nombre) {
            return productos.stream()
                    .filter(p -> p.getNombre().equalsIgnoreCase(nombre))
                    .findFirst();
        }
    }

    public static void main(String[] args) {
        RepositorioProductos repo = new RepositorioProductos();

        // Producto existente — se obtiene el precio real
        double precioTeclado = repo.findByNombre("Teclado")
                .map(Producto::getPrecio)
                .orElse(0.0);
        System.out.println("Precio Teclado:  " + precioTeclado);

        // Producto inexistente — orElse devuelve 0.0
        double precioDesconocido = repo.findByNombre("Auriculares")
                .map(Producto::getPrecio)
                .orElse(0.0);
        System.out.println("Precio Auriculares: " + precioDesconocido);

        // También podemos obtener el producto completo
        repo.findByNombre("Monitor")
                .ifPresentOrElse(
                        p -> System.out.println("Producto encontrado: " + p),
                        () -> System.out.println("Producto no encontrado")
                );
    }
}
