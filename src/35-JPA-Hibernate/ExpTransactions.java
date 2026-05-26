import java.util.ArrayDeque;
import java.util.Deque;

// Simula @Transactional con distintos modos de propagación.
// TransactionManager gestiona begin/commit/rollback y el contexto activo.
// Las propagaciones REQUIRED, REQUIRES_NEW y NESTED se aplican al invocar servicios anidados.

// ── TransactionStatus ─────────────────────────────────────────────────────────

class TransactionStatus {
    final String name;
    boolean markedForRollback = false;

    TransactionStatus(String name) { this.name = name; }

    @Override
    public String toString() {
        return "Tx[" + name + (markedForRollback ? ",ROLLBACK_ONLY" : "") + "]";
    }
}

// ── TransactionManager ────────────────────────────────────────────────────────

// Equivale a PlatformTransactionManager de Spring
class TransactionManager {

    // Pila de transacciones activas (la del tope es la actual)
    private final Deque<TransactionStatus> stack = new ArrayDeque<>();
    private int txCounter = 0;

    public TransactionStatus getCurrent() {
        return stack.isEmpty() ? null : stack.peek();
    }

    public TransactionStatus begin(String name) {
        TransactionStatus tx = new TransactionStatus(name + "#" + (++txCounter));
        stack.push(tx);
        System.out.println("    [TxManager] BEGIN  " + tx);
        return tx;
    }

    public void commit(TransactionStatus tx) {
        if (stack.isEmpty() || stack.peek() != tx) {
            throw new IllegalStateException("La transacción " + tx + " no está en el tope de la pila");
        }
        if (tx.markedForRollback) {
            rollback(tx);
            return;
        }
        stack.pop();
        System.out.println("    [TxManager] COMMIT " + tx);
    }

    public void rollback(TransactionStatus tx) {
        while (!stack.isEmpty() && stack.peek() != tx) {
            System.out.println("    [TxManager] ROLLBACK " + stack.pop() + " (cascada)");
        }
        if (!stack.isEmpty()) stack.pop();
        System.out.println("    [TxManager] ROLLBACK " + tx);
    }
}

// ── Propagaciones ─────────────────────────────────────────────────────────────

enum Propagation {
    REQUIRED,       // une a tx existente; si no hay, crea una nueva
    REQUIRES_NEW,   // siempre crea una nueva tx (suspende la actual)
    NESTED          // savepoint dentro de la tx existente; si no hay, crea una
}

// ── Métodos de soporte para simular la semántica de cada propagación ──────────

class TxSupport {

    static TransactionStatus getOrCreate(TransactionManager tm, String name, Propagation prop) {
        TransactionStatus current = tm.getCurrent();
        switch (prop) {
            case REQUIRED -> {
                if (current != null) {
                    System.out.println("    [REQUIRED] Uniendo a tx existente: " + current);
                    return current;    // participa en la tx padre
                }
                return tm.begin(name);
            }
            case REQUIRES_NEW -> {
                if (current != null) {
                    System.out.println("    [REQUIRES_NEW] Suspendiendo " + current + " — nueva tx:");
                }
                return tm.begin(name + "-NEW");
            }
            case NESTED -> {
                if (current != null) {
                    // Savepoint: en este simulador simplemente creamos una sub-tx
                    System.out.println("    [NESTED] Savepoint dentro de " + current + ":");
                    return tm.begin(name + "-NESTED");
                }
                return tm.begin(name);
            }
            default -> throw new IllegalArgumentException("Propagación no soportada");
        }
    }

    // Commit solo si la tx nos pertenece (no si solo nos unimos a una existente)
    static void endMethod(TransactionManager tm, TransactionStatus ownTx,
                          TransactionStatus callerTx, boolean forceRollback) {
        boolean isOwner = (ownTx != callerTx); // true si se creó una nueva tx
        if (forceRollback) {
            ownTx.markedForRollback = true;
        }
        if (isOwner) {
            if (forceRollback) {
                tm.rollback(ownTx);
            } else {
                tm.commit(ownTx);
            }
        } else {
            if (forceRollback) {
                System.out.println("    → Tx marcada para rollback (se propagará al commit del padre)");
            }
        }
    }
}

// ── Servicios de ejemplo ──────────────────────────────────────────────────────

// @Service
class NotificacionService {

    private final TransactionManager tm;

    NotificacionService(TransactionManager tm) { this.tm = tm; }

    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    void enviar(String mensaje, boolean fallar) {
        TransactionStatus callerTx = tm.getCurrent();
        TransactionStatus ownTx    = TxSupport.getOrCreate(tm, "NotificacionTx", Propagation.REQUIRES_NEW);
        System.out.println("    NotificacionService.enviar() — " + mensaje);
        if (fallar) {
            System.out.println("    ERROR en NotificacionService — rollback solo de esta tx");
            TxSupport.endMethod(tm, ownTx, callerTx, true);
            throw new RuntimeException("Fallo al enviar notificación");
        }
        TxSupport.endMethod(tm, ownTx, callerTx, false);
    }
}

