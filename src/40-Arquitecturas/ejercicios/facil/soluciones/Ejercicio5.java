import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio5 {

    record CrearCuenta(String titular) {}
    record Depositar(int id, double monto) {}
    record Retirar(int id, double monto) {}
    record ObtenerSaldo(int id) {}
    record ListarMovimientos(int id) {}

    static class CuentaStore {
        final Map<Integer, Double> saldos = new HashMap<>();
        final Map<Integer, List<String>> movimientos = new HashMap<>();
        int nextId = 1;
    }

    static class CuentaCommandHandler {
        private final CuentaStore store;

        CuentaCommandHandler(CuentaStore store) {
            this.store = store;
        }

        int handle(CrearCuenta cmd) {
            int id = store.nextId++;
            store.saldos.put(id, 0.0);
            store.movimientos.put(id, new ArrayList<>());
            store.movimientos.get(id).add("Cuenta creada para " + cmd.titular());
            System.out.println("Cuenta " + id + " creada para " + cmd.titular());
            return id;
        }

        void handle(Depositar cmd) {
            if (cmd.monto() <= 0) throw new IllegalArgumentException("Monto debe ser positivo");
            store.saldos.merge(cmd.id(), cmd.monto(), Double::sum);
            store.movimientos.get(cmd.id()).add("Depósito: +" + cmd.monto());
            System.out.println("Depósito de " + cmd.monto() + " en cuenta " + cmd.id());
        }

        void handle(Retirar cmd) {
            double saldo = store.saldos.getOrDefault(cmd.id(), 0.0);
            if (cmd.monto() > saldo) throw new IllegalStateException("Saldo insuficiente");
            store.saldos.put(cmd.id(), saldo - cmd.monto());
            store.movimientos.get(cmd.id()).add("Retiro: -" + cmd.monto());
            System.out.println("Retiro de " + cmd.monto() + " de cuenta " + cmd.id());
        }
    }

    static class CuentaQueryHandler {
        private final CuentaStore store;

        CuentaQueryHandler(CuentaStore store) {
            this.store = store;
        }

        double handle(ObtenerSaldo query) {
            return store.saldos.getOrDefault(query.id(), 0.0);
        }

        List<String> handle(ListarMovimientos query) {
            return store.movimientos.getOrDefault(query.id(), List.of());
        }
    }

    public static void main(String[] args) {
        CuentaStore store = new CuentaStore();
        CuentaCommandHandler commands = new CuentaCommandHandler(store);
        CuentaQueryHandler queries = new CuentaQueryHandler(store);

        System.out.println("--- Commands ---");
        int id1 = commands.handle(new CrearCuenta("Ana"));
        commands.handle(new Depositar(id1, 500.0));
        commands.handle(new Retirar(id1, 150.0));

        System.out.println("\n--- Queries ---");
        double saldo = queries.handle(new ObtenerSaldo(id1));
        System.out.println("Saldo cuenta " + id1 + ": " + saldo);

        List<String> movs = queries.handle(new ListarMovimientos(id1));
        System.out.println("Movimientos cuenta " + id1 + ":");
        movs.forEach(m -> System.out.println("  " + m));
    }
}
