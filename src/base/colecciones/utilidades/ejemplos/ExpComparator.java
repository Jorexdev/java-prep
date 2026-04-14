package base.colecciones.utilidades.ejemplos;

import java.util.*;

public class ExpComparator {







    public static void main(String[] args) {

        List<String> lista = new ArrayList<>(List.of("Java", "Spring", "Hibernate", "JPA"));

        // Comparator natural (orden alfabético)
        lista.sort(Comparator.naturalOrder());
        System.out.println("Comparator.naturalOrder(): " + lista);

        // Comparator inverso
        lista.sort(Comparator.reverseOrder());
        System.out.println("Comparator.reverseOrder(): " + lista);

        // Comparator por longitud
        lista.sort((a, b) -> Integer.compare(a.length(), b.length()));
        System.out.println("Comparator por longitud: " + lista);

        // Comparator con métod0 de referencia
        lista.sort(Comparator.comparing(String::toLowerCase));
        System.out.println("Comparator.comparing(String::toLowerCase): " + lista);
    }
}
