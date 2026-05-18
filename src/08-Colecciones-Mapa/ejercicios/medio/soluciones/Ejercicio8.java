import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class Ejercicio8 {

    public static void main(String[] args) {
        // Comparator: primero por longitud, luego alfabéticamente si igual longitud
        Comparator<String> porLongitud = Comparator
            .comparingInt(String::length)
            .thenComparing(Comparator.naturalOrder());

        Map<String, Integer> mapa = new TreeMap<>(porLongitud);
        mapa.put("sol", 1);
        mapa.put("luna", 2);
        mapa.put("tierra", 3);
        mapa.put("marte", 4);
        mapa.put("venus", 5);
        mapa.put("io", 6);
        mapa.put("saturno", 7);

        System.out.println("TreeMap con Comparator por longitud de clave:");
        mapa.forEach((k, v) ->
            System.out.println("  [" + k.length() + "] " + k + " -> " + v)
        );
    }
}
