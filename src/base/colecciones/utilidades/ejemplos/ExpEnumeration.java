package base.colecciones.utilidades.ejemplos;

import java.util.Enumeration;
import java.util.Vector;

public class ExpEnumeration {

    /*
        Enumeration es una interfaz anterior a Iterator (legacy).
        Usada con colecciones antiguas como Vector o Hashtable.
     */


    /*                             KEY FEATURES                            */

    /*
        - Métodos: hasMoreElements(), nextElement().
        - Solo permite iterar hacia adelante.
        - No tiene remove() (a diferencia de Iterator).
        - Todavía usado en código legacy.
     */


    public static void main(String[] args) {

        Vector<String> vector = new Vector<>();
        vector.add("Java");
        vector.add("Spring");
        vector.add("Hibernate");

        Enumeration<String> e = vector.elements();

        while (e.hasMoreElements()) {
            System.out.println("Enumeration → " + e.nextElement());
        }
    }
}
