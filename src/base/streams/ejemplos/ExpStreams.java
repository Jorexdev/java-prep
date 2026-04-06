package base.streams.ejemplos;

import java.util.*;
import java.util.stream.Collectors;

/*
    STREAMS - Operaciones principales

    Ejemplos de las operaciones más usadas y preguntadas en entrevistas.
    Cada bloque muestra una operación con su comentario explicativo.
*/
public class ExpStreams {

    public static void main(String[] args) {
        List<String> nombres = List.of("Ana", "Luis", "Marta", "Alba", "Jorge");

        // filter: solo los elementos que cumplen el predicado
        // devuelve false = se descarta del stream
        List<String> conA = nombres.stream()
                .filter(n -> n.startsWith("A"))
                .toList();
        System.out.println("filter: " + conA);

        // map: transforma cada elemento en otro valor
        // aquí usamos method reference en lugar de lambda
        List<String> mayus = nombres.stream()
                .map(String::toUpperCase)
                .toList();
        System.out.println("map: " + mayus);

        // flatMap: aplana una lista de listas en un único stream
        // útil cuando cada elemento produce varios elementos
        List<List<String>> grupos = List.of(
                List.of("Ana", "Luis"),
                List.of("Marta", "Alba")
        );
        List<String> todos = grupos.stream()
                .flatMap(Collection::stream)
                .toList();
        System.out.println("flatMap: " + todos);

        // sorted: ordena usando un Comparator personalizado
        List<String> ordenados = nombres.stream()
                .sorted(String::compareToIgnoreCase)
                .toList();
        System.out.println("sorted: " + ordenados);

        // reduce: combina todos los elementos en uno solo
        String concatenado = nombres.stream()
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "-" + b);
        System.out.println("reduce: " + concatenado);

        // distinct + limit: elimina duplicados y toma solo los primeros N
        List<Integer> numeros = List.of(5, 1, 2, 2, 3, 5, 4);
        List<Integer> sinDuplicados = numeros.stream()
                .distinct()
                .sorted()
                .limit(4)
                .toList();
        System.out.println("distinct + sorted + limit: " + sinDuplicados);

        // skip: omite los primeros N elementos
        List<String> saltar2 = nombres.stream()
                .skip(2)
                .toList();
        System.out.println("skip(2): " + saltar2);

        // anyMatch / allMatch / noneMatch: comprueban condiciones, devuelven boolean
        // son operaciones de cortocircuito: paran en cuanto tienen la respuesta
        boolean hayAna     = nombres.stream().anyMatch(n -> n.equals("Ana"));
        boolean todosCortosNew = nombres.stream().allMatch(n -> n.length() < 10);
        System.out.println("anyMatch Ana: " + hayAna);
        System.out.println("allMatch longitud < 10: " + todosCortosNew);

        // findFirst: devuelve el primer elemento que cumple el filtro como Optional
        Optional<String> primeroConL = nombres.stream()
                .filter(n -> n.startsWith("L"))
                .findFirst();
        primeroConL.ifPresent(n -> System.out.println("findFirst con L: " + n));

        // count: número de elementos que pasan el filtro
        long cuantos = nombres.stream()
                .filter(n -> n.length() > 3)
                .count();
        System.out.println("count (longitud > 3): " + cuantos);

        // collect con groupingBy: agrupa elementos por criterio en un Map
        Map<Integer, List<String>> porLongitud = nombres.stream()
                .collect(Collectors.groupingBy(String::length));
        System.out.println("groupingBy longitud: " + porLongitud);

        // peek: operación intermedia para depurar sin modificar el stream
        // no lo uses en producción para lógica real, solo para debugging
        List<String> resultado = nombres.stream()
                .peek(n -> System.out.println("  antes filter: " + n))
                .filter(n -> n.length() > 3)
                .peek(n -> System.out.println("  despues filter: " + n))
                .toList();
        System.out.println("peek resultado: " + resultado);
    }
}
