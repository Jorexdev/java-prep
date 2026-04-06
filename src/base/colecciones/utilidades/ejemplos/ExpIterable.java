package base.colecciones.utilidades.ejemplos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExpIterable {

    /*
        Iterable es la superinterfaz de todas las colecciones del JCF.
        Define el método iterator() y permite que se pueda usar foreach.
     */


    /*                             KEY FEATURES                            */

    /*
        - Método iterator(): devuelve un Iterator.
        - Gracias a Iterable, podemos usar el bucle for-each.
        - Todas las colecciones estándar implementan Iterable.
     */


    public static void main(String[] args) {

        List<String> lista = new ArrayList<>(List.of("Java", "Spring", "Hibernate"));

        // Uso de iterator() directamente
        Iterator<String> it = lista.iterator();
        while (it.hasNext()) {
            String elem = it.next();
            System.out.println("iterator() → " + elem);
        }

        // Uso de foreach gracias a Iterable
        for (String elem : lista) {
            System.out.println("foreach → " + elem);
        }
    }
}
