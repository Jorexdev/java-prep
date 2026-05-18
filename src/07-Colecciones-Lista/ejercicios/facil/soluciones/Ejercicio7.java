import java.util.ArrayList;
import java.util.List;

public class Ejercicio7 {
    public static void main(String[] args) {
        List<String> listaA = new ArrayList<>(List.of("Java", "Python", "Go"));
        List<String> listaB = new ArrayList<>(List.of("Rust", "Kotlin", "Swift"));

        List<String> combinada = new ArrayList<>(listaA);
        combinada.addAll(listaB);

        System.out.println("Lista A:    " + listaA);
        System.out.println("Lista B:    " + listaB);
        System.out.println("Combinada:  " + combinada);
        System.out.println("¿Contiene todo A? " + combinada.containsAll(listaA));
        System.out.println("¿Contiene todo B? " + combinada.containsAll(listaB));
    }
}
