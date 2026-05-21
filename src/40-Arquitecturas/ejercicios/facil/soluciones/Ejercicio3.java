import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ejercicio3 {

    static class Producto {
        final int id;
        final String nombre;
        final double precio;

        Producto(int id, String nombre, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }

        @Override
        public String toString() {
            return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + "}";
        }
    }

    interface ProductoRepository {
        void save(Producto p);
        Optional<Producto> findById(int id);
        List<Producto> findAll();
        void delete(int id);
    }

    static class InMemoryProductoRepository implements ProductoRepository {
        private final Map<Integer, Producto> store = new HashMap<>();

        @Override
        public void save(Producto p) {
            store.put(p.id, p);
        }

        @Override
        public Optional<Producto> findById(int id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Producto> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public void delete(int id) {
            store.remove(id);
        }
    }

    static class ProductoService {
        private final ProductoRepository repository;

        ProductoService(ProductoRepository repository) {
            this.repository = repository;
        }

        void crear(Producto p) {
            repository.save(p);
        }

        Optional<Producto> buscar(int id) {
            return repository.findById(id);
        }

        List<Producto> listar() {
            return repository.findAll();
        }

        void eliminar(int id) {
            repository.delete(id);
        }
    }

    public static void main(String[] args) {
        ProductoRepository repo = new InMemoryProductoRepository();
        ProductoService service = new ProductoService(repo);

        service.crear(new Producto(1, "Teclado", 49.99));
        service.crear(new Producto(2, "Ratón", 29.99));
        service.crear(new Producto(3, "Monitor", 299.99));

        System.out.println("Todos: ");
        service.listar().forEach(p -> System.out.println("  " + p));

        System.out.println("Buscar id=2: " + service.buscar(2));
        System.out.println("Buscar id=99: " + service.buscar(99));

        service.eliminar(2);
        System.out.println("Tras eliminar id=2: ");
        service.listar().forEach(p -> System.out.println("  " + p));
    }
}
