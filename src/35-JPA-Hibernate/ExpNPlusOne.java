import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Demuestra el problema N+1 y la solución con FETCH JOIN.
// Simula una base de datos en memoria con departamentos y empleados.
// Cada "query" se registra para contar el total de roundtrips a BD.

// ── Modelos ───────────────────────────────────────────────────────────────────

class EmpleadoJpa {
    private final Long id;
    private final String nombre;
    private final Long departamentoId;

    public EmpleadoJpa(Long id, String nombre, Long departamentoId) {
        this.id = id;
        this.nombre = nombre;
        this.departamentoId = departamentoId;
    }

    public Long getId()             { return id; }
    public String getNombre()       { return nombre; }
    public Long getDepartamentoId() { return departamentoId; }

    @Override
    public String toString() { return nombre; }
}

class DepartamentoJpa {
    private final Long id;
    private final String nombre;
    private List<EmpleadoJpa> empleados; // @OneToMany(fetch = LAZY) — no se carga hasta acceder

    public DepartamentoJpa(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.empleados = null; // null = LAZY: no cargado aún
    }

    public Long getId()          { return id; }
    public String getNombre()    { return nombre; }
    public List<EmpleadoJpa> getEmpleados() { return empleados; }
    public void setEmpleados(List<EmpleadoJpa> empleados) { this.empleados = empleados; }
}

// ── Base de datos en memoria ──────────────────────────────────────────────────

class FakeDatabase {

    // Tabla departamentos
    private static final List<DepartamentoJpa> DEPARTAMENTOS = List.of(
        new DepartamentoJpa(1L, "Ingeniería"),
        new DepartamentoJpa(2L, "DevOps"),
        new DepartamentoJpa(3L, "Producto"),
        new DepartamentoJpa(4L, "QA"),
        new DepartamentoJpa(5L, "Datos")
    );

    // Tabla empleados (FK departamento_id)
    private static final List<EmpleadoJpa> EMPLEADOS = List.of(
        new EmpleadoJpa(1L,  "Ana García",    1L),
        new EmpleadoJpa(2L,  "Luis Martín",   1L),
        new EmpleadoJpa(3L,  "Marta López",   1L),
        new EmpleadoJpa(4L,  "Carlos Ruiz",   2L),
        new EmpleadoJpa(5L,  "Elena Torres",  2L),
        new EmpleadoJpa(6L,  "Jorge Díaz",    3L),
        new EmpleadoJpa(7L,  "Paula Vega",    3L),
        new EmpleadoJpa(8L,  "Rubén Mora",    3L),
        new EmpleadoJpa(9L,  "Sofía Pardo",   4L),
        new EmpleadoJpa(10L, "Diego Castro",  5L),
        new EmpleadoJpa(11L, "Irene Blanco",  5L)
    );

    private int queryCount = 0;

    // SELECT * FROM departamentos
    public List<DepartamentoJpa> selectAllDepartamentos() {
        queryCount++;
        System.out.println("  [SQL " + queryCount + "] SELECT * FROM departamentos");
        // Devolvemos nuevas instancias con empleados = null (LAZY no cargado)
        return DEPARTAMENTOS.stream()
            .map(d -> new DepartamentoJpa(d.getId(), d.getNombre()))
            .toList();
    }

    // SELECT * FROM empleados WHERE departamento_id = ? (lazy load individual)
    public List<EmpleadoJpa> selectEmpleadosByDepartamento(Long deptoId) {
        queryCount++;
        System.out.println("  [SQL " + queryCount + "] SELECT * FROM empleados WHERE departamento_id = " + deptoId);
        return EMPLEADOS.stream()
            .filter(e -> e.getDepartamentoId().equals(deptoId))
            .toList();
    }

    // SELECT d.*, e.* FROM departamentos d JOIN empleados e ON e.departamento_id = d.id
    // Equivale al FETCH JOIN de JPQL: SELECT DISTINCT d FROM Departamento d JOIN FETCH d.empleados
    public List<DepartamentoJpa> selectDepartamentosConEmpleados() {
        queryCount++;
        System.out.println("  [SQL " + queryCount + "] SELECT d.*, e.* FROM departamentos d");
        System.out.println("              JOIN empleados e ON e.departamento_id = d.id");

        // Agrupamos el ResultSet por departamento (lo que hace Hibernate con FETCH JOIN)
        Map<Long, DepartamentoJpa> mapa = new java.util.LinkedHashMap<>();
        for (DepartamentoJpa d : DEPARTAMENTOS) {
            mapa.put(d.getId(), new DepartamentoJpa(d.getId(), d.getNombre()));
            mapa.get(d.getId()).setEmpleados(new ArrayList<>());
        }
        for (EmpleadoJpa e : EMPLEADOS) {
            mapa.get(e.getDepartamentoId()).getEmpleados().add(e);
        }
        return new ArrayList<>(mapa.values());
    }

