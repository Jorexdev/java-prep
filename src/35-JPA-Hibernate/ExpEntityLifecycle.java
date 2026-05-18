import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

// Simula el ciclo de vida de entidades JPA (Transient → Managed → Detached → Removed)
// usando un EntityManager simplificado con Map interno.
// Las anotaciones JPA aparecen como comentarios junto al código equivalente.

// ── Entidad ───────────────────────────────────────────────────────────────────

// @Entity
// @Table(name = "empleados")
class Empleado {
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(nullable = false)
    private String nombre;

    private String departamento;

    // Estado de ciclo de vida — en JPA real lo gestiona Hibernate internamente
    enum Estado { TRANSIENT, MANAGED, DETACHED, REMOVED }
    private Estado estado = Estado.TRANSIENT;

    public Empleado(String nombre, String departamento) {
        this.nombre = nombre;
        this.departamento = departamento;
    }

    public Long getId()            { return id; }
    public String getNombre()      { return nombre; }
    public String getDepartamento(){ return departamento; }
    public Estado getEstado()      { return estado; }

    void setId(Long id)            { this.id = id; }
    void setEstado(Estado estado)  { this.estado = estado; }
    void setNombre(String nombre)  { this.nombre = nombre; }

    @Override
    public String toString() {
        return "Empleado{id=" + id + ", nombre='" + nombre
            + "', depto='" + departamento + "', estado=" + estado + "}";
    }
}

// ── EntityManager simplificado ────────────────────────────────────────────────

// Simula javax.persistence.EntityManager / jakarta.persistence.EntityManager
class SimpleEntityManager {

    // PersistenceContext — caché L1: mapa de identidad dentro de la transacción
    private final Map<Long, Empleado> persistenceContext = new HashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);
    private boolean transaccionActiva = false;

    // Simula EntityTransaction.begin()
    public void beginTransaction() {
        transaccionActiva = true;
        System.out.println("  [EM] beginTransaction()");
    }

    // persist(entity): TRANSIENT → MANAGED
    // INSERT se ejecutará al hacer flush/commit — NO inmediatamente
    public void persist(Empleado e) {
        if (e.getId() != null) throw new IllegalStateException("La entidad ya tiene id asignado");
        long id = secuencia.getAndIncrement();
        e.setId(id);
        e.setEstado(Empleado.Estado.MANAGED);
        persistenceContext.put(id, e);
        System.out.println("  [EM] persist() → MANAGED  (INSERT pendiente): " + e);
    }

    // find(Class, id): busca primero en PersistenceContext (caché L1), luego en "BD"
    public Empleado find(Long id) {
        if (persistenceContext.containsKey(id)) {
            System.out.println("  [EM] find(" + id + ") → HIT caché L1 (sin query a BD)");
            return persistenceContext.get(id);
        }
        System.out.println("  [EM] find(" + id + ") → MISS caché — SELECT en BD");
        return null; // BD vacía en la simulación inicial
    }

    // detach(entity): MANAGED → DETACHED — sale del PersistenceContext
    public void detach(Empleado e) {
        persistenceContext.remove(e.getId());
        e.setEstado(Empleado.Estado.DETACHED);
        System.out.println("  [EM] detach() → DETACHED: " + e);
    }

    // merge(entity): DETACHED → MANAGED — copia estado al contexto activo
    // Devuelve una NUEVA instancia managed (la original sigue detached)
    public Empleado merge(Empleado e) {
        Empleado managed;
        if (persistenceContext.containsKey(e.getId())) {
            managed = persistenceContext.get(e.getId());
            managed.setNombre(e.getNombre()); // copia cambios
        } else {
            managed = new Empleado(e.getNombre(), e.getDepartamento());
            managed.setId(e.getId());
            persistenceContext.put(e.getId(), managed);
        }
        managed.setEstado(Empleado.Estado.MANAGED);
        System.out.println("  [EM] merge() → MANAGED (nueva instancia): " + managed);
        System.out.println("       (instancia original sigue DETACHED)");
        return managed;
    }

    // remove(entity): MANAGED → REMOVED — marcado para DELETE al hacer flush
    public void remove(Empleado e) {
        if (e.getEstado() != Empleado.Estado.MANAGED) {
            throw new IllegalStateException("Solo se puede eliminar una entidad MANAGED");
        }
        e.setEstado(Empleado.Estado.REMOVED);
        persistenceContext.remove(e.getId());
        System.out.println("  [EM] remove() → REMOVED  (DELETE pendiente): " + e);
    }

    // commit: flush dirty checking + ejecuta SQL pendiente + cierra transacción
    public void commit() {
        System.out.println("  [EM] commit() — dirty checking + SQL flush:");
        for (Empleado e : persistenceContext.values()) {
            if (e.getEstado() == Empleado.Estado.MANAGED) {
                System.out.println("       → INSERT/UPDATE INTO empleados VALUES (" + e.getId() + ", '" + e.getNombre() + "', '" + e.getDepartamento() + "')");
            }
        }
        transaccionActiva = false;
        System.out.println("  [EM] transacción completada");
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpEntityLifecycle {
    public static void main(String[] args) {

        SimpleEntityManager em = new SimpleEntityManager();

        System.out.println("=== Ciclo de vida de entidades JPA ===\n");

        // ── Estado 1: TRANSIENT ──
        System.out.println("-- 1. TRANSIENT --");
        Empleado emp = new Empleado("Ana García", "Ingeniería");
        System.out.println("  new Empleado() → " + emp);
        System.out.println();

        // ── Estado 2: MANAGED (via persist) ──
        System.out.println("-- 2. TRANSIENT → MANAGED (persist) --");
        em.beginTransaction();
        em.persist(emp);
        System.out.println();

        // Persist un segundo empleado
        Empleado emp2 = new Empleado("Luis Martín", "DevOps");
        em.persist(emp2);
        System.out.println();

        // Caché L1 en acción: segunda llamada a find no va a BD
        System.out.println("-- Caché L1: find del mismo id dos veces --");
        em.find(emp.getId());
        em.find(emp.getId()); // hit caché
        System.out.println();

        em.commit();
        System.out.println();

        // ── Estado 3: DETACHED ──
        System.out.println("-- 3. MANAGED → DETACHED (detach) --");
        em.beginTransaction();
        em.detach(emp);
        System.out.println();

        // Modificamos el objeto detached — cambios NO se sincronizan
        System.out.println("-- Modificar objeto DETACHED (cambio NO persiste) --");
        emp.setNombre("Ana García Ruiz");
        System.out.println("  emp.setNombre() → " + emp);
        System.out.println();

        // ── merge: DETACHED → MANAGED con cambios ──
        System.out.println("-- 4. DETACHED → MANAGED (merge) --");
        Empleado empManaged = em.merge(emp);
        System.out.println();

        em.commit();
        System.out.println();

        // ── Estado 4: REMOVED ──
        System.out.println("-- 5. MANAGED → REMOVED (remove) --");
        em.beginTransaction();
        em.remove(emp2);
        em.commit();
        System.out.println();

        // Intentar remove de entidad DETACHED lanza excepción
        System.out.println("-- remove() de entidad DETACHED → IllegalStateException --");
        try {
            em.beginTransaction();
            em.remove(emp); // emp sigue DETACHED (empManaged es la instancia managed)
        } catch (IllegalStateException ex) {
            System.out.println("  [Error] " + ex.getMessage());
        }
    }
}