// @Service
class AuditoriaService {

    private final TransactionManager tm;

    AuditoriaService(TransactionManager tm) { this.tm = tm; }

    // @Transactional(propagation = Propagation.NESTED)
    void registrar(String evento) {
        TransactionStatus callerTx = tm.getCurrent();
        TransactionStatus ownTx    = TxSupport.getOrCreate(tm, "AuditoriaTx", Propagation.NESTED);
        System.out.println("    AuditoriaService.registrar() — " + evento);
        TxSupport.endMethod(tm, ownTx, callerTx, false);
    }
}

// @Service
class PedidoService {

    private final TransactionManager    tm;
    private final NotificacionService   notiSvc;
    private final AuditoriaService      auditSvc;

    PedidoService(TransactionManager tm, NotificacionService notiSvc, AuditoriaService auditSvc) {
        this.tm       = tm;
        this.notiSvc  = notiSvc;
        this.auditSvc = auditSvc;
    }

    // @Transactional(propagation = Propagation.REQUIRED)
    void crearPedido(String descripcion, boolean notificacionFalla) {
        TransactionStatus callerTx = tm.getCurrent();    // null si no hay tx exterior
        TransactionStatus ownTx    = TxSupport.getOrCreate(tm, "PedidoTx", Propagation.REQUIRED);

        try {
            System.out.println("    PedidoService.crearPedido() — guardando: " + descripcion);
            auditSvc.registrar("PEDIDO_CREADO: " + descripcion);        // NESTED
            notiSvc.enviar("Nuevo pedido: " + descripcion, notificacionFalla); // REQUIRES_NEW
            TxSupport.endMethod(tm, ownTx, callerTx, false);

        } catch (RuntimeException ex) {
            System.out.println("    PedidoService captura excepción: " + ex.getMessage());
            // Con REQUIRES_NEW, el fallo de notificación NO obliga a rollback del pedido
            // si elegimos manejar la excepción aquí
            System.out.println("    → Pedido guardado igualmente (notificación falló en tx independiente)");
            TxSupport.endMethod(tm, ownTx, callerTx, false);
        }
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpTransactions {
    public static void main(String[] args) {

        System.out.println("=== Simulación @Transactional — Propagaciones ===\n");

        // ─── Escenario 1: flujo feliz ─────────────────────────────────────────
        System.out.println("[ Escenario 1: flujo feliz — todo commit ]");
        {
            TransactionManager   tm       = new TransactionManager();
            NotificacionService  notiSvc  = new NotificacionService(tm);
            AuditoriaService     auditSvc = new AuditoriaService(tm);
            PedidoService        pedSvc   = new PedidoService(tm, notiSvc, auditSvc);

            pedSvc.crearPedido("Laptop Pro 16\"", false);
        }

        System.out.println("\n" + "─".repeat(60) + "\n");

        // ─── Escenario 2: notificación falla (REQUIRES_NEW) ───────────────────
        System.out.println("[ Escenario 2: REQUIRES_NEW — notificación falla ]");
        System.out.println("  La tx de Notificación hace rollback pero el Pedido se salva.");
        {
            TransactionManager   tm       = new TransactionManager();
            NotificacionService  notiSvc  = new NotificacionService(tm);
            AuditoriaService     auditSvc = new AuditoriaService(tm);
            PedidoService        pedSvc   = new PedidoService(tm, notiSvc, auditSvc);

            pedSvc.crearPedido("Teclado Mecánico", true);
        }

        System.out.println("\n" + "─".repeat(60) + "\n");

        // ─── Escenario 3: comparativa de propagaciones ───────────────────────
        System.out.println("[ Escenario 3: comparativa de propagaciones ]");
        TransactionManager tm = new TransactionManager();

        System.out.println("\n  → REQUIRED (outer ya existe):");
        TransactionStatus outer = tm.begin("Outer");
        TransactionStatus inner = TxSupport.getOrCreate(tm, "Inner", Propagation.REQUIRED);
        System.out.println("    outer == inner: " + (outer == inner) + " (comparten la misma tx)");
        tm.commit(outer);

        System.out.println("\n  → REQUIRES_NEW (outer existe, se suspende):");
        TransactionManager tm2 = new TransactionManager();
        TransactionStatus outer2 = tm2.begin("Outer2");
        TransactionStatus newTx  = TxSupport.getOrCreate(tm2, "Inner2", Propagation.REQUIRES_NEW);
        System.out.println("    outer2 == newTx: " + (outer2 == newTx) + " (son tx distintas)");
        tm2.commit(newTx);
        tm2.commit(outer2);

        System.out.println("\n  → NESTED (savepoint dentro de outer):");
        TransactionManager tm3 = new TransactionManager();
        TransactionStatus outer3  = tm3.begin("Outer3");
        TransactionStatus nested  = TxSupport.getOrCreate(tm3, "Inner3", Propagation.NESTED);
        System.out.println("    inner tiene savepoint propio dentro de outer3");
        tm3.commit(nested);
        tm3.commit(outer3);
    }
}
