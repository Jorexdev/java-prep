import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio5 {

    static boolean esPalindromo(String s) {
        Deque<Character> deque = new ArrayDeque<>();
        for (char c : s.toCharArray()) deque.addLast(c);

        while (deque.size() > 1) {
            if (!deque.pollFirst().equals(deque.pollLast())) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        String[] palabras = {"reconocer", "java", "ana", "nivel", "kafka", "racecar"};

        for (String palabra : palabras) {
            System.out.printf("  %-12s → %s%n", palabra, esPalindromo(palabra) ? "palíndromo" : "no es palíndromo");
        }
    }
}
