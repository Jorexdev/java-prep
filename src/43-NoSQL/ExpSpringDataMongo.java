import java.util.*;
import java.util.stream.*;
import java.util.function.Predicate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulación de patrones de Spring Data MongoDB con Java puro.
 *
 * Conceptos demostrados:
 *  - @Document, @Id, @Field, @Indexed (anotaciones simuladas como comentarios)
 *  - MongoRepository: findByEmail, findByAgeGreaterThan, @Query personalizada
 *  - MongoTemplate vs MongoRepository: cuándo usar cada uno
 *  - @Transactional en MongoDB: requiere replica set
 *  - Queries derivadas del nombre del método
 */
public class ExpSpringDataMongo {

    // ─────────────────────────────────────────────
    // ENTIDAD: simula @Document(collection="usuarios")
    // En Spring Data: @Document, @Id, @Field, @Indexed
    // ─────────────────────────────────────────────

    // @Document(collection = "usuarios")
    static class Usuario {
        // @Id
        private String id;
        // @Field("nombre")
        private String nombre;
        // @Indexed(unique = true)
        private String email;
        private int edad;
        private String estado;

        Usuario(String id, String nombre, String email, int edad, String estado) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.edad = edad;
            this.estado = estado;
        }

        String getId()     { return id; }
        String getNombre() { return nombre; }
        String getEmail()  { return email; }
        int getEdad()      { return edad; }
        String getEstado() { return estado; }
        void setEstado(String estado) { this.estado = estado; }

