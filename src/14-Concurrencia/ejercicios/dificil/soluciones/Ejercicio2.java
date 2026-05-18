import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ejercicio2 {
    public static void main(String[] args) throws Exception {
        List<String> palabras = Stream.generate(() -> List.of("java","spring","kafka","docker","java","spring","java"))
            .limit(1000 / 7 + 1)
            .flatMap(List::stream)
            .limit(1000)
            .collect(Collectors.toList());

        ConcurrentHashMap<String, Integer> mapa = new ConcurrentHashMap<>();
        palabras.parallelStream().forEach(p -> mapa.merge(p, 1, Integer::sum));

        System.out.println("Frecuencias (ConcurrentHashMap):");
        new java.util.TreeMap<>(mapa).forEach((k, v) -> System.out.println("  " + k + ": " + v));
        System.out.println("Total: " + mapa.values().stream().mapToInt(Integer::intValue).sum());
    }
}
