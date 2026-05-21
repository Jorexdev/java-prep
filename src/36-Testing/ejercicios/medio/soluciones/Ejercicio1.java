import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    static class MockBuilder {
        private final Map<String, Object> respuestas  = new HashMap<>();
        private final Map<String, Integer> invocaciones = new HashMap<>();

        void when(String metodo, Object valor) {
            respuestas.put(metodo, valor);
        }

        @SuppressWarnings("unchecked")
        <T> T invoke(String metodo) {
            invocaciones.merge(metodo, 1, Integer::sum);
            return (T) respuestas.get(metodo);
        }

        void invokeVoid(String metodo) {
            invocaciones.merge(metodo, 1, Integer::sum);
        }

        void verify(String metodo, int vecesEsperadas, String nombre) {
            int actual = invocaciones.getOrDefault(metodo, 0);
            if (actual == vecesEsperadas) {
                System.out.println("PASS: " + nombre);
            } else {
                System.out.println("FAIL: " + nombre + " — " + metodo + " llamado " + actual + " vez/veces, esperado " + vecesEsperadas);
            }
        }
    }

    record Producto(int id, String nombre, double precio) {}

    interface ProductoRepository {
        Producto findById(int id);
        void save(Producto p);
        void delete(int id);
    }

    static class ProductoService {
        private final ProductoRepository repo;

        ProductoService(ProductoRepository repo) { this.repo = repo; }

        Producto obtener(int id) { return repo.findById(id); }

        void crear(Producto p) { repo.save(p); }

        void borrar(int id) {
            Producto p = repo.findById(id);
            if (p != null) repo.delete(id);
        }
    }

    static class MockProductoRepository implements ProductoRepository {
        private final MockBuilder mock = new MockBuilder();

        void configurar(String metodo, Object valor) { mock.when(metodo, valor); }
        MockBuilder getMock() { return mock; }

        @Override public Producto findById(int id) { return mock.invoke("findById"); }
        @Override public void save(Producto p)      { mock.invokeVoid("save"); }
        @Override public void delete(int id)        { mock.invokeVoid("delete"); }
    }

    public static void main(String[] args) {
        Producto producto = new Producto(1, "Laptop", 999.99);

        MockProductoRepository mockRepo = new MockProductoRepository();
        mockRepo.configurar("findById", producto);

        ProductoService svc = new ProductoService(mockRepo);

        Producto resultado = svc.obtener(1);
        mockRepo.getMock().verify("findById", 1, "findById llamado una vez al obtener");

        svc.borrar(1);
        mockRepo.getMock().verify("findById", 2, "findById llamado de nuevo al borrar");
        mockRepo.getMock().verify("delete",   1, "delete llamado una vez al borrar");

        MockProductoRepository mockRepo2 = new MockProductoRepository();
        mockRepo2.configurar("findById", null);
        ProductoService svc2 = new ProductoService(mockRepo2);
        svc2.borrar(99);
        mockRepo2.getMock().verify("delete", 0, "delete no llamado si findById devuelve null");

        if (resultado != null && resultado.nombre().equals("Laptop")) {
            System.out.println("PASS: findById devuelve el producto correcto");
        } else {
            System.out.println("FAIL: findById devuelve el producto correcto");
        }
    }
}
