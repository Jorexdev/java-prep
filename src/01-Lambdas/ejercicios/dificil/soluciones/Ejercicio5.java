import java.util.Comparator;
import java.util.List;

public class Ejercicio5 {

    static class Empleado {
        private final String nombre;
        private final String departamento;
        private final double salario;

        Empleado(String nombre, String departamento, double salario) {
            this.nombre = nombre;
            this.departamento = departamento;
            this.salario = salario;
        }

        public String getNombre()       { return nombre; }
        public String getDepartamento() { return departamento; }
        public double getSalario()      { return salario; }

        @Override
        public String toString() {
            return String.format("%-10s %-12s %.0f€", nombre, departamento, salario);
        }
    }

    public static void main(String[] args) {

        List<Empleado> empleados = List.of(
            new Empleado("Ana",    "Ventas",    55000),
            new Empleado("Luis",   "Tech",       72000),
            new Empleado("Marta",  "Ventas",    61000),
            new Empleado("Jorge",  "Tech",       72000),
            new Empleado("Carla",  "RRHH",      48000),
            new Empleado("Pedro",  "Tech",       68000)
        );

        // Ordenar: 1º departamento alfabético, 2º salario descendente, 3º nombre alfabético
        // Todo con referencias a métodos — sin lambdas
        Comparator<Empleado> orden = Comparator
            .comparing(Empleado::getDepartamento)
            .thenComparing(Comparator.comparingDouble(Empleado::getSalario).reversed())
            .thenComparing(Empleado::getNombre);

        System.out.println("Nombre     Departamento Salario");
        System.out.println("---------- ------------ -------");
        empleados.stream()
                 .sorted(orden)
                 .forEach(System.out::println);
    }
}
