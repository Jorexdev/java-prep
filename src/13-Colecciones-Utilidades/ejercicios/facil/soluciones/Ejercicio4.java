import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ejercicio4 {

    static class Persona {
        private final String nombre;
        private final int edad;

        Persona(String nombre, int edad) { this.nombre = nombre; this.edad = edad; }
        int getEdad()      { return edad; }
        String getNombre() { return nombre; }
        @Override public String toString() { return nombre + "(" + edad + ")"; }
    }

    public static void main(String[] args) {
        List<Persona> personas = new ArrayList<>(List.of(
            new Persona("Carlos", 35),
            new Persona("Ana",    22),
            new Persona("Luis",   28),
            new Persona("Marta",  22)
        ));

        personas.sort(Comparator.comparing(Persona::getEdad));
        System.out.println("Por edad: " + personas);
    }
}
