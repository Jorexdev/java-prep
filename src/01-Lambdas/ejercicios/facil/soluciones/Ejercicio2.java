import java.util.function.Predicate;

public class Ejercicio2 {

    public static void main(String[] args) {

        Predicate<String> empiezaMayuscula = s -> !s.isEmpty() && Character.isUpperCase(s.charAt(0));

        System.out.println("Java:    " + empiezaMayuscula.test("Java"));
        System.out.println("stream:  " + empiezaMayuscula.test("stream"));
        System.out.println("Kotlin:  " + empiezaMayuscula.test("Kotlin"));
        System.out.println("python:  " + empiezaMayuscula.test("python"));
    }
}
