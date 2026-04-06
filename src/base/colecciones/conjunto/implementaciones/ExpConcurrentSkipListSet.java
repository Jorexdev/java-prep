package base.colecciones.conjunto.implementaciones;

import java.util.concurrent.ConcurrentSkipListSet;

public class ExpConcurrentSkipListSet {

    /*
        En Java, ConcurrentSkipListSet es una implementación concurrente de NavigableSet.
        Está basada en un Skip List (lista con saltos), lo que permite operaciones
        eficientes en orden O(log n) y es segura para múltiples hilos (thread-safe).
     */


    /*                           CONSTRUCTORES                             */

    /*
        - ConcurrentSkipListSet()
            → Crea un conjunto vacío en orden natural.

        - ConcurrentSkipListSet(Comparator<? super E> comparator)
            → Crea un conjunto vacío con el comparador dado.

        - ConcurrentSkipListSet(Collection<? extends E> c)
            → Crea un conjunto con los elementos de la colección.
     */


    /*                             KEY FEATURES                            */

    /*
        - Implementa NavigableSet.
        - Basado en Skip List → operaciones O(log n).
        - Orden natural o definido por Comparator.
        - Thread-safe (sincronización eficiente, no bloquea como synchronized).
        - No permite null.
     */


    public static void main(String[] args) {
        ConcurrentSkipListSet<String> csls = new ConcurrentSkipListSet<>();

        // add(E e): añade elementos en orden natural
        csls.add("Spring");
        csls.add("Java");
        csls.add("Hibernate");
        csls.add("Microservices");
        System.out.println("ConcurrentSkipListSet → " + csls); // [Hibernate, Java, Microservices, Spring]

        // contains(Object o): verifica existencia
        System.out.println("contains('Java'): " + csls.contains("Java")); // true

        // first() / last()
        System.out.println("first(): " + csls.first()); // Hibernate
        System.out.println("last(): " + csls.last());   // Spring

        // pollFirst() / pollLast()
        System.out.println("pollFirst(): " + csls.pollFirst()); // Hibernate
        System.out.println("pollLast(): " + csls.pollLast());   // Spring
        System.out.println("Después de polls → " + csls); // [Java, Microservices]

        // descendingSet(): vista en orden inverso
        System.out.println("descendingSet(): " + csls.descendingSet());
    }
}
