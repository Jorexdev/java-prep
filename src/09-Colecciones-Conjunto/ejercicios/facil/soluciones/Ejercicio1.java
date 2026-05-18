import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ejercicio1 {
    public static void main(String[] args) {
        List<String> listaOriginal = List.of("a", "b", "a", "c", "b", "d");
        System.out.println("Lista original:     " + listaOriginal);

        Set<String> sinDuplicados = new HashSet<>(listaOriginal);
        System.out.println("Conjunto resultante: " + sinDuplicados);
        System.out.println("Tamaño original: " + listaOriginal.size() + " → sin duplicados: " + sinDuplicados.size());
    }
}