    public int getQueryCount() { return queryCount; }
    public void resetQueryCount() { queryCount = 0; }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpNPlusOne {
    public static void main(String[] args) {

        FakeDatabase db = new FakeDatabase();

        System.out.println("=== Demostración problema N+1 ===\n");

        // ─── VERSIÓN PROBLEMÁTICA: N+1 ───────────────────────────────────────
        System.out.println("[ PROBLEMA N+1 ]");
        System.out.println("Código equivalente en JPA:");
        System.out.println("  List<Departamento> deps = em.createQuery(");
        System.out.println("      \"SELECT d FROM Departamento d\", Departamento.class).getResultList();");
        System.out.println("  for (Departamento d : deps) {");
        System.out.println("      d.getEmpleados().size();  // ← lazy load por cada departamento");
        System.out.println("  }\n");

        System.out.println("SQL generado:");
        List<DepartamentoJpa> departamentos = db.selectAllDepartamentos(); // query 1

        int totalEmpleados = 0;
        for (DepartamentoJpa depto : departamentos) {
            // Al acceder a getEmpleados() con LAZY → Hibernate ejecuta 1 query extra por depto
            List<EmpleadoJpa> empleados = db.selectEmpleadosByDepartamento(depto.getId()); // queries 2..N+1
            depto.setEmpleados(empleados);
            totalEmpleados += empleados.size();
        }

        System.out.println("\nTotal queries ejecutadas: " + db.getQueryCount()
            + " (1 inicial + " + departamentos.size() + " lazy loads)");
        System.out.println("Empleados procesados: " + totalEmpleados);

        // ─── VERSIÓN OPTIMIZADA: FETCH JOIN ──────────────────────────────────
        System.out.println("\n" + "─".repeat(60) + "\n");
        db.resetQueryCount();

        System.out.println("[ SOLUCIÓN: FETCH JOIN ]");
        System.out.println("Código equivalente en JPA:");
        System.out.println("  List<Departamento> deps = em.createQuery(");
        System.out.println("      \"SELECT DISTINCT d FROM Departamento d JOIN FETCH d.empleados\",");
        System.out.println("      Departamento.class).getResultList();\n");

        System.out.println("SQL generado:");
        List<DepartamentoJpa> deptosConEmpleados = db.selectDepartamentosConEmpleados(); // 1 sola query

        int totalOpt = deptosConEmpleados.stream()
            .mapToInt(d -> d.getEmpleados().size())
            .sum();

        System.out.println("\nTotal queries ejecutadas: " + db.getQueryCount()
            + " (JOIN en una sola query)");
        System.out.println("Empleados procesados: " + totalOpt);

        // ─── Comparativa ─────────────────────────────────────────────────────
        System.out.println("\n" + "─".repeat(60));
        System.out.println("\n[ COMPARATIVA ]");
        System.out.printf("  %-30s %s%n", "Estrategia", "Queries a BD");
        System.out.printf("  %-30s %s%n", "-".repeat(30), "-".repeat(12));
        System.out.printf("  %-30s %d%n", "N+1 (lazy sin join)",      1 + departamentos.size());
        System.out.printf("  %-30s %d%n", "FETCH JOIN",               1);

        System.out.println("\nNota: con 100 departamentos, N+1 ejecutaría 101 queries.");
        System.out.println("Soluciones alternativas:");
        System.out.println("  @BatchSize(size=25) → agrupa lazy loads en batches (25 queries en lugar de 100)");
        System.out.println("  @EntityGraph        → especifica eager paths por query en Spring Data");

        // ─── Resultado final ──────────────────────────────────────────────────
        System.out.println("\n[ Departamentos y empleados (FETCH JOIN) ]");
        for (DepartamentoJpa d : deptosConEmpleados) {
            System.out.println("  " + d.getNombre() + " → " + d.getEmpleados());
        }
    }
}
