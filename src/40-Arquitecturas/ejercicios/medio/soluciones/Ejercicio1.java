import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ejercicio1 {

    // --- Dominio ---

    static class Cuenta {
        final int id;
        final String titular;
        double saldo;

        Cuenta(int id, String titular, double saldo) {
            this.id = id;
            this.titular = titular;
            this.saldo = saldo;
        }

        @Override
        public String toString() {
            return "Cuenta{id=" + id + ", titular='" + titular + "', saldo=" + saldo + "}";
        }
    }

    interface CuentaRepository {
        void save(Cuenta c);
        Optional<Cuenta> findById(int id);
    }

    interface TransferirUseCase {
        void transfer(int fromId, int toId, double amount);
    }

    static class TransferirService implements TransferirUseCase {
        private final CuentaRepository repository;

        TransferirService(CuentaRepository repository) {
            this.repository = repository;
        }

        @Override
        public void transfer(int fromId, int toId, double amount) {
            Cuenta from = repository.findById(fromId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no encontrada: " + fromId));
            Cuenta to = repository.findById(toId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada: " + toId));

            if (from.saldo < amount) throw new IllegalStateException("Saldo insuficiente");
            from.saldo -= amount;
            to.saldo += amount;

            repository.save(from);
            repository.save(to);
        }
    }

    // --- Adaptadores de salida ---

    static class MemoriaCuentaRepository implements CuentaRepository {
        private final Map<Integer, Cuenta> store = new HashMap<>();

        @Override
        public void save(Cuenta c) {
            store.put(c.id, c);
        }

        @Override
        public Optional<Cuenta> findById(int id) {
            return Optional.ofNullable(store.get(id));
        }
    }

    static class LoggingCuentaRepository implements CuentaRepository {
        private final CuentaRepository delegate;

        LoggingCuentaRepository(CuentaRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public void save(Cuenta c) {
            System.out.println("[LOG] save: " + c);
            delegate.save(c);
        }

        @Override
        public Optional<Cuenta> findById(int id) {
            Optional<Cuenta> result = delegate.findById(id);
            System.out.println("[LOG] findById(" + id + ") → " + result);
            return result;
        }
    }

    // --- Adaptadores de entrada ---

    static class ApiController {
        private final TransferirUseCase useCase;

        ApiController(TransferirUseCase useCase) {
            this.useCase = useCase;
        }

        void transfer(int from, int to, double amount) {
            System.out.println("[HTTP] POST /transfer from=" + from + " to=" + to + " amount=" + amount);
            useCase.transfer(from, to, amount);
            System.out.println("[HTTP] 200 OK");
        }
    }

    static class BatchProcessor {
        private final TransferirUseCase useCase;

        BatchProcessor(TransferirUseCase useCase) {
            this.useCase = useCase;
        }

        void process(List<String> instructions) {
            for (String line : instructions) {
                String[] parts = line.split(":");
                int from = Integer.parseInt(parts[0]);
                int to = Integer.parseInt(parts[1]);
                double amount = Double.parseDouble(parts[2]);
                System.out.println("[BATCH] " + line);
                useCase.transfer(from, to, amount);
            }
        }
    }

    // --- main ---

    public static void main(String[] args) {
        System.out.println("=== Con MemoriaCuentaRepository ===");
        MemoriaCuentaRepository memRepo = new MemoriaCuentaRepository();
        memRepo.save(new Cuenta(1, "Ana", 1000.0));
        memRepo.save(new Cuenta(2, "Luis", 500.0));
        memRepo.save(new Cuenta(3, "Marta", 200.0));

        TransferirService service = new TransferirService(memRepo);
        ApiController api = new ApiController(service);
        BatchProcessor batch = new BatchProcessor(service);

        api.transfer(1, 2, 300.0);
        System.out.println("Saldo Ana: " + memRepo.findById(1).get().saldo);
        System.out.println("Saldo Luis: " + memRepo.findById(2).get().saldo);

        batch.process(List.of("2:3:100", "1:3:50"));
        System.out.println("Saldo Marta: " + memRepo.findById(3).get().saldo);

        System.out.println("\n=== Con LoggingCuentaRepository ===");
        MemoriaCuentaRepository base = new MemoriaCuentaRepository();
        base.save(new Cuenta(1, "Ana", 1000.0));
        base.save(new Cuenta(2, "Luis", 500.0));
        base.save(new Cuenta(3, "Marta", 200.0));

        LoggingCuentaRepository loggingRepo = new LoggingCuentaRepository(base);
        TransferirService loggingService = new TransferirService(loggingRepo);
        ApiController apiLogging = new ApiController(loggingService);

        apiLogging.transfer(1, 2, 200.0);
    }
}
