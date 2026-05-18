import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ejercicio2 {
    public static void main(String[] args) {
        List<String> listaA = List.of("java", "python", "kotlin", "rust", "go");
        List<String> listaB = List.of("python", "ruby", "go", "swift", "java");

        Set<String> setA = new HashSet<>(listaA);
        Set<String> setB = new HashSet<>(listaB);

        System.out.println("Conjunto A: " + setA);
        System.out.println("Conjunto B: " + setB);

        setA.retainAll(setB); // mantiene solo los comunes
        System.out.println("Intersección A ∩ B: " + setA);
    }
}
