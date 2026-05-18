import java.util.*;
import java.util.stream.*;

public class Ejercicio1 {

    static Collector<String, ?, Map<Integer, List<String>>> agruparPorLongitud() {
        return Collector.of(
            HashMap::new,
            (map, s) -> map.computeIfAbsent(s.length(), k -> new ArrayList<>()).add(s),
            (m1, m2) -> { m2.forEach((k, v) -> m1.merge(k, v, (l1, l2) -> { l1.addAll(l2); return l1; })); return m1; },
            map -> map
        );
    }

    public static void main(String[] args) {
        List<String> palabras = List.of("hi", "hola", "hey", "adios", "bye", "java", "go", "python", "c");
        Map<Integer, List<String>> agrupadas = palabras.stream().collect(agruparPorLongitud());
        new TreeMap<>(agrupadas).forEach((len, lista) ->
            System.out.println("longitud " + len + ": " + lista));
    }
}