        @Override
        public String toString() {
            return String.format("Usuario{id='%s', nombre='%s', email='%s', edad=%d, estado='%s'}",
                    id, nombre, email, edad, estado);
        }
    }

    // ─────────────────────────────────────────────
    // REPOSITORIO: simula MongoRepository<Usuario, String>
    // Spring Data genera estas implementaciones en tiempo de compilación
    // basándose en el nombre del método (derived queries)
    // ─────────────────────────────────────────────

    // interface UsuarioRepository extends MongoRepository<Usuario, String>
    static class UsuarioRepository {
        private final List<Usuario> store = new ArrayList<>();
        private final AtomicInteger queryCount = new AtomicInteger(0);

        // save / findById (heredados de MongoRepository)
        void save(Usuario u) {
            store.removeIf(existing -> existing.getId().equals(u.getId()));
            store.add(u);
        }

        Optional<Usuario> findById(String id) {
            queryCount.incrementAndGet();
            logQuery("findById", "{'_id': '" + id + "'}");
            return store.stream().filter(u -> u.getId().equals(id)).findFirst();
        }

        List<Usuario> findAll() {
            queryCount.incrementAndGet();
            logQuery("findAll", "{}");
            return Collections.unmodifiableList(store);
        }

        // Derived query: Spring interpreta el nombre del método
        // → db.usuarios.find({ email: ?0 })
        Optional<Usuario> findByEmail(String email) {
            queryCount.incrementAndGet();
            logQuery("findByEmail", "{'email': '" + email + "'}");
            return store.stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }

        // Derived query: findByEdadGreaterThan
        // → db.usuarios.find({ edad: { $gt: ?0 } })
        List<Usuario> findByEdadGreaterThan(int edad) {
            queryCount.incrementAndGet();
            logQuery("findByEdadGreaterThan", "{'edad': {$gt: " + edad + "}}");
            return store.stream().filter(u -> u.getEdad() > edad).collect(Collectors.toList());
        }

        // @Query personalizada: simula @Query("{'estado': ?0, 'edad': {$gte: ?1}}")
        // @Query("{'estado': ?0, 'edad': {$gte: ?1}}")
        List<Usuario> findByEstadoAndEdadMinima(String estado, int edadMinima) {
            queryCount.incrementAndGet();
            logQuery("@Query custom", String.format("{'estado': '%s', 'edad': {$gte: %d}}", estado, edadMinima));
            return store.stream()
                    .filter(u -> u.getEstado().equals(estado) && u.getEdad() >= edadMinima)
                    .collect(Collectors.toList());
        }

        int getQueryCount() { return queryCount.get(); }

        private void logQuery(String method, String mongoQuery) {
            System.out.printf("  [QUERY #%d] %s → %s%n", queryCount.get(), method, mongoQuery);
        }
    }

    // ─────────────────────────────────────────────
    // MONGO TEMPLATE: más control que el repositorio
    // Útil para aggregations complejas y operaciones ad-hoc
    // ─────────────────────────────────────────────

    static class MongoTemplate {
        private final UsuarioRepository repository;

        MongoTemplate(UsuarioRepository repository) {
            this.repository = repository;
        }

        // MongoTemplate.find(Query, Class) — más flexible
        List<Usuario> find(Predicate<Usuario> criteria, String descripcion) {
            System.out.printf("  [MongoTemplate.find] criteria: %s%n", descripcion);
            return repository.store.stream().filter(criteria).collect(Collectors.toList());
        }

        // MongoTemplate.updateMulti: actualiza todos los documentos que cumplen criteria
        long updateMulti(Predicate<Usuario> criteria, String campo, Object valor) {
            System.out.printf("  [MongoTemplate.updateMulti] %s = %s%n", campo, valor);
            return repository.store.stream()
                    .filter(criteria)
                    .peek(u -> { if ("estado".equals(campo)) u.setEstado((String) valor); })
                    .count();
        }

        // Aggregation: MongoTemplate tiene soporte directo para Aggregation.newAggregation(...)
        Map<String, Long> aggregateCountByEstado() {
            System.out.println("  [MongoTemplate.aggregate] group by estado, count");
            return repository.store.stream()
                    .collect(Collectors.groupingBy(Usuario::getEstado, Collectors.counting()));
        }
    }

    // ─────────────────────────────────────────────
    // TRANSACCIONAL: simula @Transactional en MongoDB
    // ─────────────────────────────────────────────

    static class UsuarioService {
        private final UsuarioRepository repository;

        UsuarioService(UsuarioRepository repository) {
            this.repository = repository;
        }

        // @Transactional
        // IMPORTANTE: @Transactional en MongoDB requiere replica set.
        // En MongoDB standalone, las operaciones son atómicas a nivel de documento
        // pero NO hay transacciones multi-documento. Con replica set activado,
        // MongoDB soporta transacciones ACID multi-colección vía sesiones.
        void transferirEstado(String idOrigen, String idDestino, String estadoNuevo) {
            System.out.println("  [TX] Iniciando transacción (requiere replica set)");
            // En Spring real: ambas operaciones van en la misma sesión de MongoDB
            // Si una falla → rollback automático (solo con replica set)
            Usuario origen = repository.findById(idOrigen).orElseThrow();
            Usuario destino = repository.findById(idDestino).orElseThrow();
            origen.setEstado("procesado");
            destino.setEstado(estadoNuevo);
            repository.save(origen);
            repository.save(destino);
            System.out.printf("  [TX] Commit: %s → procesado, %s → %s%n", idOrigen, idDestino, estadoNuevo);
        }
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== ExpSpringDataMongo: Patrones Spring Data MongoDB ===\n");

        UsuarioRepository repo = new UsuarioRepository();

        // Poblar datos
        repo.save(new Usuario("U01", "Ana García",   "ana@ejemplo.com",    28, "activo"));
        repo.save(new Usuario("U02", "Carlos López",  "carlos@ejemplo.com", 35, "activo"));
        repo.save(new Usuario("U03", "Bea Martínez",  "bea@ejemplo.com",    22, "inactivo"));
        repo.save(new Usuario("U04", "David Ruiz",    "david@ejemplo.com",  41, "activo"));
        repo.save(new Usuario("U05", "Elena Sánchez", "elena@ejemplo.com",  19, "pendiente"));

        // ── Derived queries ──
        System.out.println("── 1. Derived queries (nombre del método) ──");
        repo.findByEmail("ana@ejemplo.com")
                .ifPresent(u -> System.out.println("  Encontrado: " + u));

        List<Usuario> mayores30 = repo.findByEdadGreaterThan(30);
        System.out.println("  Mayores de 30: " + mayores30.stream().map(Usuario::getNombre).collect(Collectors.joining(", ")));

        // ── @Query personalizada ──
        System.out.println("\n── 2. @Query personalizada ──");
        List<Usuario> activosMayores25 = repo.findByEstadoAndEdadMinima("activo", 25);
        System.out.println("  Activos ≥25 años: " + activosMayores25.stream().map(Usuario::getNombre).collect(Collectors.joining(", ")));

        // ── MongoTemplate vs Repository ──
        System.out.println("\n── 3. MongoTemplate (más control) ──");
        MongoTemplate template = new MongoTemplate(repo);

        List<Usuario> jóvenes = template.find(u -> u.getEdad() < 25, "edad < 25");
        System.out.println("  Menores de 25: " + jóvenes.stream().map(u -> u.getNombre() + "(" + u.getEdad() + ")").collect(Collectors.joining(", ")));

        long actualizados = template.updateMulti(u -> "inactivo".equals(u.getEstado()), "estado", "archivado");
        System.out.println("  Usuarios actualizados a 'archivado': " + actualizados);

        Map<String, Long> porEstado = template.aggregateCountByEstado();
        System.out.println("  Distribución por estado: " + porEstado);

        // ── @Transactional ──
        System.out.println("\n── 4. @Transactional (requiere replica set en producción) ──");
        UsuarioService service = new UsuarioService(repo);
        service.transferirEstado("U01", "U05", "activo");

        System.out.printf("%n── Resumen: %d queries ejecutadas en total ──%n", repo.getQueryCount());

        // ── Comparativa MongoRepository vs MongoTemplate ──
        System.out.println("""

── MongoRepository vs MongoTemplate ──
  MongoRepository:
    ✓ Queries derivadas del nombre del método (sin código)
    ✓ CRUD básico heredado (save, findById, findAll, deleteById)
    ✓ @Query para queries custom en JSON
    ✗ Aggregations complejas requieren @Aggregation o pasar a Template

  MongoTemplate:
    ✓ Control total: Criteria, Query, Update, Aggregation
    ✓ Operaciones bulk (updateMulti, insertAll)
    ✓ Aggregation pipeline complejo
    ✗ Más verboso: requiere construir Query y Criteria manualmente""");
    }
}
