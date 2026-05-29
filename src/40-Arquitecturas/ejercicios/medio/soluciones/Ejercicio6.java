import java.util.*;

public class Ejercicio6 {

    // --- Interfaces de Command y Query ---

    interface Command {}
    interface Query<R> {}

    @FunctionalInterface
    interface CommandHandler<C extends Command> {
        void handle(C command);
    }

    @FunctionalInterface
    interface QueryHandler<Q extends Query<R>, R> {
        R handle(Q query);
    }

    // --- EventBus ---

    @FunctionalInterface
    interface DomainEventHandler {
        void on(Object event);
    }

    static class EventBus {
        private final Map<Class<?>, List<DomainEventHandler>> subs = new HashMap<>();

        <T> void subscribe(Class<T> type, DomainEventHandler handler) {
            subs.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
        }

        void publish(Object event) {
            subs.getOrDefault(event.getClass(), List.of()).forEach(h -> h.on(event));
        }
    }

    // --- CommandBus ---

    static class CommandBus {
        private final Map<Class<?>, CommandHandler<?>> handlers = new HashMap<>();

        @SuppressWarnings("unchecked")
        <C extends Command> void register(Class<C> type, CommandHandler<C> handler) {
            handlers.put(type, handler);
        }

        @SuppressWarnings("unchecked")
        <C extends Command> void dispatch(C command) {
            CommandHandler<C> handler = (CommandHandler<C>) handlers.get(command.getClass());
            if (handler == null) throw new IllegalStateException("Sin handler para " + command.getClass().getSimpleName());
            handler.handle(command);
        }
    }

    // --- QueryBus ---

    static class QueryBus {
        private final Map<Class<?>, QueryHandler<?, ?>> handlers = new HashMap<>();

        @SuppressWarnings("unchecked")
        <Q extends Query<R>, R> void register(Class<Q> type, QueryHandler<Q, R> handler) {
            handlers.put(type, handler);
        }

        @SuppressWarnings("unchecked")
        <Q extends Query<R>, R> R query(Q query) {
            QueryHandler<Q, R> handler = (QueryHandler<Q, R>) handlers.get(query.getClass());
            if (handler == null) throw new IllegalStateException("Sin handler para " + query.getClass().getSimpleName());
            return handler.handle(query);
        }
    }

    // --- Commands ---

    record AbrirCuenta   (String cuentaId, String titular, double saldoInicial) implements Command {}
    record Depositar     (String cuentaId, double importe)                       implements Command {}
    record Retirar       (String cuentaId, double importe)                       implements Command {}

    // --- Queries ---

    record ObtenerSaldo   (String cuentaId) implements Query<Double>       {}
    record ObtenerHistorial(String cuentaId) implements Query<List<String>> {}

    // --- Domain Events ---

    record CuentaAbiertaEvt(String cuentaId, String titular, double saldo) {}
    record DepositadoEvt   (String cuentaId, double importe, double nuevoSaldo) {}
    record RetiradoEvt     (String cuentaId, double importe, double nuevoSaldo) {}

    // --- Write Model ---

    static class CuentaBancaria {
        final String id;
        final String titular;
        double saldo;

        CuentaBancaria(String id, String titular, double saldoInicial) {
            this.id      = id;
            this.titular = titular;
            this.saldo   = saldoInicial;
        }

        void depositar(double importe) {
            if (importe <= 0) throw new IllegalArgumentException("Importe debe ser positivo");
            saldo += importe;
        }

        void retirar(double importe) {
            if (importe <= 0) throw new IllegalArgumentException("Importe debe ser positivo");
            if (importe > saldo) throw new IllegalStateException("Saldo insuficiente");
            saldo -= importe;
        }
    }

    // --- Read Model ---

    static class CuentaResumen {
        String id;
        String titular;
        double saldo;
        int numOperaciones;
        List<String> historial = new ArrayList<>();

        @Override
        public String toString() {
            return "CuentaResumen{id=" + id + ", titular=" + titular
                + ", saldo=" + saldo + ", operaciones=" + numOperaciones + "}";
        }
    }

    // --- Application ---

    static class CuentaBancariaApp {
        private final Map<String, CuentaBancaria> writeModel = new HashMap<>();
        private final Map<String, CuentaResumen>  readModel  = new HashMap<>();
        private final EventBus   eventBus   = new EventBus();
        private final CommandBus commandBus = new CommandBus();
        private final QueryBus   queryBus   = new QueryBus();

