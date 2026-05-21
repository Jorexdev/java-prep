import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio1 {

    // @Entity
    static class Depto {
        // @Id
        int id;
        String nombre;
        // @OneToMany(fetch = FetchType.LAZY)
        List<Empleado> empleados;

        Depto(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
            this.empleados = new ArrayList<>();
        }
    }

    // @Entity
    static class Empleado {
        // @Id
        int id;
        String nombre;
        int deptoId;

        Empleado(int id, String nombre, int deptoId) {
            this.id = id;
            this.nombre = nombre;
            this.deptoId = deptoId;
        }
    }

    static class EmpleadoRepository {
        private final Map<Integer, Depto> deptos = new HashMap<>();
        private final Map<Integer, List<Empleado>> empleadosPorDepto = new HashMap<>();
        int queryCount = 0;

        void agregarDepto(Depto d) {
            deptos.put(d.id, d);
            empleadosPorDepto.put(d.id, new ArrayList<>());
        }

        void agregarEmpleado(Empleado e) {
            empleadosPorDepto.computeIfAbsent(e.deptoId, k -> new ArrayList<>()).add(e);
        }

        List<Depto> findAllDeptos() {
            queryCount++;
            System.out.println("  [SQL] SELECT * FROM depto");
            return new ArrayList<>(deptos.values());
        }

        List<Empleado> findEmpleadosByDeptoId(int deptoId) {
            queryCount++;
            System.out.println("  [SQL] SELECT * FROM empleado WHERE depto_id=" + deptoId);
            return empleadosPorDepto.getOrDefault(deptoId, new ArrayList<>());
        }

        List<Depto> findAllDeptosConEmpleados() {
            queryCount++;
            System.out.println("  [SQL] SELECT d.*, e.* FROM depto d LEFT JOIN empleado e ON e.depto_id=d.id");
            List<Depto> resultado = new ArrayList<>(deptos.values());
            for (Depto d : resultado) {
                d.empleados = empleadosPorDepto.getOrDefault(d.id, new ArrayList<>());
            }
            return resultado;
        }
    }

    public static void main(String[] args) {

        EmpleadoRepository repo = new EmpleadoRepository();

        for (int i = 1; i <= 4; i++) {
            repo.agregarDepto(new Depto(i, "Depto-" + i));
        }
        int empId = 1;
        for (int deptoId = 1; deptoId <= 4; deptoId++) {
            for (int j = 0; j < 3; j++) {
                repo.agregarEmpleado(new Empleado(empId++, "Emp-" + empId, deptoId));
            }
        }

        System.out.println("=== Modo LAZY (N+1) ===");
        repo.queryCount = 0;
        List<Depto> deptos = repo.findAllDeptos();
        for (Depto d : deptos) {
            d.empleados = repo.findEmpleadosByDeptoId(d.id);
        }
        System.out.println("Total queries: " + repo.queryCount);

        System.out.println("\n=== Modo EAGER / FETCH JOIN ===");
        repo.queryCount = 0;
        repo.findAllDeptosConEmpleados();
        System.out.println("Total queries: " + repo.queryCount);
    }
}
