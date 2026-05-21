import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    // @Entity
    static class LineaPedido {
        // @Id
        int id;
        // @Column
        String producto;
        int cantidad;

        LineaPedido(int id, String producto, int cantidad) {
            this.id = id;
            this.producto = producto;
            this.cantidad = cantidad;
        }

        @Override
        public String toString() {
            return "  LineaPedido{id=" + id + ", producto='" + producto + "', cantidad=" + cantidad + "}";
        }
    }

    // @Entity
    static class Pedido {
        // @Id
        int id;
        // @Column
        String cliente;
        // @OneToMany(cascade = CascadeType.ALL)
        // @JoinColumn(name = "pedido_id")
        List<LineaPedido> lineas;

        Pedido(int id, String cliente, List<LineaPedido> lineas) {
            this.id = id;
            this.cliente = cliente;
            this.lineas = lineas;
        }

        @Override
        public String toString() {
            return "Pedido{id=" + id + ", cliente='" + cliente + "', lineas=" + lineas.size() + "}";
        }
    }

    static class PedidoRepository {
        private final Map<Integer, Pedido> store = new HashMap<>();

        void save(Pedido p) {
            store.put(p.id, p);
        }

        Pedido findById(int id) {
            return store.get(id);
        }
    }

    public static void main(String[] args) {

        List<LineaPedido> lineas = new ArrayList<>();
        lineas.add(new LineaPedido(1, "Portátil", 1));
        lineas.add(new LineaPedido(2, "Ratón", 2));
        lineas.add(new LineaPedido(3, "Alfombrilla", 1));

        Pedido pedido = new Pedido(100, "Carlos", lineas);

        PedidoRepository repo = new PedidoRepository();
        repo.save(pedido);

        Pedido recuperado = repo.findById(100);
        System.out.println(recuperado);
        recuperado.lineas.forEach(System.out::println);
    }
}
