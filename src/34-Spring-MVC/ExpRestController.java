import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

// Simula un @RestController de Spring MVC con CRUD completo de productos.
// Las anotaciones Spring aparecen como comentarios junto al código equivalente en Java puro.

// ── Modelo ────────────────────────────────────────────────────────────────────

// @Entity  (en JPA el modelo también sería una entidad)
class Producto {
    private Long id;
    private String nombre;
    private double precio;

    public Producto(Long id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public Long getId()        { return id; }
    public String getNombre()  { return nombre; }
    public double getPrecio()  { return precio; }

    @Override
    public String toString() {
        return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + "}";
    }
}

// ── Excepción de negocio ──────────────────────────────────────────────────────

class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(Long id) {
        super("Producto con id=" + id + " no encontrado");
    }
}

// ── "Base de datos" en memoria ────────────────────────────────────────────────

class ProductoRepository {
    private final List<Producto> bd = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public List<Producto> findAll() {
        return List.copyOf(bd);
    }

    public Optional<Producto> findById(Long id) {
        return bd.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Producto save(String nombre, double precio) {
        Producto p = new Producto(secuencia.getAndIncrement(), nombre, precio);
        bd.add(p);
        return p;
    }

    public Producto update(Long id, String nombre, double precio) {
        Producto viejo = findById(id).orElseThrow(() -> new ProductoNoEncontradoException(id));
        bd.remove(viejo);
        Producto nuevo = new Producto(id, nombre, precio);
        bd.add(nuevo);
        return nuevo;
    }

    public void delete(Long id) {
        Producto p = findById(id).orElseThrow(() -> new ProductoNoEncontradoException(id));
        bd.remove(p);
    }
}

// ── Controller ────────────────────────────────────────────────────────────────

// @RestController                     ← marca la clase como handler REST
// @RequestMapping("/api/productos")   ← prefijo común para todas las rutas
class ProductoController {

    private final ProductoRepository repo;

    // @Autowired (inyección por constructor)
    ProductoController(ProductoRepository repo) {
        this.repo = repo;
    }

    // @GetMapping          → GET /api/productos
    // @ResponseStatus(200) → implícito
    List<Producto> listar() {
        System.out.println("  GET /api/productos → 200 OK");
        return repo.findAll();
    }

    // @GetMapping("/{id}") → GET /api/productos/{id}
    // @PathVariable Long id
    Producto obtener(Long id) {
        System.out.println("  GET /api/productos/" + id);
        Producto p = repo.findById(id)
            .orElseThrow(() -> new ProductoNoEncontradoException(id));
        System.out.println("  → 200 OK: " + p);
        return p;
    }

    // @PostMapping                          → POST /api/productos
    // @RequestBody ProductoDto dto          → deserializa JSON del body
    // @Valid                                → activa Bean Validation en el dto
    // ResponseEntity<Producto>              → 201 Created + Location header
    Producto crear(String nombre, double precio) {
        Producto p = repo.save(nombre, precio);
        // En Spring real: ResponseEntity.created(URI.create("/api/productos/" + p.getId())).body(p)
        System.out.println("  POST /api/productos → 201 Created: " + p);
        return p;
    }

    // @PutMapping("/{id}")  → PUT /api/productos/{id}
    // @PathVariable Long id
    // @RequestBody ProductoDto dto
    Producto actualizar(Long id, String nombre, double precio) {
        Producto p = repo.update(id, nombre, precio);
        System.out.println("  PUT /api/productos/" + id + " → 200 OK: " + p);
        return p;
    }

    // @DeleteMapping("/{id}")              → DELETE /api/productos/{id}
    // @ResponseStatus(HttpStatus.NO_CONTENT) → 204 sin cuerpo
    void eliminar(Long id) {
        repo.delete(id);
        System.out.println("  DELETE /api/productos/" + id + " → 204 No Content");
    }

    // Manejo local de 404 — en producción se movería a @ControllerAdvice
    // @ExceptionHandler(ProductoNoEncontradoException.class)
    // @ResponseStatus(HttpStatus.NOT_FOUND)
    String manejarNotFound(ProductoNoEncontradoException ex) {
        return "{\"error\": 404, \"mensaje\": \"" + ex.getMessage() + "\"}";
    }
}

// ── Simulación del flujo HTTP ─────────────────────────────────────────────────

public class ExpRestController {
    public static void main(String[] args) {

        ProductoRepository repo = new ProductoRepository();
        ProductoController ctrl = new ProductoController(repo);

        System.out.println("=== Simulación CRUD Spring MVC REST ===\n");

        // POST — crear productos
        ctrl.crear("Teclado Mecánico", 89.99);
        ctrl.crear("Monitor 4K", 349.00);
        ctrl.crear("Ratón Inalámbrico", 45.50);

        System.out.println();

        // GET — listar todos
        System.out.println("  GET /api/productos → 200 OK: " + ctrl.listar());

        System.out.println();

        // GET — obtener por id
        ctrl.obtener(2L);

        System.out.println();

        // PUT — actualizar
        ctrl.actualizar(1L, "Teclado Mecánico RGB", 109.99);

        System.out.println();

        // DELETE — eliminar
        ctrl.eliminar(3L);

        System.out.println();

        // GET — listar tras eliminar
        System.out.println("  GET /api/productos (tras DELETE) → " + ctrl.listar());

        System.out.println();

        // GET — id inexistente → 404
        System.out.println("  GET /api/productos/99 →");
        try {
            ctrl.obtener(99L);
        } catch (ProductoNoEncontradoException ex) {
            String respuesta = ctrl.manejarNotFound(ex);
            System.out.println("  → 404 Not Found: " + respuesta);
        }
    }
}
