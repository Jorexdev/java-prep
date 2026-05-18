import java.util.Comparator;
import java.util.TreeSet;

public class Ejercicio2 {

    static class Empleado {
        private final String nombre;
        private final double salario;

        Empleado(String nombre, double salario) {
            this.nombre = nombre;
            this.salario = salario;
        }

        double getSalario() { return salario; }
        String getNombre()  { return nombre; }

        @Override
        public String toString() {
            return nombre + "(" + salario + "€)";
        }
    }

    public static void main(String[] args) {
        TreeSet<Empleado> empleados = new TreeSet<>(
                Comparator.comparingDouble(Empleado::getSalario)
        );

        empleados.add(new Empleado("María",  45_000));
        empleados.add(new Empleado("Carlos", 62_000));
        empleados.add(new Empleado("Ana",    38_000));
        empleados.add(new Empleado("Pedro",  55_000));

        System.out.println("Empleados ordenados por salario (menor a mayor):");
        empleados.forEach(e -> System.out.printf("  %-10s %.0f€%n", e.getNombre(), e.getSalario()));
    }
}
