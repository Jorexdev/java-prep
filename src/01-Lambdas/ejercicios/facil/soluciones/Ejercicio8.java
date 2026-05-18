import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Ejercicio8 {

    public static void main(String[] args) {

        List<Empleado> empleados = new ArrayList<>(List.of(
                new Empleado("Marta"),
                new Empleado("Ana"),
                new Empleado("Zoe"),
                new Empleado("Jorge")
        ));

        // Clase anónima (forma antigua)
        // Collections.sort(empleados, new Comparator<Empleado>() {
        //     @Override
        //     public int compare(Empleado a, Empleado b) {
        //         return a.getNombre().compareTo(b.getNombre());
        //     }
        // });

        // Lambda equivalente
        Collections.sort(empleados, (a, b) -> a.getNombre().compareTo(b.getNombre()));

        empleados.forEach(e -> System.out.println(e.getNombre()));
    }

    static class Empleado {
        private final String nombre;

        Empleado(String nombre) { this.nombre = nombre; }

        public String getNombre() { return nombre; }
    }
}
