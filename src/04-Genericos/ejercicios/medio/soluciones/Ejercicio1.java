import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    // PECS — Producer Extends, Consumer Super
    // src PRODUCE elementos → usamos ? extends T (podemos leer T o subtipo de T)
    // dest CONSUME elementos → usamos ? super T (acepta T o cualquier supertipo)
    static <T> void copiar(List<? super T> dest, List<? extends T> src) {
        for (T elemento : src) {
            dest.add(elemento);
        }
    }

    public static void main(String[] args) {
        List<Integer> origen = List.of(1, 2, 3, 4, 5);

        // Number es supertipo de Integer → List<Number> satisface List<? super Integer>
        List<Number> destino = new ArrayList<>();

        copiar(destino, origen);
        System.out.println("Destino tras copiar: " + destino); // [1, 2, 3, 4, 5]

        // También funciona con Long → Integer (ambos extienden Number)
        List<Long> longs = List.of(10L, 20L, 30L);
        copiar(destino, longs);
        System.out.println("Destino con longs también: " + destino);
    }
}
