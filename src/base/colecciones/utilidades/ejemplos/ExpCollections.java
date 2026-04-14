package base.colecciones.utilidades.ejemplos;

import java.util.*;

public class ExpCollections {















    public static void main(String[] args) {

        List<String> lista = new ArrayList<>(List.of("Java", "Spring", "Hibernate", "JPA"));

        // sort(List): ordena en orden natural
        Collections.sort(lista);
        System.out.println("sort → " + lista); // [Hibernate, JPA, Java, Spring]

        // reverse(List): invierte el orden
        Collections.reverse(lista);
        System.out.println("reverse → " + lista);

        // shuffle(List): mezcla aleatoriamente (top 10 mentiras)
        Collections.shuffle(lista);
        System.out.println("shuffle → " + lista);

        // binarySearch(List, key): busca en lista ordenada
        Collections.sort(lista);
        int idx = Collections.binarySearch(lista, "Java");
        System.out.println("binarySearch(\"Java\") → índice " + idx);

        // max / min
        System.out.println("max → " + Collections.max(lista));
        System.out.println("min → " + Collections.min(lista));

        // frequency: cuenta ocurrencias
        System.out.println("frequency(\"Java\") → " + Collections.frequency(lista, "Java"));

        // disjoint: true si no comparten elementos
        List<String> otra = List.of("Docker", "Kubernetes");
        System.out.println("disjoint con otra lista → " + Collections.disjoint(lista, otra));

        // unmodifiableList: lista inmutable (lanzará UnsupportedOperationException al modificar)
        List<String> inmutable = Collections.unmodifiableList(lista);
        System.out.println("unmodifiableList → " + inmutable);
        try {
            inmutable.add("nuevo");
        } catch (UnsupportedOperationException e) {
            System.out.println("No se puede modificar unmodifiableList");
        }

        // singletonList: lista con un solo elemento
        List<String> singleton = Collections.singletonList("único");
        System.out.println("singletonList → " + singleton);

        // emptyList: lista vacía inmutable
        List<String> vacia = Collections.emptyList();
        System.out.println("emptyList → " + vacia);

        // synchronizedList: envoltorio thread-safe
        List<String> sincronizada = Collections.synchronizedList(new ArrayList<>(lista));
        synchronized (sincronizada) { // acceso sincronizado
            System.out.println("synchronizedList → " + sincronizada);
        }

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - checkedList/checkedSet/checkedMap: devuelven colecciones con comprobación de tipo en tiempo de ejecución.
        // - rotate(List, distance): rota los elementos una distancia dada.
        // - replaceAll(List, oldVal, newVal): reemplaza todas las ocurrencias de un valor.
        // - nCopies(int, obj): lista inmutable con n copias del mismo objeto.
    }
}
