import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Simula integración con base de datos real usando el concepto de Testcontainers.
// TestDatabase arranca un "contenedor" H2 en memoria con schema.sql y datos seed.
// UserRepositoryIT es el test de integración que ejecuta SQL real via JDBC simulado.

// ── Entidad ───────────────────────────────────────────────────────────────────

class UserTC {
    private Long   id;
    private String nombre;
    private String email;
    private boolean activo;

    UserTC(Long id, String nombre, String email, boolean activo) {
        this.id     = id;
        this.nombre = nombre;
        this.email  = email;
        this.activo = activo;
    }

    public Long    getId()     { return id; }
    public String  getNombre() { return nombre; }
    public String  getEmail()  { return email; }
    public boolean isActivo()  { return activo; }
    public void    setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "User{id=" + id + ", nombre='" + nombre + "', email='" + email
            + "', activo=" + activo + "}";
    }
}

// ── TestDatabase — simula el contenedor H2 de Testcontainers ─────────────────

// @Testcontainers → JUnit 5 gestiona el ciclo de vida del contenedor
// @Container PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
class TestDatabase {

    private final Map<Long, UserTC> tabla  = new HashMap<>();
    private long nextId = 1;
    private boolean started = false;

    // Equivale a @BeforeAll — Docker start + flyway/liquibase
    void start() {
        System.out.println("  [TestDB] Iniciando contenedor H2 en memoria...");
        System.out.println("  [TestDB] Ejecutando schema.sql:");
        System.out.println("    CREATE TABLE users (");
        System.out.println("        id      BIGINT PRIMARY KEY AUTO_INCREMENT,");
        System.out.println("        nombre  VARCHAR(100) NOT NULL,");
        System.out.println("        email   VARCHAR(150) UNIQUE NOT NULL,");
        System.out.println("        activo  BOOLEAN DEFAULT TRUE");
        System.out.println("    );");
        started = true;
        System.out.println("  [TestDB] Schema aplicado. Contenedor listo.");
    }

    // Equivale a @BeforeEach — limpiar y sembrar datos iniciales
    void seed() {
        tabla.clear();
        nextId = 1;
        System.out.println("  [TestDB] Seed: INSERT INTO users ...");
        save(new UserTC(null, "Ana García",  "ana@test.com",    true));
        save(new UserTC(null, "Luis Martín", "luis@test.com",   true));
        save(new UserTC(null, "Marta López", "marta@test.com",  false));
        System.out.println("  [TestDB] Seed completado: " + tabla.size() + " usuarios.");
    }

    // Equivale a @AfterAll — Docker stop
    void stop() {
        tabla.clear();
        started = false;
        System.out.println("  [TestDB] Contenedor detenido y limpiado.");
    }

    // Operaciones "JDBC" sobre la tabla en memoria
    UserTC save(UserTC u) {
        if (u.getId() == null) u = new UserTC(nextId++, u.getNombre(), u.getEmail(), u.isActivo());
        tabla.put(u.getId(), u);
        return u;
    }

    Optional<UserTC> findByEmail(String email) {
        return tabla.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    Optional<UserTC> findById(Long id) { return Optional.ofNullable(tabla.get(id)); }

    boolean deleteById(Long id) { return tabla.remove(id) != null; }

    long count() { return tabla.size(); }

    List<UserTC> findAll() { return new ArrayList<>(tabla.values()); }
}

// ── UserRepositoryIT — test de integración ────────────────────────────────────

// @SpringBootTest @Testcontainers
// @Transactional — cada test corre en su propia transacción que se revierte al final
class UserRepositoryIT {

    private final TestDatabase db;

    UserRepositoryIT(TestDatabase db) { this.db = db; }

