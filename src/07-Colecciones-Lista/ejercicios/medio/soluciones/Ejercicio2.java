import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Ejercicio2 {

    public static List<String> eliminarDuplicados(List<String> lista) {
        Set<String> vistos = new LinkedHashSet<>();
        for (String elemento : lista) {
            vistos.add(elemento);
        }
        return new ArrayList<>(vistos);
    }

    public static void main(String[] args) {
        List<String> conDuplicados = new ArrayList<>(
            List.of("java", "python", "java", "go", "python", "rust", "java", "go")
        );

        System.out.println("Lista original:  " + conDuplicados);
        List<String> sinDuplicados = eliminarDuplicados(conDuplicados);
        System.out.println("Sin duplicados:  " + sinDuplicados);
    }
}
