import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class ExpOptionalAntipatterns {

    public static void main(String[] args) {

        // ======================================
        // ANTIPATRÓN 1 — get() sin isPresent()
        //   Riesgo: NoSuchElementException en runtime si el Optional está vacío
        // ======================================

        Optional<String> optVacio = Optional.empty();

        // MAL: lanza NoSuchElementException
        // String valor = optVacio.get();

        // BIEN: orElseThrow da el mismo resultado pero con mensaje de error claro
        try {
            optVacio.orElseThrow(() -> new IllegalStateException("Valor obligatorio no encontrado"));
        } catch (IllegalStateException e) {
            System.out.println("AP1 correcto: " + e.getMessage());
        }

        // BIEN: si el vacío es un caso legítimo, usar orElse o ifPresent
        String resultado = optVacio.orElse("valor-defecto");
        System.out.println("AP1 orElse: " + resultado);

        // ======================================
        // ANTIPATRÓN 2 — Optional como parámetro de método
        //   Obliga al llamador a envolver el valor; complica las llamadas
        // ======================================

        // MAL: el llamador tiene que escribir buscar(Optional.of("Ana"))
        // static List<String> buscarMal(Optional<String> nombre) { ... }

        // BIEN: usa sobrecarga (overloading)
        System.out.println("AP2 sin nombre:   " + buscar(null));
        System.out.println("AP2 con nombre:   " + buscar("Ana"));

        // ======================================
        // ANTIPATRÓN 3 — Optional dentro de colecciones
        //   List<Optional<T>> añade complejidad; simplemente omite el elemento
        // ======================================

        // MAL: obliga a desenvolver cada Optional al iterar
        List<Optional<String>> conOpcionales = List.of(
                Optional.of("Spring"), Optional.empty(), Optional.of("Kafka"));
        // Hay que flatMap o filter en cada acceso — ruido innecesario

        // BIEN: filtra los nulls antes de meter en la colección, o usa lista vacía como ausencia
        List<String> sinOpcionales = List.of("Spring", "Kafka"); // solo los valores presentes
        System.out.println("AP3 lista limpia: " + sinOpcionales);

        // Si el resultado de una búsqueda puede ser "sin resultados", devuelve List vacía
        List<String> resultados = findByPrefix("Z"); // lista vacía en vez de Optional<List>
        System.out.println("AP3 lista vacía:  " + resultados);

        // ======================================
        // ANTIPATRÓN 4 — Optional.of(null)
        //   Optional.of lanza NullPointerException; usa ofNullable para valores que pueden ser null
        // ======================================

        String posibleNull = System.getProperty("variable.inexistente"); // devuelve null

        // MAL: lanza NullPointerException
        // Optional<String> mal = Optional.of(posibleNull);

        // BIEN: ofNullable maneja el null devolviendo Optional.empty()
        Optional<String> bien = Optional.ofNullable(posibleNull);
        System.out.println("AP4 ofNullable(null): " + bien.isPresent());  // false

        // ======================================
        // ANTIPATRÓN 5 — Optional<Integer/Long/Double> para primitivos
        //   Añade boxing innecesario; hay tipos especializados en java.util
        // ======================================

        // MAL: boxing de int
        Optional<Integer> optIntBoxed = Optional.of(42);

        // BIEN: OptionalInt/Long/Double sin boxing
        OptionalInt    optInt    = OptionalInt.of(42);
        OptionalLong   optLong   = OptionalLong.of(1_000_000L);
        OptionalDouble optDouble = OptionalDouble.of(3.14);

        System.out.println("AP5 OptionalInt:    " + optInt.getAsInt());
        System.out.println("AP5 OptionalLong:   " + optLong.getAsLong());
        System.out.println("AP5 OptionalDouble: " + optDouble.getAsDouble());

        // Los tipos primitivos tienen orElse directamente sin casting
        int valor = OptionalInt.empty().orElse(0);
        System.out.println("AP5 orElse primitivo: " + valor);
    }

    // BIEN: sobrecarga en lugar de Optional como parámetro
    static List<String> buscar(String nombre) {
        List<String> todos = List.of("Ana", "Luis", "Marta");
        if (nombre == null) return todos;
        return todos.stream().filter(n -> n.equalsIgnoreCase(nombre)).toList();
    }

    // Devuelve lista vacía cuando no hay resultados — nunca null, nunca Optional<List>
    static List<String> findByPrefix(String prefijo) {
        List<String> todos = List.of("Ana", "Arturo", "Luis", "Marta");
        return todos.stream().filter(n -> n.startsWith(prefijo)).toList();
    }
}