        CuentaBancariaApp() {
            // --- Read Model Updater ---
            eventBus.subscribe(CuentaAbiertaEvt.class, ev -> {
                CuentaAbiertaEvt e = (CuentaAbiertaEvt) ev;
                CuentaResumen r = new CuentaResumen();
                r.id = e.cuentaId();
                r.titular = e.titular();
                r.saldo = e.saldo();
                r.numOperaciones = 1;
                r.historial.add("APERTURA: saldo inicial=" + e.saldo());
                readModel.put(e.cuentaId(), r);
            });

            eventBus.subscribe(DepositadoEvt.class, ev -> {
                DepositadoEvt e = (DepositadoEvt) ev;
                CuentaResumen r = readModel.get(e.cuentaId());
                if (r != null) {
                    r.saldo = e.nuevoSaldo();
                    r.numOperaciones++;
                    r.historial.add("DEPÓSITO: +" + e.importe() + " → saldo=" + e.nuevoSaldo());
                }
            });

            eventBus.subscribe(RetiradoEvt.class, ev -> {
                RetiradoEvt e = (RetiradoEvt) ev;
                CuentaResumen r = readModel.get(e.cuentaId());
                if (r != null) {
                    r.saldo = e.nuevoSaldo();
                    r.numOperaciones++;
                    r.historial.add("RETIRADA: -" + e.importe() + " → saldo=" + e.nuevoSaldo());
                }
            });

            // --- Command Handlers ---
            commandBus.register(AbrirCuenta.class, cmd -> {
                CuentaBancaria cuenta = new CuentaBancaria(cmd.cuentaId(), cmd.titular(), cmd.saldoInicial());
                writeModel.put(cmd.cuentaId(), cuenta);
                eventBus.publish(new CuentaAbiertaEvt(cmd.cuentaId(), cmd.titular(), cmd.saldoInicial()));
                System.out.println("[WriteModel] Cuenta abierta: " + cmd.cuentaId());
            });

            commandBus.register(Depositar.class, cmd -> {
                CuentaBancaria cuenta = getCuenta(cmd.cuentaId());
                cuenta.depositar(cmd.importe());
                eventBus.publish(new DepositadoEvt(cmd.cuentaId(), cmd.importe(), cuenta.saldo));
                System.out.println("[WriteModel] Depósito +" + cmd.importe() + " en " + cmd.cuentaId());
            });

            commandBus.register(Retirar.class, cmd -> {
                CuentaBancaria cuenta = getCuenta(cmd.cuentaId());
                cuenta.retirar(cmd.importe());
                eventBus.publish(new RetiradoEvt(cmd.cuentaId(), cmd.importe(), cuenta.saldo));
                System.out.println("[WriteModel] Retirada -" + cmd.importe() + " de " + cmd.cuentaId());
            });

            // --- Query Handlers ---
            queryBus.register(ObtenerSaldo.class,
                q -> readModel.getOrDefault(q.cuentaId(), new CuentaResumen()).saldo);

            queryBus.register(ObtenerHistorial.class,
                q -> readModel.getOrDefault(q.cuentaId(), new CuentaResumen()).historial);
        }

        private CuentaBancaria getCuenta(String id) {
            CuentaBancaria c = writeModel.get(id);
            if (c == null) throw new NoSuchElementException("Cuenta no encontrada: " + id);
            return c;
        }

        CommandBus commands() { return commandBus; }
        QueryBus   queries()  { return queryBus;   }
        Map<String, CuentaResumen> readModel() { return readModel; }
    }

    public static void main(String[] args) {
        CuentaBancariaApp app = new CuentaBancariaApp();

        System.out.println("=== Commands ===");
        app.commands().dispatch(new AbrirCuenta("CTA-001", "Ana García", 1_000.0));
        app.commands().dispatch(new Depositar("CTA-001", 500.0));
        app.commands().dispatch(new Depositar("CTA-001", 250.0));
        app.commands().dispatch(new Retirar  ("CTA-001", 300.0));
        app.commands().dispatch(new Depositar("CTA-001", 100.0));

        System.out.println("\n=== Queries ===");
        double saldo = app.queries().query(new ObtenerSaldo("CTA-001"));
        System.out.println("Saldo actual: " + saldo);

        List<String> historial = app.queries().query(new ObtenerHistorial("CTA-001"));
        System.out.println("Historial (" + historial.size() + " operaciones):");
        historial.forEach(h -> System.out.println("  " + h));

        System.out.println("\n=== Read Model ===");
        CuentaResumen resumen = app.readModel().get("CTA-001");
        System.out.println(resumen);

        // Verificación
        System.out.println("\n=== Verificación ===");
        double esperado = 1_000 + 500 + 250 - 300 + 100;
        System.out.printf("%s  saldo esperado=%.1f actual=%.1f%n",
            saldo == esperado ? "PASS" : "FAIL", esperado, saldo);
        System.out.printf("%s  numOperaciones esperado=5 actual=%d%n",
            resumen.numOperaciones == 5 ? "PASS" : "FAIL", resumen.numOperaciones);
    }
}
