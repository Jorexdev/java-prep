package base.colecciones.utilidades.ejemplos;

import java.util.Enumeration;
import java.util.Vector;

public class ExpEnumeration {

    public static void main(String[] args) {

        // Enumeration es la interfaz de iteración legacy (anterior a Iterator)
        // todavía aparece en Vector, Hashtable y APIs antiguas como Properties
        Vector<String> vector = new Vector<>();
        vector.add("Java");
        vector.add("Spring");
        vector.add("Hibernate");

        Enumeration<String> e = vector.elements(); // equivalente moderno: list.iterator()

        while (e.hasMoreElements()) {               // hasMoreElements() = hasNext()
            System.out.println(e.nextElement());    // nextElement() = next() — no tiene remove()
        }
    }
}
