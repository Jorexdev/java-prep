import java.util.*;
import java.util.stream.*;

public class ExpFlatMap {

    public static void main(String[] args) {

        // ======================================
        // 1. map vs flatMap — la diferencia clave
        // ======================================

        List<String> frases = List.of("hola mundo", "java es genial", "stream api");

        // map produce Stream<String[]> — un stream de arrays, no de palabras
        Stream<String[]> conMap = frases.stream()
                .map(f -> f.split(" "));
        System.out.println("map → tipo: Stream<String[]>, elementos: " + conMap.count());

        // flatMap aplana: cada String[] se convierte en Stream<String> y se fusionan
        List<String> palabras = frases.stream()
                .flatMap(f -> Arrays.stream(f.split(" ")))
                .toList();
        System.out.println("flatMap → palabras: " + palabras);

        // ======================================
        // 2. Lista de órdenes con ítems — aplanar estructura anidada
        // ======================================

        List<Orden> ordenes = List.of(
                new Orden("O1", List.of("Laptop", "Ratón", "Teclado")),
                new Orden("O2", List.of("Monitor")),
                new Orden("O3", List.of("Auriculares", "Webcam"))
        );

        // Sin flatMap necesitaríamos dos bucles anidados
        List<String> todosLosItems = ordenes.stream()
                .flatMap(o -> o.items().stream())   // cada Orden → Stream<String> de sus ítems
                .toList();
        System.out.println("Todos los ítems: " + todosLosItems);

        // Contar ítems únicos en todas las órdenes
        long itemsUnicos = ordenes.stream()
                .flatMap(o -> o.items().stream())
                .distinct()
                .count();
        System.out.println("Ítems únicos: " + itemsUnicos);

        // ======================================
        // 3. Optional.stream() con flatMap — filtrar Optionals vacíos
        // ======================================

        List<Optional<String>> conOpcionales = List.of(
                Optional.of("Spring"),
                Optional.empty(),
                Optional.of("Kafka"),
                Optional.empty(),
                Optional.of("Docker")
        );

        // Optional.stream() devuelve Stream de 0 o 1 elemento
        // flatMap lo aplana, eliminando los vacíos de forma limpia
        List<String> soloPresentes = conOpcionales.stream()
                .flatMap(Optional::stream)
                .toList();
        System.out.println("Sin vacíos: " + soloPresentes);

        // ======================================
        // 4. Fusionar colecciones con Stream.of + flatMap
        // ======================================

        List<String> backendDevs  = List.of("Jorex", "Ana", "Miguel");
        List<String> frontendDevs = List.of("Sara", "Pablo");
        List<String> devops       = List.of("Carlos");

        List<String> todosDevs = Stream.of(backendDevs, frontendDevs, devops)
                .flatMap(Collection::stream)
                .sorted()
                .toList();
        System.out.println("Todos los devs: " + todosDevs);

        // ======================================
        // 5. DEMO FINAL — frecuencia de palabras en párrafos
        // ======================================

        List<String> parrafos = List.of(
                "java stream flatMap aplana listas",
                "flatMap es diferente de map en java",
                "stream api hace el código más limpio"
        );

        Map<String, Long> frecuencia = parrafos.stream()
                .flatMap(p -> Arrays.stream(p.split(" ")))
                .collect(Collectors.groupingBy(
                        s -> s,                    // clave = la propia palabra
                        Collectors.counting()      // valor = cuántas veces aparece
                ));

        System.out.println("\n--- Frecuencia de palabras ---");
        frecuencia.entrySet().stream()
                .filter(e -> e.getValue() > 1)    // solo las que aparecen más de una vez
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));
    }

    record Orden(String id, List<String> items) {}
}
