import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Simula JPQL y Criteria API de JPA.
// JpqlParser ejecuta consultas JPQL básicas sobre listas en memoria.
// CriteriaBuilder ofrece una API fluida equivalente.
// Ambos producen resultados idénticos para las mismas condiciones.

// ── Entidad ───────────────────────────────────────────────────────────────────

// @Entity
class UserEntity {
    private final Long   id;
    private final String nombre;
    private final String email;
    private final boolean activo;
    private final int    edad;

    UserEntity(Long id, String nombre, String email, boolean activo, int edad) {
        this.id     = id;
        this.nombre = nombre;
        this.email  = email;
        this.activo = activo;
        this.edad   = edad;
    }

    public Long    getId()     { return id; }
    public String  getNombre() { return nombre; }
    public String  getEmail()  { return email; }
    public boolean isActivo()  { return activo; }
    public int     getEdad()   { return edad; }

    @Override
    public String toString() {
        return "User{id=" + id + ", nombre='" + nombre + "', activo=" + activo + ", edad=" + edad + "}";
    }
}

// ── Repositorio en memoria ────────────────────────────────────────────────────

class UserRepository {
    private final List<UserEntity> datos = new ArrayList<>(List.of(
        new UserEntity(1L, "Ana García",   "ana@example.com",    true,  28),
        new UserEntity(2L, "Luis Martín",  "luis@example.com",   true,  34),
        new UserEntity(3L, "Marta López",  "marta@example.com",  false, 22),
        new UserEntity(4L, "Carlos Ruiz",  "carlos@example.com", true,  45),
        new UserEntity(5L, "Elena Torres", "elena@example.com",  false, 19),
        new UserEntity(6L, "Jorge Díaz",   "jorge@example.com",  true,  31)
    ));

    public List<UserEntity> getAll() { return List.copyOf(datos); }
}

// ── JPQL Parser (simulación) ──────────────────────────────────────────────────

// Simula la ejecución de JPQL básico:
// "SELECT u FROM UserEntity u WHERE u.activo = true ORDER BY u.nombre"
// El parser extrae la condición WHERE y el ORDER BY de la cadena JPQL.
class JpqlParser {

    private final UserRepository repo;

    JpqlParser(UserRepository repo) { this.repo = repo; }

    // Soporta: SELECT u FROM <Entity> u [WHERE u.<field> = <value>] [ORDER BY u.<field> [ASC|DESC]]
    public List<UserEntity> execute(String jpql) {
        System.out.println("  [JPQL ] " + jpql);

        List<UserEntity> result = new ArrayList<>(repo.getAll());

        // Extraer WHERE
        int whereIdx = jpql.toUpperCase().indexOf(" WHERE ");
        int orderIdx = jpql.toUpperCase().indexOf(" ORDER BY ");

        if (whereIdx != -1) {
            String condition = (orderIdx != -1)
                ? jpql.substring(whereIdx + 7, orderIdx).trim()
                : jpql.substring(whereIdx + 7).trim();
            result = applyWhere(result, condition);
        }

        // Extraer ORDER BY
        if (orderIdx != -1) {
            String orderClause = jpql.substring(orderIdx + 10).trim();
            result = applyOrder(result, orderClause);
        }

        return result;
    }

    private List<UserEntity> applyWhere(List<UserEntity> list, String condition) {
        // Soporta: u.<field> = <value>   y   u.<field> > <number>
        if (condition.contains(" = ")) {
            String[] parts = condition.split(" = ");
            String field = parts[0].trim().replaceAll("u\\.", "");
            String value = parts[1].trim().replace("'", "");
            return list.stream().filter(u -> matchField(u, field, "=", value)).toList();
        }
        if (condition.contains(" > ")) {
            String[] parts = condition.split(" > ");
            String field = parts[0].trim().replaceAll("u\\.", "");
            String value = parts[1].trim();
            return list.stream().filter(u -> matchField(u, field, ">", value)).toList();
        }
        return list;
    }

    private boolean matchField(UserEntity u, String field, String op, String value) {
        return switch (field) {
            case "activo"  -> op.equals("=") && u.isActivo() == Boolean.parseBoolean(value);
            case "nombre"  -> op.equals("=") && u.getNombre().equals(value);
            case "edad"    -> {
                int v = Integer.parseInt(value);
                yield op.equals("=") ? u.getEdad() == v : u.getEdad() > v;
            }
            default -> true;
        };
    }

    private List<UserEntity> applyOrder(List<UserEntity> list, String clause) {
        boolean desc = clause.toUpperCase().endsWith("DESC");
        String field = clause.replaceAll("(?i)u\\.","").replaceAll("(?i)\\s+(ASC|DESC)","").trim();
        Comparator<UserEntity> comp = switch (field) {
            case "nombre" -> Comparator.comparing(UserEntity::getNombre);
            case "edad"   -> Comparator.comparingInt(UserEntity::getEdad);
            default       -> Comparator.comparing(UserEntity::getId);
        };
        if (desc) comp = comp.reversed();
        return list.stream().sorted(comp).toList();
    }
}

