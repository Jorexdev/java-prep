import java.util.Vector;

public class ExpVector {



















    public static void main(String[] args) {

        // Crear un Vector vacío
        Vector<String> vector = new Vector<>();

        // add(E e): añade elementos al final
        vector.add("programando");
        vector.add("con");
        vector.add("Jorge");
        System.out.println(vector); // [programando, con, Jorge]

        // add(int index, E element): inserta en una posición específica
        vector.add(1, "Java");
        System.out.println(vector); // [programando, Java, con, Jorge]

        // get(int index): obtiene el elemento en un índice
        System.out.println("Elemento en 2: " + vector.get(2)); // con

        // set(int index, E element): reemplaza el valor en un índice
        vector.set(0, "cocinando");
        System.out.println("Tras set: " + vector); // [cocinando, Java, con, Jorge]

        // remove(int index): elimina el elemento en el índice
        vector.remove(1);
        System.out.println("Tras remove(1): " + vector); // [cocinando, con, Jorge]

        // remove(Object o): elimina la primera ocurrencia del objeto
        vector.remove("con");
        System.out.println("Tras remove(\"con\"): " + vector); // [cocinando, Jorge]

        // contains(Object o): devuelve true si contiene el elemento
        System.out.println("¿Contiene 'Jorge'? " + vector.contains("Jorge")); // true

        // firstElement(): obtiene el primer elemento
        System.out.println("Primer elemento: " + vector.firstElement()); // cocinando

        // lastElement(): obtiene el último elemento
        System.out.println("Último elemento: " + vector.lastElement()); // Jorge

        // size(): devuelve el número de elementos
        System.out.println("Tamaño: " + vector.size());

        // capacity(): devuelve la capacidad actual del Vector
        System.out.println("Capacidad actual: " + vector.capacity());

        // toArray(): convierte a un arreglo
        Object[] arreglo = vector.toArray();
        System.out.println("toArray() -> length: " + arreglo.length);

        // clear(): elimina todos los elementos
        vector.clear();
        System.out.println("Tras clear(): vacío=" + vector.isEmpty() + ", size=" + vector.size());

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - addAll(Collection c): añadir todos los elementos de otra colección.
        // - ensureCapacity(int minCapacity): garantiza una capacidad mínima.
        // - trimToSize(): ajusta la capacidad al tamaño actual.
        // - clone(): devuelve una copia superficial (shallow copy).
        // - elements(): devuelve un Enumeration para iterar (forma antigua antes de Iterator).
    }
}
