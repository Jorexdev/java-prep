import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    public static void main(String[] args) {

        List<String>  strings  = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();

        // En runtime, ambas tienen el mismo tipo borrado: class java.util.ArrayList
        System.out.println(strings.getClass());           // class java.util.ArrayList
        System.out.println(integers.getClass());          // class java.util.ArrayList
        System.out.println(strings.getClass() == integers.getClass()); // true

        // NO se puede hacer instanceof con tipo parametrizado:
        // if (strings instanceof List<String>) { } // ERROR de compilación

        // Sí se puede con wildcard (comprobación sin parámetro de tipo):
        Object obj = strings;
        if (obj instanceof List<?>) {
            System.out.println("Es una List (sin importar el tipo de elemento)");
        }

        // Por eso no se puede crear un array genérico: new T[10] no compila
        // El compilador elimina <T> en bytecode → solo queda Object
    }
}
