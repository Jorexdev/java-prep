import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExpComparableVsComparator {

    // Comparable: la propia clase define su orden natural implementando compareTo
    // permite usar Collections.sort() y TreeSet/TreeMap sin Comparator externo
    static class Persona implements Comparable<Persona> {

        String nombre;
        int edad;

        Persona(String nombre, int edad) {
            this.nombre = nombre;
            this.edad = edad;
        }

        @Override
        public int compareTo(Persona otra) {
            return Integer.compare(this.edad, otra.edad); // orden natural: edad ascendente
            // devuelve negativo si menor, 0 si igual, positivo si mayor
        }

        @Override
        public String toString() { return nombre + "(" + edad + ")"; }
    }

    public static void main(String[] args) {

        List<Persona> personas = new ArrayList<>(List.of(
                new Persona("Ana",   30),
                new Persona("Luis",  25),
                new Persona("Marta", 35),
                new Persona("Alba",  25)
        ));

        // Comparable: usa el compareTo de la clase — orden por edad asc
        Collections.sort(personas);
        System.out.println("Comparable (edad asc): " + personas);

        // Comparator con lambda: criterio externo sin modificar la clase
        personas.sort(Comparator.comparing(p -> p.nombre));
        System.out.println("Comparator (nombre asc): " + personas);

        // Comparator compuesto: edad como criterio principal, nombre como desempate
        personas.sort(
                Comparator.comparingInt((Persona p) -> p.edad)
                        .thenComparing(p -> p.nombre) // thenComparing añade criterio secundario
        );
        System.out.println("Comparator compuesto (edad, nombre): " + personas);

        // reverseOrder() invierte el orden natural (Comparable)
        personas.sort(Comparator.reverseOrder());
        System.out.println("ReverseOrder (edad desc): " + personas);

        // Strings: orden natural (mayúsculas antes) vs case-insensitive con Comparator
        List<String> textos = new ArrayList<>(List.of("java", "Spring", "hibernate", "JPA"));
        Collections.sort(textos);
        System.out.println("Strings orden natural: " + textos);

        textos.sort(Comparator.comparing(String::toLowerCase));
        System.out.println("Strings case-insensitive: " + textos);
    }
}
