import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ejercicio3 {

    static class Empleado implements Comparable<Empleado> {
        final String nombre;
        final String departamento;
        final double salario;
        final int antiguedad;

        Empleado(String nombre, String departamento, double salario, int antiguedad) {
            this.nombre = nombre;
            this.departamento = departamento;
            this.salario = salario;
            this.antiguedad = antiguedad;
        }

        @Override public int compareTo(Empleado o) {
            int cmp = Double.compare(o.salario, this.salario);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(o.antiguedad, this.antiguedad);
            if (cmp != 0) return cmp;
            return this.nombre.compareTo(o.nombre);
        }

        @Override public String toString() {
            return String.format("%-8s %-12s %.0f€ %dy", nombre, departamento, salario, antiguedad);
        }

        static final Comparator<Empleado> POR_DEPARTAMENTO = Comparator.comparing(e -> e.departamento);
        static final Comparator<Empleado> POR_NOMBRE       = Comparator.comparing(e -> e.nombre);
        static final Comparator<Empleado> POR_SALARIO      = Comparator.comparingDouble((Empleado e) -> e.salario).reversed();
    }

    public static void main(String[] args) {
        List<Empleado> empleados = new ArrayList<>(List.of(
            new Empleado("Zara",   "Ventas",      55000, 3),
            new Empleado("Ana",    "Ingeniería",  70000, 5),
            new Empleado("Marcos", "Ventas",      55000, 7),
            new Empleado("Bea",    "Ingeniería",  80000, 2),
            new Empleado("Luis",   "Marketing",   60000, 4)
        ));

        List<Empleado> copia1 = new ArrayList<>(empleados);
        copia1.sort(null);
        System.out.println("Orden natural (salario↓, antigüedad↓, nombre):");
        copia1.forEach(e -> System.out.println("  " + e));

        List<Empleado> copia2 = new ArrayList<>(empleados);
        copia2.sort(Empleado.POR_DEPARTAMENTO);
        System.out.println("Por departamento:");
        copia2.forEach(e -> System.out.println("  " + e));
    }
}
