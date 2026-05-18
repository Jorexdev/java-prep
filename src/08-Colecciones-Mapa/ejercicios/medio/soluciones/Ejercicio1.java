import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    public static void main(String[] args) {
        List<String> palabras = List.of("sol", "luna", "silla", "llave", "mesa", "lago", "mapa", "sombra");

        Map<Character, List<String>> porLetra = new HashMap<>();
        for (String palabra : palabras) {
            porLetra.computeIfAbsent(palabra.charAt(0), k -> new ArrayList<>()).add(palabra);
        }

        System.out.println("Palabras agrupadas por primera letra:");
        porLetra.forEach((letra, lista) ->
            System.out.println("  '" + letra + "' -> " + lista)
        );
    }
}
