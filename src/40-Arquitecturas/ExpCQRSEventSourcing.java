import java.util.*;

/**
 * Simulación de CQRS + Event Sourcing combinados con Java puro.
 *
 * Conceptos demostrados:
 *  - EventStore: log append-only, origen de verdad del estado
 *  - Replay: reconstruir el aggregate aplicando eventos en orden
 *  - Snapshot: optimización cada N eventos para evitar replays largos
 *  - Projection: suscriptor que construye el read model (lado Q de CQRS)
 *  - El write model (aggregate) y el read model (projection) son independientes
 */
public class ExpCQRSEventSourcing {

    // ═══════════════════════════════════════════════════════════════
    // EVENTOS DE DOMINIO
    // ═══════════════════════════════════════════════════════════════

    sealed interface AccountEvent permits AccountOpened, MoneyDeposited, MoneyWithdrawn, AccountClosed {}

    record AccountOpened(String accountId, String owner, long timestamp) implements AccountEvent {}
    record MoneyDeposited(String accountId, double amount, String description, long timestamp) implements AccountEvent {}
    record MoneyWithdrawn(String accountId, double amount, String description, long timestamp) implements AccountEvent {}
    record AccountClosed(String accountId, String reason, long timestamp) implements AccountEvent {}

    // ═══════════════════════════════════════════════════════════════
    // SNAPSHOT: estado guardado cada N eventos para acelerar replay
    // ═══════════════════════════════════════════════════════════════

