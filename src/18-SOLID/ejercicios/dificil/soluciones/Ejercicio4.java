import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Ejercicio4 {

    static class Contenedor {
        private final Map<Class<?>, Supplier<?>> registro = new HashMap<>();

        <T> void registrar(Class<T> tipo, Supplier<T> factory) {
            registro.put(tipo, factory);
        }

        @SuppressWarnings("unchecked")
        <T> T resolver(Class<T> tipo) {
            Supplier<?> factory = registro.get(tipo);
            if (factory == null) throw new IllegalStateException("No registrado: " + tipo.getSimpleName());
            return (T) factory.get();
        }
    }

    interface ConexionBD {
        String query(String sql);
    }

    interface RepositorioPedidos2 {
        void guardar(String pedido);
    }

    static class ConexionH2 implements ConexionBD {
        @Override public String query(String sql) { return "H2 → " + sql; }
    }

    static class RepositorioSQL implements RepositorioPedidos2 {
        private final ConexionBD conexion;
        RepositorioSQL(ConexionBD conexion) { this.conexion = conexion; }
        @Override public void guardar(String pedido) {
            System.out.println(conexion.query("INSERT INTO pedidos VALUES ('" + pedido + "')"));
        }
    }

    static class ServicioPedidos2 {
        private final RepositorioPedidos2 repo;
        ServicioPedidos2(RepositorioPedidos2 repo) { this.repo = repo; }
        void crear(String pedido) { repo.guardar(pedido); System.out.println("Pedido creado: " + pedido); }
    }

    public static void main(String[] args) {
        Contenedor contenedor = new Contenedor();

        contenedor.registrar(ConexionBD.class,         ConexionH2::new);
        contenedor.registrar(RepositorioPedidos2.class, () -> new RepositorioSQL(contenedor.resolver(ConexionBD.class)));
        contenedor.registrar(ServicioPedidos2.class,    () -> new ServicioPedidos2(contenedor.resolver(RepositorioPedidos2.class)));

        ServicioPedidos2 servicio = contenedor.resolver(ServicioPedidos2.class);
        servicio.crear("Pedido #001");
        servicio.crear("Pedido #002");
    }
}
