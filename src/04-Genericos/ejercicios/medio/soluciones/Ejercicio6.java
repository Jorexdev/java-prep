import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ejercicio6 {

    interface Repositorio<T, ID> {
        void save(T entidad);
        Optional<T> findById(ID id);
        List<T> findAll();
    }

    static class RepositorioEnMemoria<T, ID> implements Repositorio<T, ID> {
        private final Map<ID, T> almacen = new HashMap<>();
        private final java.util.function.Function<T, ID> extractorId;

        RepositorioEnMemoria(java.util.function.Function<T, ID> extractorId) {
            this.extractorId = extractorId;
        }

        @Override
        public void save(T entidad) {
            almacen.put(extractorId.apply(entidad), entidad);
        }

        @Override
        public Optional<T> findById(ID id) {
            return Optional.ofNullable(almacen.get(id));
        }

        @Override
        public List<T> findAll() {
            return new ArrayList<>(almacen.values());
        }
    }

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

    public static void main(String[] args) {

        Repositorio<Producto, Integer> repo = new RepositorioEnMemoria<>(p -> p.id);

        repo.save(new Producto(1, "Laptop",  999.99));
        repo.save(new Producto(2, "Ratón",    29.99));
        repo.save(new Producto(3, "Monitor", 349.99));

        System.out.println("findById(2): " + repo.findById(2).orElse(null));
        System.out.println("findById(9): " + repo.findById(9).orElse(null));
        System.out.println("findAll:     " + repo.findAll());
    }
}