    record Snapshot(String accountId, double balance, boolean closed,
                    int eventsApplied, long takenAt) {
        @Override
        public String toString() {
            return String.format("Snapshot{account='%s', balance=%.2f, events=%d}",
                    accountId, balance, eventsApplied);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EVENT STORE — append-only, inmutable
    // ═══════════════════════════════════════════════════════════════

    static class EventStore {
        // aggregateId → lista de eventos en orden de inserción
        private final Map<String, List<AccountEvent>> events = new LinkedHashMap<>();
        // aggregateId → último snapshot guardado
        private final Map<String, Snapshot> snapshots = new LinkedHashMap<>();

        private int totalEvents = 0;

        void append(AccountEvent event) {
            events.computeIfAbsent(aggregateId(event), k -> new ArrayList<>()).add(event);
            totalEvents++;
            System.out.printf("  [EventStore] #%d → %s{account='%s'}%n",
                    totalEvents, event.getClass().getSimpleName(), aggregateId(event));
        }

        List<AccountEvent> loadEvents(String accountId) {
            return events.getOrDefault(accountId, Collections.emptyList());
        }

        // Cargar eventos desde un número de versión dado (para usar junto con snapshot)
        List<AccountEvent> loadEventsSince(String accountId, int fromIndex) {
            List<AccountEvent> all = loadEvents(accountId);
            return fromIndex < all.size() ? all.subList(fromIndex, all.size()) : Collections.emptyList();
        }

        void saveSnapshot(Snapshot snapshot) {
            snapshots.put(snapshot.accountId(), snapshot);
            System.out.printf("  [EventStore] Snapshot guardado: %s%n", snapshot);
        }

        Optional<Snapshot> loadSnapshot(String accountId) {
            return Optional.ofNullable(snapshots.get(accountId));
        }

        int totalEvents() { return totalEvents; }
        int totalEventsFor(String accountId) {
            return events.getOrDefault(accountId, Collections.emptyList()).size();
        }

        private String aggregateId(AccountEvent e) {
            return switch (e) {
                case AccountOpened  ev -> ev.accountId();
                case MoneyDeposited ev -> ev.accountId();
                case MoneyWithdrawn ev -> ev.accountId();
                case AccountClosed  ev -> ev.accountId();
            };
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // AGGREGATE: BankAccount — estado derivado de eventos
    // ═══════════════════════════════════════════════════════════════

    static class BankAccount {
        private String accountId;
        private String owner;
        private double balance = 0;
        private boolean closed = false;
        private int version = 0; // número de eventos aplicados

        private BankAccount() {}

        // ── Comandos — producen eventos sin aplicarlos aún ────────────

        static List<AccountEvent> open(String accountId, String owner) {
            return List.of(new AccountOpened(accountId, owner, System.currentTimeMillis()));
        }

        List<AccountEvent> deposit(double amount, String description) {
            if (closed) throw new IllegalStateException("Cuenta cerrada");
            if (amount <= 0) throw new IllegalArgumentException("El importe debe ser positivo");
            return List.of(new MoneyDeposited(accountId, amount, description, System.currentTimeMillis()));
        }

        List<AccountEvent> withdraw(double amount, String description) {
            if (closed) throw new IllegalStateException("Cuenta cerrada");
            if (amount > balance) throw new IllegalStateException("Saldo insuficiente: " + balance);
            return List.of(new MoneyWithdrawn(accountId, amount, description, System.currentTimeMillis()));
        }

        List<AccountEvent> close(String reason) {
            if (closed) throw new IllegalStateException("La cuenta ya está cerrada");
            return List.of(new AccountClosed(accountId, reason, System.currentTimeMillis()));
        }

        // ── Reconstrucción desde EventStore ───────────────────────────

        // Reconstruir sin snapshot: replay completo desde el inicio
        static BankAccount reconstitute(List<AccountEvent> events) {
            BankAccount acc = new BankAccount();
            events.forEach(acc::apply);
            return acc;
        }

        // Reconstruir con snapshot: empezar desde el snapshot y aplicar solo los eventos nuevos
        static BankAccount reconstituteFromSnapshot(Snapshot snapshot, List<AccountEvent> newEvents) {
            BankAccount acc = new BankAccount();
            // Restaurar estado del snapshot
            acc.accountId = snapshot.accountId();
            acc.balance = snapshot.balance();
            acc.closed = snapshot.closed();
            acc.version = snapshot.eventsApplied();
            System.out.printf("  [BankAccount] Cargando desde snapshot (v%d) + %d eventos nuevos%n",
                    snapshot.eventsApplied(), newEvents.size());
            // Aplicar solo los eventos posteriores al snapshot
            newEvents.forEach(acc::apply);
            return acc;
        }

        private void apply(AccountEvent event) {
            switch (event) {
                case AccountOpened  e -> { accountId = e.accountId(); owner = e.owner(); balance = 0; }
                case MoneyDeposited e -> balance += e.amount();
                case MoneyWithdrawn e -> balance -= e.amount();
                case AccountClosed  e -> closed = true;
            }
            version++;
        }

        String accountId() { return accountId; }
        String owner()     { return owner; }
        double balance()   { return balance; }
        boolean closed()   { return closed; }
        int version()      { return version; }

        @Override
        public String toString() {
            return String.format("BankAccount{id='%s', owner='%s', balance=%.2f, closed=%b, v=%d}",
                    accountId, owner, balance, closed, version);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // REPOSITORIO — usa snapshots si están disponibles
    // ═══════════════════════════════════════════════════════════════

    static class BankAccountRepository {
        private final EventStore store;
        private static final int SNAPSHOT_INTERVAL = 3; // snapshot cada 3 eventos

        BankAccountRepository(EventStore store) {
            this.store = store;
        }

        void save(String accountId, List<AccountEvent> newEvents) {
            newEvents.forEach(store::append);

            // Tomar snapshot si corresponde
            int totalEvents = store.totalEventsFor(accountId);
            if (totalEvents % SNAPSHOT_INTERVAL == 0) {
                BankAccount current = load(accountId);
                Snapshot snap = new Snapshot(accountId, current.balance(), current.closed(),
                        current.version(), System.currentTimeMillis());
                store.saveSnapshot(snap);
            }
        }

        BankAccount load(String accountId) {
            Optional<Snapshot> snap = store.loadSnapshot(accountId);
            if (snap.isPresent()) {
                // Optimización: cargar desde snapshot + solo los eventos nuevos
                List<AccountEvent> newEvents = store.loadEventsSince(accountId, snap.get().eventsApplied());
                return BankAccount.reconstituteFromSnapshot(snap.get(), newEvents);
            }
            // Sin snapshot: replay completo
            List<AccountEvent> allEvents = store.loadEvents(accountId);
            System.out.printf("  [Repository] Replay completo de %d eventos para '%s'%n",
                    allEvents.size(), accountId);
            return BankAccount.reconstitute(allEvents);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PROJECTION — construye el read model (lado Q de CQRS)
    // ═══════════════════════════════════════════════════════════════

    record AccountSummary(String accountId, String owner, double balance,
                          int totalTransactions, double totalDeposited, double totalWithdrawn) {}

    static class AccountProjection {
        private final Map<String, AccountSummary> readModel = new LinkedHashMap<>();

        // Proyectar todos los eventos de una cuenta al read model
        void project(List<AccountEvent> events) {
            String accountId = null;
            String owner = null;
            double balance = 0, deposited = 0, withdrawn = 0;
            int txCount = 0;

            for (AccountEvent e : events) {
                switch (e) {
                    case AccountOpened  ev -> { accountId = ev.accountId(); owner = ev.owner(); }
                    case MoneyDeposited ev -> { balance += ev.amount(); deposited += ev.amount(); txCount++; }
                    case MoneyWithdrawn ev -> { balance -= ev.amount(); withdrawn += ev.amount(); txCount++; }
                    case AccountClosed  ev -> { /* no afecta al balance */ }
                }
            }

            if (accountId != null) {
                readModel.put(accountId,
                        new AccountSummary(accountId, owner, balance, txCount, deposited, withdrawn));
            }
        }

        Optional<AccountSummary> findById(String accountId) {
            return Optional.ofNullable(readModel.get(accountId));
        }

        void mostrar(String accountId) {
            findById(accountId).ifPresentOrElse(
                    s -> System.out.printf(
                            "  AccountSummary{id='%s', owner='%s', balance=%.2f, tx=%d, dep=%.2f, wd=%.2f}%n",
                            s.accountId(), s.owner(), s.balance(), s.totalTransactions(),
                            s.totalDeposited(), s.totalWithdrawn()),
                    () -> System.out.println("  (no encontrado en read model)"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  CQRS + EVENT SOURCING — Java puro");
        System.out.println("═".repeat(65));

        EventStore store = new EventStore();
        BankAccountRepository repo = new BankAccountRepository(store);
        AccountProjection projection = new AccountProjection();

        // ── Fase 1: crear cuenta y operaciones ────────────────────────
        System.out.println("\n─ FASE 1: Operaciones sobre la cuenta ─");

        repo.save("ACC-001", BankAccount.open("ACC-001", "Ana García"));

        BankAccount acc = repo.load("ACC-001");
        repo.save("ACC-001", acc.deposit(1000.0, "Nómina mayo"));

        acc = repo.load("ACC-001");
        repo.save("ACC-001", acc.deposit(500.0, "Freelance"));

        // En este punto hay 3 eventos → se toma snapshot automáticamente
        acc = repo.load("ACC-001");
        repo.save("ACC-001", acc.withdraw(200.0, "Alquiler"));

        acc = repo.load("ACC-001");
        repo.save("ACC-001", acc.deposit(100.0, "Devolución"));

        acc = repo.load("ACC-001");
        repo.save("ACC-001", acc.withdraw(50.0, "Supermercado"));

        // ── Fase 2: reconstruir estado actual ─────────────────────────
        System.out.println("\n─ FASE 2: Estado actual (reconstituido) ─");
        BankAccount current = repo.load("ACC-001");
        System.out.println("  " + current);
        System.out.printf("  Saldo esperado: 1000 + 500 - 200 + 100 - 50 = 1350.00%n");

        // ── Fase 3: snapshot shortcut ─────────────────────────────────
        System.out.println("\n─ FASE 3: Snapshot shortcut (snapshot cada " + 3 + " eventos) ─");
        store.loadSnapshot("ACC-001").ifPresent(s ->
            System.out.printf("  Snapshot disponible: %s%n  → El replay solo necesita los eventos posteriores al snapshot%n", s));

        // ── Fase 4: temporal query — estado pasado ────────────────────
        System.out.println("\n─ FASE 4: Temporal query — estado tras los primeros 2 eventos ─");
        List<AccountEvent> first2 = store.loadEvents("ACC-001").subList(0, 2);
        BankAccount past = BankAccount.reconstitute(first2);
        System.out.printf("  Estado tras 2 eventos: balance=%.2f (solo apertura + depósito 1000€)%n",
                past.balance());

        // ── Fase 5: projection (read model CQRS) ─────────────────────
        System.out.println("\n─ FASE 5: Projection (read model para queries) ─");
        projection.project(store.loadEvents("ACC-001"));
        projection.mostrar("ACC-001");

        // ── Fase 6: intento de retirada con saldo insuficiente ─────────
        System.out.println("\n─ FASE 6: Invariante — saldo insuficiente ─");
        BankAccount accFinal = repo.load("ACC-001");
        try {
            accFinal.withdraw(9999.0, "retiro imposible");
        } catch (IllegalStateException e) {
            System.out.printf("  ✓ Invariante aplicada: %s%n", e.getMessage());
        }

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN CQRS + EVENT SOURCING");
        System.out.println("═".repeat(65));
        System.out.printf("  Total eventos en el store: %d%n", store.totalEvents());
        System.out.println("  EventStore: append-only, fuente de verdad inmutable");
        System.out.println("  Replay: cualquier estado pasado es reconstruible");
        System.out.println("  Snapshot: optimización para evitar replays largos (cada N eventos)");
        System.out.println("  Projection: read model independiente y optimizado para queries");
        System.out.println("  CQRS: comandos al aggregate (write), queries al read model");
        System.out.println("═".repeat(65));
    }
}
