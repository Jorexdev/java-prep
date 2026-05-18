import java.util.*;
import java.util.stream.*;

public class Ejercicio6 {
    public static void main(String[] args) {
        List<String> documentos = List.of(
            "doc1:java spring kafka",
            "doc2:java python docker",
            "doc3:kafka docker kubernetes",
            "doc4:spring security jwt",
            "doc5:java kafka streaming"
        );

        Map<String, List<String>> indice = documentos.stream()
            .flatMap(doc -> {
                String[] partes  = doc.split(":", 2);
                String docId     = partes[0];
                String[] palabras = partes[1].split(" ");
                return Arrays.stream(palabras).map(p -> Map.entry(p, docId));
            })
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ));

        new TreeMap<>(indice).forEach((palabra, docs) ->
            System.out.printf("%-12s → %s%n", palabra, docs));
    }
}