    void testCrearUsuario() {
        System.out.println("  [Test] testCrearUsuario");
        long antes = db.count();
        UserTC nuevo = db.save(new UserTC(null, "Pedro Ruiz", "pedro@test.com", true));
        long despues = db.count();

        assert nuevo.getId() != null           : "ID debe ser asignado";
        assert despues == antes + 1            : "count debe aumentar en 1";
        System.out.println("    count antes=" + antes + " → después=" + despues + " ✓");
        System.out.println("    usuario creado: " + nuevo + " ✓");
    }

    void testBuscarPorEmail() {
        System.out.println("  [Test] testBuscarPorEmail");
        Optional<UserTC> found    = db.findByEmail("ana@test.com");
        Optional<UserTC> notFound = db.findByEmail("noexiste@x.com");

        assert found.isPresent()    : "debe encontrar ana@test.com";
        assert !notFound.isPresent(): "no debe encontrar noexiste@x.com";
        System.out.println("    findByEmail('ana@test.com'): " + found.get() + " ✓");
        System.out.println("    findByEmail('noexiste@x.com'): empty=" + notFound.isEmpty() + " ✓");
    }

    void testActualizarActivo() {
        System.out.println("  [Test] testActualizarActivo");
        // Marta empieza inactiva, la activamos
        Optional<UserTC> marta = db.findByEmail("marta@test.com");
        assert marta.isPresent() : "Marta debe existir";
        System.out.println("    activo antes: " + marta.get().isActivo());
        marta.get().setActivo(true);
        db.save(marta.get());
        System.out.println("    activo después: " + db.findByEmail("marta@test.com").get().isActivo() + " ✓");
    }

    void testEliminarUsuario() {
        System.out.println("  [Test] testEliminarUsuario");
        long antes = db.count();
        boolean eliminado = db.deleteById(1L);   // Ana, id=1 del seed
        long despues = db.count();

        assert eliminado           : "debe eliminar con id=1";
        assert despues == antes - 1: "count debe disminuir en 1";
        System.out.println("    eliminado=true, count: " + antes + "→" + despues + " ✓");
        System.out.println("    findById(1): " + db.findById(1L) + " (empty) ✓");
    }

    void testContarFilas() {
        System.out.println("  [Test] testContarFilas");
        long count = db.count();
        System.out.println("    count=" + count + " (seed insertó 3) ✓");
        assert count == 3 : "debe haber 3 filas del seed";
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpTestContainers {
    public static void main(String[] args) {

        System.out.println("=== Simulación Testcontainers — Integration Test con BD real ===\n");

        TestDatabase    db   = new TestDatabase();
        UserRepositoryIT test = new UserRepositoryIT(db);

        // ─── @BeforeAll: arrancar contenedor ─────────────────────────────────
        db.start();

        System.out.println();

        // ─── Cada test: @BeforeEach seed + test + @AfterEach rollback ─────────
        System.out.println("[ Tests de integración ]");
        System.out.println();

        db.seed();
        test.testContarFilas();
        System.out.println("  → @AfterEach: transacción revertida\n");

        db.seed();
        test.testCrearUsuario();
        System.out.println("  → @AfterEach: transacción revertida\n");

        db.seed();
        test.testBuscarPorEmail();
        System.out.println("  → @AfterEach: transacción revertida\n");

        db.seed();
        test.testActualizarActivo();
        System.out.println("  → @AfterEach: transacción revertida\n");

        db.seed();
        test.testEliminarUsuario();
        System.out.println("  → @AfterEach: transacción revertida\n");

        // ─── @AfterAll: detener contenedor ────────────────────────────────────
        db.stop();

        System.out.println();
        System.out.println("[ Notas Testcontainers ]");
        System.out.println("  @Container: el contenedor Docker arranca 1 vez por clase (@Shared)");
        System.out.println("              o 1 vez por test (sin @Shared).");
        System.out.println("  @Transactional en tests: cada test corre en su propia tx que se revierte.");
        System.out.println("  Ventaja sobre H2: prueba contra el mismo motor que producción (PostgreSQL).");
        System.out.println("  Requiere Docker instalado. Sin Docker → lanzar tests con H2 como fallback.");
    }
}
