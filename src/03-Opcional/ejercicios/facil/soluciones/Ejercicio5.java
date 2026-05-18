import java.util.Optional;

public class Ejercicio5 {

    public static void main(String[] args) {
        // Edad que cumple el filtro
        Optional<Integer> edad20 = Optional.of(20);
        Optional<Integer> mayorDeEdad20 = edad20.filter(e -> e >= 18);
        System.out.println("Edad 20 — tras filter(>=18): " + mayorDeEdad20); // Optional[20]

        // Edad que NO cumple el filtro → Optional vacío
        Optional<Integer> edad15 = Optional.of(15);
        Optional<Integer> mayorDeEdad15 = edad15.filter(e -> e >= 18);
        System.out.println("Edad 15 — tras filter(>=18): " + mayorDeEdad15); // Optional.empty

        // Uso práctico con orElse
        String resultado20 = mayorDeEdad20.map(e -> "Mayor de edad (" + e + ")").orElse("Menor de edad");
        String resultado15 = mayorDeEdad15.map(e -> "Mayor de edad (" + e + ")").orElse("Menor de edad");

        System.out.println("\nResultado edad 20: " + resultado20);
        System.out.println("Resultado edad 15: " + resultado15);
    }
}
