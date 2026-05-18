import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ejercicio1 {

    static class Empleado {
        private String nombre;
        private double salario;

        public Empleado(String nombre, double salario) {
            this.nombre = nombre;
            this.salario = salario;
        }

        public String getNombre() { return nombre; }
        public double getSalario() { return salario; }
    }

    public static void main(String[] args) {
        List<Empleado> empleados = new ArrayList<>();
        empleados.add(new Empleado("Ana", 45000));
        empleados.add(new Empleado("Luis", 62000));
        empleados.add(new Empleado("Marta", 38000));
        empleados.add(new Empleado("Jorge", 75000));
        empleados.add(new Empleado("Eva", 55000));

        empleados.sort(Comparator.comparingDouble(Empleado::getSalario).reversed());

        System.out.println("Empleados ordenados por salario descendente:");
        empleados.forEach(e ->
            System.out.println("  " + e.getNombre() + " — " + e.getSalario())
        );
    }
}
