package base.colecciones.conjunto.implementaciones;

import java.util.concurrent.ConcurrentSkipListSet;

public class ExpConcurrentSkipListSet {











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
