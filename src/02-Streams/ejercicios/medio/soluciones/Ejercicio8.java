import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Ejercicio8 {
    record Empleado(String nombre, double salario, String departamento) {}

    public static void main(String[] args) {
        List<Empleado> empleados = List.of(
            new Empleado("Zara",   75000, "Ingeniería"),
            new Empleado("Ana",    60000, "Marketing"),
            new Empleado("Luis",   80000, "Ingeniería"),
            new Empleado("Marta",  55000, "Marketing"),
            new Empleado("Carlos", 70000, "Ingeniería"),
            new Empleado("Bea",    65000, "Ventas"),
            new Empleado("Jorge",  72000, "Ventas")
        );

        System.out.println("Top-3 por salario:");
        empleados.stream()
            .sorted(Comparator.comparingDouble(Empleado::salario).reversed()
                              .thenComparing(Empleado::nombre))
            .limit(3)
            .forEach(e -> System.out.printf("  %-8s %.0f€%n", e.nombre(), e.salario()));

        System.out.println("Mayor salario por departamento:");
        Map<String, Optional<Empleado>> topPorDepto = empleados.stream()
            .collect(Collectors.groupingBy(Empleado::departamento,
                     Collectors.maxBy(Comparator.comparingDouble(Empleado::salario))));
        topPorDepto.forEach((depto, emp) ->
            System.out.printf("  %-15s → %s (%.0f€)%n",
                depto, emp.map(Empleado::nombre).orElse(""), emp.map(Empleado::salario).orElse(0.0)));
    }
}
