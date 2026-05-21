import java.util.List;
import java.util.stream.Collectors;

// @RestController
// @RequestMapping("/api")
public class Ejercicio2 {

    static class Empleado {
        String nombre;
        String depto;

        Empleado(String nombre, String depto) {
            this.nombre = nombre;
            this.depto = depto;
        }

        @Override
        public String toString() {
            return "Empleado{nombre='" + nombre + "', depto='" + depto + "'}";
        }
    }

    static final List<Empleado> empleados = List.of(
        new Empleado("Ana García", "IT"),
        new Empleado("Luis Martín", "HR"),
        new Empleado("Sara López", "IT"),
        new Empleado("Pedro Ruiz", "HR"),
        new Empleado("Marta Díaz", "Finance")
    );

    // @GetMapping("/empleados")
    static List<Empleado> buscarPorDepto(/* @RequestParam */ String depto) {
        return empleados.stream()
            .filter(e -> e.depto.equalsIgnoreCase(depto))
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println("Departamento IT:");
        buscarPorDepto("IT").forEach(e -> System.out.println("  " + e));

        System.out.println("Departamento HR:");
        buscarPorDepto("HR").forEach(e -> System.out.println("  " + e));
    }
}
