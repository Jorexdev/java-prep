import java.time.Instant;
import java.util.*;

/**
 * Event Sourcing con Java puro.
 *
 * Concepto: el estado se deriva de una secuencia de eventos inmutables.
 * No se guarda "saldo=150", se guarda:
 *   [CuentaCreada, DepositoRealizado(200), RetiroRealizado(50)] → saldo=150
 *
 * Componentes:
 *  - Event: interfaz sellada de eventos de dominio
 *  - EventStore: log append-only de eventos
 *  - CuentaBancaria: aggregate que se reconstruye desde eventos
 *  - Proyector: construye una vista de lectura a partir de eventos
 */
public class ExpEventSourcing {

    // ═══════════════════════════════════════════════════════════════
    // EVENTOS DE DOMINIO — inmutables, en pasado
    // ═══════════════════════════════════════════════════════════════

    sealed interface Evento permits
            CuentaCreada, DepositoRealizado, RetiroRealizado, CuentaBloqueada {
        String cuentaId();
        Instant ocurridoEn();
    }

    record CuentaCreada(String cuentaId, String titular, Instant ocurridoEn) implements Evento {}

    record DepositoRealizado(String cuentaId, double cantidad, String concepto, Instant ocurridoEn)
            implements Evento {}

    record RetiroRealizado(String cuentaId, double cantidad, String concepto, Instant ocurridoEn)
            implements Evento {}

    record CuentaBloqueada(String cuentaId, String motivo, Instant ocurridoEn) implements Evento {}

    // ═══════════════════════════════════════════════════════════════
    // EVENT STORE — append-only, nunca se modifica
    // ═══════════════════════════════════════════════════════════════

    static class EventStore {
        // En producción sería una tabla con columnas: id, aggregateId, eventType, payload, version, timestamp
        private final List<Evento> log = new ArrayList<>();
        private int version = 0;

        void append(Evento evento) {
            log.add(evento);
            version++;
            System.out.printf("  [EventStore] v%d → %s%n", version, describir(evento));
        }

        List<Evento> cargarEventos(String cuentaId) {
            return log.stream()
                    .filter(e -> e.cuentaId().equals(cuentaId))
                    .toList();
        }

        List<Evento> cargarEventosHasta(String cuentaId, int maxVersion) {
            return log.stream()
                    .filter(e -> e.cuentaId().equals(cuentaId))
                    .limit(maxVersion)
                    .toList();
        }

        int version() { return version; }

