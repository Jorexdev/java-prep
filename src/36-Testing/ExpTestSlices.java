import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Simula los test slices de Spring Boot: @WebMvcTest y @DataJpaTest.
// SliceContext carga solo los beans necesarios para la capa bajo prueba,
// imitando el comportamiento del ApplicationContext parcial de Spring.

// ── Capa de entidad / repositorio ─────────────────────────────────────────────

// @Entity
class UsuarioSlice {
    private Long   id;
    private String nombre;
    private String email;

    UsuarioSlice(Long id, String nombre, String email) {
        this.id     = id;
        this.nombre = nombre;
        this.email  = email;
    }

    public Long   getId()     { return id; }
    public String getNombre() { return nombre; }
    public String getEmail()  { return email; }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nombre='" + nombre + "', email='" + email + "'}";
    }
}

// @Repository (JpaRepository<UsuarioSlice, Long>)
class UsuarioRepository {
    private final Map<Long, UsuarioSlice> store = new HashMap<>();
    private long nextId = 1;

    public UsuarioSlice save(UsuarioSlice u) {
        if (u.getId() == null) u = new UsuarioSlice(nextId++, u.getNombre(), u.getEmail());
        store.put(u.getId(), u);
        return u;
    }

    public Optional<UsuarioSlice> findById(Long id)    { return Optional.ofNullable(store.get(id)); }
    public Optional<UsuarioSlice> findByEmail(String e){ return store.values().stream().filter(u -> u.getEmail().equals(e)).findFirst(); }
    public List<UsuarioSlice>     findAll()             { return new ArrayList<>(store.values()); }
    public void                   deleteById(Long id)   { store.remove(id); }
    public long                   count()               { return store.size(); }
}

// ── Capa de servicio ──────────────────────────────────────────────────────────

// @Service
class UsuarioService {
    private final UsuarioRepository repo;

    UsuarioService(UsuarioRepository repo) { this.repo = repo; }

    public UsuarioSlice crear(String nombre, String email) {
        return repo.save(new UsuarioSlice(null, nombre, email));
    }

    public Optional<UsuarioSlice> buscarPorEmail(String email) {
        return repo.findByEmail(email);
    }
}

// ── Capa web (controller) ─────────────────────────────────────────────────────

// @RestController @RequestMapping("/api/usuarios")
class UsuarioControllerSlice {
    private final UsuarioService service;

    // @Autowired
    UsuarioControllerSlice(UsuarioService service) { this.service = service; }

    // @GetMapping("/{email}") → simula MockMvc.perform(get(...))
    String obtenerPorEmail(String email) {
        return service.buscarPorEmail(email)
            .map(u -> "200 OK: " + u)
            .orElse("404 Not Found");
    }

    // @PostMapping
    String crear(String nombre, String email) {
        UsuarioSlice u = service.crear(nombre, email);
        return "201 Created: " + u;
    }
}

// ── SliceContext ──────────────────────────────────────────────────────────────

// Simula el ApplicationContext parcial de Spring Boot para cada slice
class SliceContext {

    private UsuarioRepository       repositoryBean;
    private UsuarioService          serviceBean;
    private UsuarioControllerSlice  controllerBean;

    // @WebMvcTest — solo carga Controller y MockMvc; no hay Service real ni Repository real
    static SliceContext webMvcTest() {
        SliceContext ctx = new SliceContext();
        // En @WebMvcTest el Service se mockea con @MockBean
        // Aquí simulamos un "mock" de service que siempre devuelve vacío
        UsuarioService mockService = new UsuarioService(new UsuarioRepository()) {
            @Override
            public Optional<UsuarioSlice> buscarPorEmail(String email) {
                return Optional.empty();   // mock: no hay datos reales
            }
        };
        ctx.controllerBean = new UsuarioControllerSlice(mockService);
        // NO hay ctx.repositoryBean — el repositorio no se carga en WebMvcTest
        return ctx;
    }

    // @DataJpaTest — solo carga Entity + Repository + H2 en memoria; no hay Controller ni Service
    static SliceContext dataJpaTest() {
        SliceContext ctx = new SliceContext();
        ctx.repositoryBean = new UsuarioRepository();   // "H2 en memoria"
        // NO hay ctx.controllerBean ni ctx.serviceBean en DataJpaTest
        return ctx;
    }

    // Acceso a los beans disponibles en cada slice
    UsuarioRepository       getRepository() {
        if (repositoryBean == null) throw new IllegalStateException(
            "UsuarioRepository NO está disponible en este slice (solo en @DataJpaTest)");
        return repositoryBean;
    }

