package base.colecciones.listaa.implementaciones;

import java.util.Vector;

public class ExpVector {

    /*
        En Java, los Vector son arrays dinámicos que crecen automáticamente cuando
        alcanzan su capacidad. Fueron introducidos en las primeras versiones de Java
        (antes de Collections Framework) y más tarde fueron adaptados para implementar
        la interfaz List.

        A diferencia de ArrayList, los métodos de Vector están sincronizados, lo cual
        hace que sean seguros en entornos multi-hilo, aunque en la práctica suelen ser
        más lentos que ArrayList en entornos de un solo hilo.
     */


    /*                           CONSTRUCTORES                             */

    /*
        - Vector()
            → Crea un vector vacío con capacidad inicial de 10.

        - Vector(int initialCapacity)
            → Crea un vector con la capacidad inicial especificada.

        - Vector(int initialCapacity, int capacityIncrement)
            → Crea un vector con capacidad inicial y un incremento definido
              (cuánto crecerá al llenarse).

        - Vector(Collection<? extends E> c)
            → Crea un vector que contiene todos los elementos de la colección dada.
     */


    /*                             KEY FEATURES                            */

    /*
        - Vector es un array dinámico redimensionable.
        - Acceso indexado (se puede acceder por índice).
        - Soporte de genéricos (type-safety en compilación).
        - Está sincronizado → seguro para hilos.
        - Permite nulos y duplicados.
        - Mantiene el orden de inserción.
     */


    /*                            VENTAJAS                                 */

    /*
        - Tamaño dinámico.
        - Seguridad en entornos multi-hilo (synchronized).
        - Acceso indexado rápido (O(1)).
        - Collection ordenada.
        - Soporta nulos y duplicados.
     */


    /*                           DESVENTAJAS                               */

    /*
        - Menor rendimiento en entornos de un solo hilo (por sincronización).
        - Considerado "legacy" (se prefiere ArrayList en la mayoría de casos).
        - Iterar suele ser más lento que en ArrayList.
     */


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