        private String describir(Evento e) {
            return switch (e) {
                case CuentaCreada c     -> String.format("CuentaCreada{cuenta='%s', titular='%s'}", c.cuentaId(), c.titular());
                case DepositoRealizado d -> String.format("DepositoRealizado{cuenta='%s', +%.2f '%s'}", d.cuentaId(), d.cantidad(), d.concepto());
                case RetiroRealizado r   -> String.format("RetiroRealizado{cuenta='%s', -%.2f '%s'}", r.cuentaId(), r.cantidad(), r.concepto());
                case CuentaBloqueada b   -> String.format("CuentaBloqueada{cuenta='%s', motivo='%s'}", b.cuentaId(), b.motivo());
            };
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // AGGREGATE: CuentaBancaria
    // El estado se reconstruye aplicando eventos en orden
    // ═══════════════════════════════════════════════════════════════

    static class CuentaBancaria {
        private String cuentaId;
        private String titular;
        private double saldo;
        private boolean bloqueada;
        private int version;

        // Constructor privado — se crea siempre desde eventos
        private CuentaBancaria() {}

        // ── API de comandos — producen eventos ────────────────────────

        static List<Evento> crearCuenta(String cuentaId, String titular) {
            return List.of(new CuentaCreada(cuentaId, titular, Instant.now()));
        }

        List<Evento> depositar(double cantidad, String concepto) {
            if (bloqueada) throw new IllegalStateException("Cuenta bloqueada");
            if (cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser positiva");
            return List.of(new DepositoRealizado(cuentaId, cantidad, concepto, Instant.now()));
        }

        List<Evento> retirar(double cantidad, String concepto) {
            if (bloqueada) throw new IllegalStateException("Cuenta bloqueada");
            if (cantidad > saldo) throw new IllegalStateException("Saldo insuficiente");
            return List.of(new RetiroRealizado(cuentaId, cantidad, concepto, Instant.now()));
        }

        List<Evento> bloquear(String motivo) {
            return List.of(new CuentaBloqueada(cuentaId, motivo, Instant.now()));
        }

        // ── Reconstrucción desde eventos ──────────────────────────────

        static CuentaBancaria reconstituir(List<Evento> eventos) {
            CuentaBancaria cuenta = new CuentaBancaria();
            for (Evento evento : eventos) {
                cuenta.apply(evento);
            }
            return cuenta;
        }

        private void apply(Evento evento) {
            switch (evento) {
                case CuentaCreada e -> {
                    this.cuentaId = e.cuentaId();
                    this.titular = e.titular();
                    this.saldo = 0;
                    this.bloqueada = false;
                }
                case DepositoRealizado e -> this.saldo += e.cantidad();
                case RetiroRealizado e   -> this.saldo -= e.cantidad();
                case CuentaBloqueada e   -> this.bloqueada = true;
            }
            this.version++;
        }

        // ── Estado actual ──────────────────────────────────────────────

        String cuentaId()  { return cuentaId; }
        String titular()   { return titular; }
        double saldo()     { return saldo; }
        boolean bloqueada(){ return bloqueada; }
        int version()      { return version; }

        @Override
        public String toString() {
            return String.format("CuentaBancaria{id='%s', titular='%s', saldo=%.2f, bloqueada=%s, v=%d}",
                    cuentaId, titular, saldo, bloqueada, version);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PROYECTOR: construye una vista de lectura (CQRS)
    // Escucha eventos y mantiene una proyección desnormalizada
    // ═══════════════════════════════════════════════════════════════

    static class MovimientosProyector {
        private final List<String> movimientos = new ArrayList<>();

        void proyectar(List<Evento> eventos) {
            for (Evento e : eventos) {
                switch (e) {
                    case DepositoRealizado d ->
                        movimientos.add(String.format("+ %.2f€  %-20s  [%s]", d.cantidad(), d.concepto(), d.ocurridoEn()));
                    case RetiroRealizado r ->
                        movimientos.add(String.format("- %.2f€  %-20s  [%s]", r.cantidad(), r.concepto(), r.ocurridoEn()));
                    default -> {} // ignorar otros eventos en esta proyección
                }
            }
        }

        void mostrar() {
            System.out.println("  Movimientos:");
            if (movimientos.isEmpty()) {
                System.out.println("    (sin movimientos)");
            } else {
                movimientos.forEach(m -> System.out.println("    " + m));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // REPOSITORIO — guarda/carga eventos del EventStore
    // ═══════════════════════════════════════════════════════════════

    static class CuentaRepository {
        private final EventStore store;

        CuentaRepository(EventStore store) {
            this.store = store;
        }

        void guardar(List<Evento> nuevosEventos) {
            nuevosEventos.forEach(store::append);
        }

        CuentaBancaria cargar(String cuentaId) {
            List<Evento> eventos = store.cargarEventos(cuentaId);
            if (eventos.isEmpty()) throw new NoSuchElementException("Cuenta no encontrada: " + cuentaId);
            return CuentaBancaria.reconstituir(eventos);
        }

        CuentaBancaria cargarEnVersion(String cuentaId, int version) {
            List<Evento> eventos = store.cargarEventosHasta(cuentaId, version);
            return CuentaBancaria.reconstituir(eventos);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  EVENT SOURCING — Java puro");
        System.out.println("═".repeat(65));

        EventStore store = new EventStore();
        CuentaRepository repo = new CuentaRepository(store);

        // ── Fase 1: Crear cuenta y hacer operaciones ───────────────────
        System.out.println("\n─ FASE 1: Creando cuenta y operaciones ─");

        // Crear cuenta (genera eventos)
        repo.guardar(CuentaBancaria.crearCuenta("CUENTA-001", "Ana García"));

        // Cargar y depositar
        CuentaBancaria cuenta = repo.cargar("CUENTA-001");
        repo.guardar(cuenta.depositar(1000.00, "Nómina mayo"));

        cuenta = repo.cargar("CUENTA-001");
        repo.guardar(cuenta.depositar(200.00, "Freelance"));

        cuenta = repo.cargar("CUENTA-001");
        repo.guardar(cuenta.retirar(150.00, "Alquiler"));

        cuenta = repo.cargar("CUENTA-001");
        repo.guardar(cuenta.depositar(50.00, "Devolución"));

        cuenta = repo.cargar("CUENTA-001");
        repo.guardar(cuenta.retirar(80.00, "Supermercado"));

        // ── Fase 2: Estado actual (reconstruido desde eventos) ─────────
        System.out.println("\n─ FASE 2: Estado actual (reconstituido desde eventos) ─");
        CuentaBancaria estadoActual = repo.cargar("CUENTA-001");
        System.out.println("  " + estadoActual);
        System.out.printf("  Saldo esperado: 1000 + 200 - 150 + 50 - 80 = 1020.00€%n");

        // ── Fase 3: Temporal query — estado en un punto del pasado ─────
        System.out.println("\n─ FASE 3: Temporal query — estado tras los primeros 2 eventos ─");
        CuentaBancaria estadoV2 = repo.cargarEnVersion("CUENTA-001", 2);
        System.out.println("  " + estadoV2);
        System.out.printf("  Saldo esperado tras crear + depositar 1000€: 1000.00€%n");

        CuentaBancaria estadoV3 = repo.cargarEnVersion("CUENTA-001", 3);
        System.out.println("  Estado tras 3 eventos: " + estadoV3);

        // ── Fase 4: Proyección de movimientos (CQRS) ───────────────────
        System.out.println("\n─ FASE 4: Proyección de movimientos (vista de lectura) ─");
        MovimientosProyector proyector = new MovimientosProyector();
        proyector.proyectar(store.cargarEventos("CUENTA-001"));
        proyector.mostrar();

        // ── Fase 5: Bloqueo de cuenta + saldo insuficiente ────────────
        System.out.println("\n─ FASE 5: Reglas de negocio en el aggregate ─");
        CuentaBancaria cuentaActual = repo.cargar("CUENTA-001");

        try {
            repo.guardar(cuentaActual.retirar(5000.00, "Retiro imposible"));
        } catch (IllegalStateException e) {
            System.out.println("  Error esperado: " + e.getMessage());
        }

        repo.guardar(cuentaActual.bloquear("Actividad sospechosa"));
        CuentaBancaria cuentaBloqueada = repo.cargar("CUENTA-001");

        try {
            repo.guardar(cuentaBloqueada.depositar(100.00, "Intento de depósito"));
        } catch (IllegalStateException e) {
            System.out.println("  Error esperado: " + e.getMessage());
        }

        // ── Resumen ────────────────────────────────────────────────────
        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN EVENT SOURCING");
        System.out.println("═".repeat(65));
        System.out.printf("  Total eventos en el store: %d%n", store.version());
        System.out.println("  Estado actual: " + repo.cargar("CUENTA-001"));
        System.out.println();
        System.out.println("  Ventajas demostradas:");
        System.out.println("  ✓ Audit trail completo (fase 4: todos los movimientos)");
        System.out.println("  ✓ Temporal queries (fase 3: estado en cualquier punto)");
        System.out.println("  ✓ Reglas de negocio en el aggregate (fase 5)");
        System.out.println("  ✓ Proyecciones independientes para lectura (fase 4)");
        System.out.println("═".repeat(65));
    }
}
