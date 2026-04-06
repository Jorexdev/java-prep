package base.colecciones.curiosidades.diferencias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
    COMPARABLE VS COMPARATOR

    Comparable
    - Interfaz que implementa la propia clase para definir su orden natural.
    - Método: compareTo(T otro)
    - Si implementas Comparable, Collections.sort() y TreeSet/TreeMap funcionan sin Comparator.

    Comparator
    - Interfaz externa para definir un criterio de ordenación sin modificar la clase.
    - Método: compare(T o1, T o2)
    - Permite múltiples órdenes distintos para la misma clase.
    - Es una interfaz funcional: se puede usar con lambdas.

    ¿Cuándo usar cada uno?
    - Comparable: cuando hay un orden natural obvio y único para la clase (ej: edad, fecha).
    - Comparator: cuando necesitas múltiples criterios o no puedes modificar la clase.

    Preguntas típicas de entrevista:
    - ¿Qué devuelve compareTo? (negativo si menor, 0 si igual, positivo si mayor)
    - ¿Puedes usar TreeSet con una clase que no implementa Comparable? (sí, pasando Comparator)
    - ¿Qué pasa si compareTo es inconsistente con equals()?
    - ¿Cómo encadenas criterios de ordenación con Comparator?
*/
public class ComparableVsComparator {

    /*
        Comparable implementado en la clase: define el orden natural por edad.
        Collections.sort() usará este compareTo automáticamente.
    */
    static class Persona implements Comparable<Persona> {
        String nombre;
        int edad;

        Persona(String n, int e) { this.nombre = n; this.edad = e; }

        @Override
        public int compareTo(Persona otra) {
            return Integer.compare(this.edad, otra.edad); // orden natural por edad asc
        }

        @Override
        public String toString() { return nombre + "(" + edad + ")"; }
    }

    public static void main(String[] args) {
        List<Persona> personas = new ArrayList<>(List.of(
                new Persona("Ana", 30),
                new Persona("Luis", 25),
                new Persona("Marta", 35),
                new Persona("Alba", 25)
        ));

        // Comparable: orden natural definido en la clase (por edad asc)
        Collections.sort(personas);
        System.out.println("Comparable (edad asc): " + personas);

        // Comparator con lambda: orden por nombre alfabético
        personas.sort(Comparator.comparing(p -> p.nombre));
        System.out.println("Comparator (nombre asc): " + personas);

        // Comparator compuesto: primero por edad, luego por nombre como desempate
        personas.sort(
                Comparator.comparingInt((Persona p) -> p.edad)
                        .thenComparing(p -> p.nombre)
        );
        System.out.println("Comparator compuesto (edad, luego nombre): " + personas);

        // reverseOrder(): invierte el orden natural (Comparable)
        personas.sort(Comparator.reverseOrder());
        System.out.println("ReverseOrder (edad desc): " + personas);

        // Strings: orden natural vs case-insensitive
        List<String> textos = new ArrayList<>(List.of("java", "Spring", "hibernate", "JPA"));
        Collections.sort(textos); // orden natural (mayúsculas antes que minúsculas)
        System.out.println("Strings orden natural: " + textos);

        textos.sort(Comparator.comparing(String::toLowerCase)); // case-insensitive
        System.out.println("Strings case-insensitive: " + textos);
    }
}
