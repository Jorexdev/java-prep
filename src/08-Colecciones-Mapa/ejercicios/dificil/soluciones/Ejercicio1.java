import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    static class Documento {
        int id;
        List<String> palabras;

        Documento(int id, List<String> palabras) {
            this.id = id;
            this.palabras = palabras;
        }
    }

    public static void main(String[] args) {
        List<Documento> documentos = List.of(
            new Documento(1, List.of("java", "streams", "lambda")),
            new Documento(2, List.of("java", "colecciones", "mapa")),
            new Documento(3, List.of("python", "lambda", "funcional")),
            new Documento(4, List.of("java", "spring", "mapa", "colecciones"))
        );

        // Construir índice invertido: palabra -> [ids de documentos que la contienen]
        Map<String, List<Integer>> indice = new HashMap<>();
        for (Documento doc : documentos) {
            for (String palabra : doc.palabras) {
                indice.computeIfAbsent(palabra, k -> new ArrayList<>()).add(doc.id);
            }
        }

        System.out.println("Índice invertido:");
        indice.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.println("  '" + e.getKey() + "' -> docs " + e.getValue()));

        System.out.println("\n¿Qué documentos contienen 'java'? " + indice.get("java"));
        System.out.println("¿Qué documentos contienen 'lambda'? " + indice.get("lambda"));
    }
}
