import java.util.*;
import java.util.stream.*;

public class ExpCollectors {

    public static void main(String[] args) {

        List<Empleado> empleados = List.of(
                new Empleado("Ana",     "Ingeniería",  85_000.0),
                new Empleado("Luis",    "Ingeniería",  92_000.0),
                new Empleado("Marta",   "Marketing",   60_000.0),
                new Empleado("Carlos",  "Marketing",   65_000.0),
                new Empleado("Elena",   "RRHH",        55_000.0),
                new Empleado("Jorex",   "Ingeniería",  110_000.0),
                new Empleado("Sofia",   "RRHH",        58_000.0),
                new Empleado("Pablo",   "Marketing",   70_000.0)
        );

        // ======================================
        // 1. groupingBy — agrupa por criterio en un Map<K, List<V>>
        // ======================================

        Map<String, List<Empleado>> porDpto = empleados.stream()
                .collect(Collectors.groupingBy(Empleado::departamento));
        System.out.println("Departamentos: " + porDpto.keySet());

        // groupingBy con downstream: contar por departamento
        Map<String, Long> countPorDpto = empleados.stream()
                .collect(Collectors.groupingBy(
                        Empleado::departamento,
                        Collectors.counting()));
        System.out.println("Empleados por dpto: " + countPorDpto);

        // groupingBy con downstream: salario medio por departamento
        Map<String, Double> mediaPorDpto = empleados.stream()
                .collect(Collectors.groupingBy(
                        Empleado::departamento,
                        Collectors.averagingDouble(Empleado::salario)));
        mediaPorDpto.forEach((dpto, media) ->
                System.out.printf("  %s → media %.0f€%n", dpto, media));

        // ======================================
        // 2. partitioningBy — divide en dos grupos: true / false
        // ======================================

        // treatedBudget: salario < 70k = bajo presupuesto
        Map<Boolean, List<Empleado>> particion = empleados.stream()
                .collect(Collectors.partitioningBy(e -> e.salario() < 70_000.0));
        System.out.println("Bajo presupuesto: " +
                particion.get(true).stream().map(Empleado::nombre).toList());
        System.out.println("Alto presupuesto: " +
                particion.get(false).stream().map(Empleado::nombre).toList());

        // ======================================
        // 3. toMap — con función de merge para claves duplicadas
        // ======================================

        // Nombre → salario máximo por nombre (en caso de duplicado, se queda el mayor)
        Map<String, Double> maxPorNombre = empleados.stream()
                .collect(Collectors.toMap(
                        Empleado::nombre,
                        Empleado::salario,
                        Double::max));   // merge function: resuelve colisiones de clave
        System.out.println("toMap nombre→salario: " + maxPorNombre.size() + " entradas");

        // ======================================
        // 4. joining — concatenar strings con delimitador, prefijo y sufijo
        // ======================================

        String nombresJoined = empleados.stream()
                .map(Empleado::nombre)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining: " + nombresJoined);

        // ======================================
        // 5. counting y summarizingInt/Double
        // ======================================

        long total = empleados.stream().collect(Collectors.counting());
        System.out.println("counting: " + total);

        DoubleSummaryStatistics stats = empleados.stream()
                .collect(Collectors.summarizingDouble(Empleado::salario));
        System.out.printf("summarizingDouble → min=%.0f max=%.0f avg=%.0f%n",
                stats.getMin(), stats.getMax(), stats.getAverage());

        // ======================================
        // 6. collectingAndThen — colectar y luego transformar el resultado
        // ======================================

        // Produce una lista no modificable directamente desde el collector
        List<String> inmodificable = empleados.stream()
                .map(Empleado::nombre)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList));
        System.out.println("collectingAndThen (inmodificable): " + inmodificable.getClass().getSimpleName());

        // ======================================
        // DEMO FINAL — estadísticas de salario por departamento
        // ======================================

        System.out.println("\n--- Stats de salario por departamento ---");
        empleados.stream()
                .collect(Collectors.groupingBy(
                        Empleado::departamento,
                        Collectors.summarizingDouble(Empleado::salario)))
                .forEach((dpto, s) ->
                        System.out.printf("  %-15s count=%d  min=%.0f  max=%.0f  avg=%.0f%n",
                                dpto, s.getCount(), s.getMin(), s.getMax(), s.getAverage()));
    }

    record Empleado(String nombre, String departamento, double salario) {}
}
