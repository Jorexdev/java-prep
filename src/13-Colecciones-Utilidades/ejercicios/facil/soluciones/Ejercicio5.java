import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ejercicio5 {

    static class Empleado {
        private final String nombre;
        private final String departamento;

        Empleado(String nombre, String departamento) {
            this.nombre = nombre;
            this.departamento = departamento;
        }

        String getNombre()       { return nombre; }
        String getDepartamento() { return departamento; }

        @Override public String toString() { return departamento + "/" + nombre; }
    }

    public static void main(String[] args) {
        List<Empleado> empleados = new ArrayList<>(List.of(
            new Empleado("Zara",   "Ventas"),
            new Empleado("Ana",    "Ingeniería"),
            new Empleado("Marcos", "Ventas"),
            new Empleado("Bea",    "Ingeniería"),
            new Empleado("Luis",   "Marketing")
        ));

        empleados.sort(
            Comparator.comparing(Empleado::getDepartamento)
                      .thenComparing(Empleado::getNombre)
        );
        empleados.forEach(System.out::println);
    }
}
