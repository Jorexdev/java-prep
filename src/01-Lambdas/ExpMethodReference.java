import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class ExpMethodReference {

    public static void main(String[] args) {

        // ======================================
        // 1. REFERENCIA A MÉTODO ESTÁTICO
        //    Clase::metodoEstatico
        // ======================================

        // Lambda equivalente: x -> Math.sqrt(x)
        Function<Double, Double> raiz = Math::sqrt;
        System.out.println("sqrt(16) = " + raiz.apply(16.0));

        // Lambda equivalente: (a, b) -> Integer.compare(a, b)
        Comparator<Integer> cmpInt = Integer::compare;
        List<Integer> nums = new ArrayList<>(List.of(5, 2, 8, 1, 9));
        nums.sort(cmpInt);
        System.out.println("sort con Integer::compare: " + nums);

        // ======================================
        // 2. REFERENCIA A MÉTODO DE INSTANCIA (sobre la instancia capturada)
        //    instancia::metodo
        // ======================================

        String prefijo = "Hola, ";
        // Lambda equivalente: s -> prefijo.concat(s)
        UnaryOperator<String> saludar = prefijo::concat;
        System.out.println(saludar.apply("Java"));

        // Lambda equivalente: () -> System.out.println(...)
        Runnable log = System.out::println; // 'out' es la instancia capturada
        // (este caso se usa típicamente con lambdas de un solo argumento)

        // ======================================
        // 3. REFERENCIA A MÉTODO DE INSTANCIA (sobre tipo arbitrario)
        //    Tipo::metodoDeInstancia — el primer argumento actúa como receptor
        // ======================================

        // Lambda equivalente: s -> s.toUpperCase()
        Function<String, String> aMayus = String::toUpperCase;
        System.out.println("toUpperCase: " + aMayus.apply("java"));

        // Lambda equivalente: (a, b) -> a.compareToIgnoreCase(b)
        // El primer parámetro es el receptor, el segundo es el argumento
        Comparator<String> cmpIgnoreCase = String::compareToIgnoreCase;
        List<String> tecnologias = new ArrayList<>(
                List.of("Spring", "kafka", "Docker", "java", "Maven"));
        tecnologias.sort(cmpIgnoreCase);
        System.out.println("sort case-insensitive: " + tecnologias);

        // ======================================
        // 4. REFERENCIA A CONSTRUCTOR
        //    Clase::new
        // ======================================

        // Lambda equivalente: () -> new ArrayList<>()
        Supplier<ArrayList<String>> factoryLista = ArrayList::new;
        ArrayList<String> nuevaLista = factoryLista.get();
        nuevaLista.add("creada vía constructor reference");
        System.out.println("ArrayList::new: " + nuevaLista);

        // Lambda equivalente: nombre -> new Producto(nombre)
        Function<String, Producto> factoryProducto = Producto::new;
        Producto p = factoryProducto.apply("Laptop");
        System.out.println("Producto::new: " + p);

        // ======================================
        // DEMO FINAL — pipeline con los 4 tipos
        // ======================================

        List<String> nombres = List.of("alice", "BOB", "Charlie", "diana", "EVE");

        List<String> resultado = nombres.stream()
                .sorted(String::compareToIgnoreCase)  // tipo 3: instancia sobre tipo arbitrario
                .map(String::toUpperCase)              // tipo 3: instancia sobre tipo arbitrario
                .collect(Collectors.toList());

        // tipo 2: instancia capturada (System.out)
        resultado.forEach(System.out::println);

        // tipo 1: estático — comprobar si algún nombre tiene longitud > 5
        boolean hayLargo = nombres.stream()
                .mapToInt(String::length)              // tipo 3
                .anyMatch(n -> n > 5);
        System.out.println("¿Algún nombre > 5 chars? " + hayLargo);
    }

    static class Producto {
        private final String nombre;

        Producto(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return "Producto{" + nombre + "}";
        }
    }
}
