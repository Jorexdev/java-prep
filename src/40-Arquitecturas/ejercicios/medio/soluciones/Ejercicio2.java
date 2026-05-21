import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ejercicio2 {

    // --- Write model ---

    static class CuentaBancaria {
        final int id;
        final String titular;
        double saldo;

        CuentaBancaria(int id, String titular) {
            this.id = id;
            this.titular = titular;
            this.saldo = 0.0;
        }

        void depositar(double monto) {
            if (monto <= 0) throw new IllegalArgumentException("Monto debe ser positivo");
            saldo += monto;
        }

        void retirar(double monto) {
            if (monto > saldo) throw new IllegalStateException("Saldo insuficiente");
            saldo -= monto;
        }
    }

    // --- Read model ---

    static class CuentaResumen {
        final int id;
        final String titular;
        double saldo;
        int numOperaciones;

        CuentaResumen(int id, String titular) {
            this.id = id;
            this.titular = titular;
        }

        @Override
        public String toString() {
            return "CuentaResumen{id=" + id + ", titular='" + titular
                + "', saldo=" + saldo + ", numOperaciones=" + numOperaciones + "}";
        }
    }

    // --- Events ---

    interface DomainEvent {}
    record CuentaCreada(int id, String titular) implements DomainEvent {}
    record Depositado(int id, double monto) implements DomainEvent {}
    record Retirado(int id, double monto) implements DomainEvent {}

    // --- Commands ---

    interface Command {}
    record CrearCuenta(int id, String titular) implements Command {}
    record Depositar(int id, double monto) implements Command {}
    record Retirar(int id, double monto) implements Command {}

    // --- Queries ---

    interface Query {}
    record ObtenerResumen(int id) implements Query {}
    record ListarResumenes() implements Query {}

    // --- Stores ---

    static class WriteStore {
        final Map<Integer, CuentaBancaria> cuentas = new HashMap<>();
    }

    static class ReadStore {
        final Map<Integer, CuentaResumen> resumenes = new HashMap<>();
    }

    // --- Event handler que actualiza read model ---

    static class ResumenProjection {
        private final ReadStore readStore;

        ResumenProjection(ReadStore readStore) {
            this.readStore = readStore;
        }

        void on(DomainEvent event) {
            switch (event) {
                case CuentaCreada e -> {
                    CuentaResumen r = new CuentaResumen(e.id(), e.titular());
                    readStore.resumenes.put(e.id(), r);
                }
                case Depositado e -> {
                    CuentaResumen r = readStore.resumenes.get(e.id());
                    if (r != null) { r.saldo += e.monto(); r.numOperaciones++; }
                }
                case Retirado e -> {
                    CuentaResumen r = readStore.resumenes.get(e.id());
                    if (r != null) { r.saldo -= e.monto(); r.numOperaciones++; }
                }
                default -> {}
            }
        }
    }

    // --- Command bus ---

    static class CommandBus {
        private final WriteStore writeStore;
        private final ResumenProjection projection;

        CommandBus(WriteStore writeStore, ResumenProjection projection) {
            this.writeStore = writeStore;
            this.projection = projection;
        }

        void dispatch(Command cmd) {
            DomainEvent event = switch (cmd) {
                case CrearCuenta c -> {
                    CuentaBancaria cuenta = new CuentaBancaria(c.id(), c.titular());
                    writeStore.cuentas.put(c.id(), cuenta);
                    yield new CuentaCreada(c.id(), c.titular());
                }
                case Depositar c -> {
                    writeStore.cuentas.get(c.id()).depositar(c.monto());
                    yield new Depositado(c.id(), c.monto());
                }
                case Retirar c -> {
                    writeStore.cuentas.get(c.id()).retirar(c.monto());
                    yield new Retirado(c.id(), c.monto());
                }
                default -> throw new IllegalArgumentException("Unknown command");
            };
            projection.on(event);
        }
    }

    // --- Query bus ---

    static class QueryBus {
        private final ReadStore readStore;

        QueryBus(ReadStore readStore) {
            this.readStore = readStore;
        }

        Object query(Query q) {
            return switch (q) {
                case ObtenerResumen qr -> readStore.resumenes.get(qr.id());
                case ListarResumenes ignored -> new ArrayList<>(readStore.resumenes.values());
                default -> throw new IllegalArgumentException("Unknown query");
            };
        }
    }

    public static void main(String[] args) {
        WriteStore writeStore = new WriteStore();
        ReadStore readStore = new ReadStore();
        ResumenProjection projection = new ResumenProjection(readStore);
        CommandBus commandBus = new CommandBus(writeStore, projection);
        QueryBus queryBus = new QueryBus(readStore);

        System.out.println("--- Commands ---");
        commandBus.dispatch(new CrearCuenta(1, "Ana"));
        commandBus.dispatch(new Depositar(1, 500.0));
        commandBus.dispatch(new Depositar(1, 200.0));
        commandBus.dispatch(new Retirar(1, 100.0));

        System.out.println("\n--- Queries ---");
        CuentaResumen resumen = (CuentaResumen) queryBus.query(new ObtenerResumen(1));
        System.out.println(resumen);
        System.out.println("numOperaciones esperado=3, actual=" + resumen.numOperaciones);
    }
}