// ── Criteria API (simulación) ─────────────────────────────────────────────────

// Equivale a javax.persistence.criteria.CriteriaBuilder
class CriteriaBuilder {

    private final UserRepository repo;

    CriteriaBuilder(UserRepository repo) { this.repo = repo; }

    // Punto de entrada: cb.from(UserEntity.class)
    CriteriaQuery from(@SuppressWarnings("unused") Class<UserEntity> entityClass) {
        return new CriteriaQuery(repo);
    }

    // Predicado equal: cb.equal("campo", valor)
    static Predicate<UserEntity> equal(String field, Object value) {
        return u -> switch (field) {
            case "activo" -> u.isActivo() == (Boolean) value;
            case "nombre" -> u.getNombre().equals(value);
            case "edad"   -> u.getEdad() == (Integer) value;
            default       -> true;
        };
    }

    // Predicado greaterThan: cb.greaterThan("campo", valor)
    static Predicate<UserEntity> greaterThan(String field, int value) {
        return u -> switch (field) {
            case "edad" -> u.getEdad() > value;
            default     -> true;
        };
    }

    static class CriteriaQuery {
        private final UserRepository       repo;
        private Predicate<UserEntity>      predicate = u -> true;
        private Comparator<UserEntity>     order     = null;

        CriteriaQuery(UserRepository repo) { this.repo = repo; }

        // .where(predicate)
        public CriteriaQuery where(Predicate<UserEntity> p) {
            this.predicate = p;
            return this;
        }

        // .orderBy("campo") o .orderBy("campo DESC")
        public CriteriaQuery orderBy(String clause) {
            boolean desc = clause.toUpperCase().endsWith("DESC");
            String field = clause.replaceAll("(?i)\\s+(ASC|DESC)","").trim();
            Comparator<UserEntity> comp = switch (field) {
                case "nombre" -> Comparator.comparing(UserEntity::getNombre);
                case "edad"   -> Comparator.comparingInt(UserEntity::getEdad);
                default       -> Comparator.comparing(UserEntity::getId);
            };
            this.order = desc ? comp.reversed() : comp;
            return this;
        }

        // .build() ejecuta la query
        public List<UserEntity> build() {
            System.out.println("  [Criteria] CriteriaQuery ejecutada");
            List<UserEntity> result = repo.getAll().stream()
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));
            if (order != null) result.sort(order);
            return result;
        }
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpQueryDSL {
    public static void main(String[] args) {

        UserRepository repo   = new UserRepository();
        JpqlParser     jpql   = new JpqlParser(repo);
        CriteriaBuilder cb    = new CriteriaBuilder(repo);

        System.out.println("=== Simulación JPQL y Criteria API ===\n");

        // ─── Query 1: usuarios activos, ordenados por nombre ─────────────────
        System.out.println("[ Query 1: usuarios activos, ordenados por nombre ]\n");

        System.out.println("  JPQL:");
        List<UserEntity> r1jpql = jpql.execute(
            "SELECT u FROM UserEntity u WHERE u.activo = true ORDER BY u.nombre");
        r1jpql.forEach(u -> System.out.println("    " + u));

        System.out.println("\n  Criteria API:");
        List<UserEntity> r1cb = cb.from(UserEntity.class)
            .where(CriteriaBuilder.equal("activo", true))
            .orderBy("nombre")
            .build();
        r1cb.forEach(u -> System.out.println("    " + u));

        System.out.println("\n  Resultados idénticos: " + r1jpql.toString().equals(r1cb.toString()));

        System.out.println("\n" + "─".repeat(60) + "\n");

        // ─── Query 2: usuarios con edad > 30, ordenados por edad DESC ────────
        System.out.println("[ Query 2: usuarios con edad > 30, orden edad DESC ]\n");

        System.out.println("  JPQL:");
        List<UserEntity> r2jpql = jpql.execute(
            "SELECT u FROM UserEntity u WHERE u.edad > 30 ORDER BY u.edad DESC");
        r2jpql.forEach(u -> System.out.println("    " + u));

        System.out.println("\n  Criteria API:");
        List<UserEntity> r2cb = cb.from(UserEntity.class)
            .where(CriteriaBuilder.greaterThan("edad", 30))
            .orderBy("edad DESC")
            .build();
        r2cb.forEach(u -> System.out.println("    " + u));

        System.out.println("\n  Resultados idénticos: " + r2jpql.toString().equals(r2cb.toString()));

        System.out.println("\n" + "─".repeat(60) + "\n");
        System.out.println("[ Notas ]");
        System.out.println("  JPQL: legible, estático, comprobado en tiempo de despliegue.");
        System.out.println("  Criteria API: type-safe, composable dinámicamente en runtime.");
        System.out.println("  QueryDSL (lib externa) combina ambos: fluent + type-safe con metamodelo.");
    }
}
