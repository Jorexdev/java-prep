import java.util.*;

public class Ejercicio4 {

    record Pedido(int id, String producto, int cantidad) {}

    interface RepositorioPedidos {
        void guardar(Pedido p);
        Optional<Pedido> buscar(int id);
        List<Pedido> todos();
    }

    static class RepositorioMemoria implements RepositorioPedidos {
        private final Map<Integer, Pedido> store = new HashMap<>();
        @Override public void guardar(Pedido p)           { store.put(p.id(), p); }
        @Override public Optional<Pedido> buscar(int id)  { return Optional.ofNullable(store.get(id)); }
        @Override public List<Pedido> todos()             { return new ArrayList<>(store.values()); }
    }

    static class RepositorioArchivo implements RepositorioPedidos {
        private final List<Pedido> store = new ArrayList<>();
        @Override public void guardar(Pedido p) {
            store.add(p);
            System.out.println("[Archivo] Persistido: " + p);
        }
        @Override public Optional<Pedido> buscar(int id) {
            return store.stream().filter(p -> p.id() == id).findFirst();
        }
        @Override public List<Pedido> todos() { return Collections.unmodifiableList(store); }
    }

    static class ServicioPedidos {
        private final RepositorioPedidos repo;
        ServicioPedidos(RepositorioPedidos repo) { this.repo = repo; }

        void crear(Pedido p)      { repo.guardar(p); System.out.println("Pedido creado: " + p.id()); }
        void mostrarTodos()       { repo.todos().forEach(p -> System.out.println("  " + p)); }
        void buscar(int id)       { System.out.println("Encontrado: " + repo.buscar(id).orElse(null)); }
    }

    public static void main(String[] args) {
        for (RepositorioPedidos repo : List.of(new RepositorioMemoria(), new RepositorioArchivo())) {
            System.out.println("\n--- " + repo.getClass().getSimpleName() + " ---");
            ServicioPedidos srv = new ServicioPedidos(repo);
            srv.crear(new Pedido(1, "Laptop", 2));
            srv.crear(new Pedido(2, "Teclado", 5));
            srv.buscar(1);
            srv.mostrarTodos();
        }
    }
}