    UsuarioControllerSlice  getController() {
        if (controllerBean == null) throw new IllegalStateException(
            "UsuarioControllerSlice NO está disponible en este slice (solo en @WebMvcTest)");
        return controllerBean;
    }

    UsuarioService          getService() {
        if (serviceBean == null) throw new IllegalStateException(
            "UsuarioService NO está disponible en este slice");
        return serviceBean;
    }
}

// ── Tests simulados ───────────────────────────────────────────────────────────

class WebMvcTests {

    // @ExtendWith(SpringExtension.class) @WebMvcTest(UsuarioControllerSlice.class)
    static void run() {
        System.out.println("[ @WebMvcTest — solo capa web ]");
        System.out.println("  Beans disponibles: UsuarioControllerSlice, MockMvc");
        System.out.println("  Beans NO disponibles: UsuarioRepository, UsuarioService (mockeado)\n");

        SliceContext ctx = SliceContext.webMvcTest();

        // Test 1: controller disponible → OK
        System.out.println("  Test 1: controller está en el contexto");
        UsuarioControllerSlice ctrl = ctx.getController();
        System.out.println("    → " + ctrl.getClass().getSimpleName() + " disponible ✓");

        // Test 2: buscar usuario (service mock → 404)
        System.out.println("  Test 2: GET /api/usuarios/test@mail.com (mock service devuelve vacío)");
        String resp = ctrl.obtenerPorEmail("test@mail.com");
        System.out.println("    → " + resp + " ✓");

        // Test 3: intentar acceder al repo → debe lanzar error
        System.out.println("  Test 3: acceder al repository (no cargado en @WebMvcTest)");
        try {
            ctx.getRepository();
            System.out.println("    → ERROR: debería haber lanzado excepción");
        } catch (IllegalStateException ex) {
            System.out.println("    → IllegalStateException: " + ex.getMessage() + " ✓");
        }
    }
}

class DataJpaTests {

    // @ExtendWith(SpringExtension.class) @DataJpaTest
    static void run() {
        System.out.println("[ @DataJpaTest — solo capa de datos ]");
        System.out.println("  Beans disponibles: UsuarioRepository, H2 DataSource, EntityManager");
        System.out.println("  Beans NO disponibles: UsuarioControllerSlice, UsuarioService\n");

        SliceContext ctx = SliceContext.dataJpaTest();

        // Test 1: repositorio disponible → guardar y buscar
        System.out.println("  Test 1: guardar usuario y buscar por email");
        UsuarioRepository repo = ctx.getRepository();
        UsuarioSlice guardado = repo.save(new UsuarioSlice(null, "Ana García", "ana@test.com"));
        Optional<UsuarioSlice> encontrado = repo.findByEmail("ana@test.com");
        System.out.println("    guardado: " + guardado);
        System.out.println("    encontrado: " + encontrado.orElse(null) + " ✓");

        // Test 2: contar entidades
        System.out.println("  Test 2: count() después de inserción");
        repo.save(new UsuarioSlice(null, "Luis Ruiz", "luis@test.com"));
        System.out.println("    count=" + repo.count() + " (esperado 2) ✓");

        // Test 3: intentar acceder al controller → debe lanzar error
        System.out.println("  Test 3: acceder al controller (no cargado en @DataJpaTest)");
        try {
            ctx.getController();
            System.out.println("    → ERROR: debería haber lanzado excepción");
        } catch (IllegalStateException ex) {
            System.out.println("    → IllegalStateException: " + ex.getMessage() + " ✓");
        }
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpTestSlices {
    public static void main(String[] args) {

        System.out.println("=== Simulación Spring Test Slices ===\n");

        WebMvcTests.run();

        System.out.println("\n" + "─".repeat(60) + "\n");

        DataJpaTests.run();

        System.out.println("\n" + "─".repeat(60));
        System.out.println("\n[ Comparativa de slices ]");
        System.out.printf("  %-20s %-15s %-15s %-10s%n", "Slice",  "Controller", "Service",   "Repository");
        System.out.printf("  %-20s %-15s %-15s %-10s%n", "------", "-----------","-------",   "----------");
        System.out.printf("  %-20s %-15s %-15s %-10s%n", "@WebMvcTest",  "✓ (real)",  "@MockBean", "✗");
        System.out.printf("  %-20s %-15s %-15s %-10s%n", "@DataJpaTest", "✗",         "✗",        "✓ (H2)");
        System.out.printf("  %-20s %-15s %-15s %-10s%n", "@SpringBootTest","✓",       "✓",        "✓");
        System.out.println("\n  Slices = pruebas más rápidas porque cargan solo la capa necesaria.");
    }
}
