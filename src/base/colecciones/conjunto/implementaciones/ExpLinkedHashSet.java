package base.colecciones.conjunto.implementaciones;

import java.util.LinkedHashSet;

public class ExpLinkedHashSet {

    /*
        En Java, los LinkedHashSet son una implementación de Set que combina una
        tabla hash con una lista doblemente enlazada. Esto permite mantener el
        orden de inserción de los elementos.
     */


    /*                           CONSTRUCTORES                             */

    /*
        - LinkedHashSet()
            → Crea un LinkedHashSet vacío con capacidad inicial de 16 y factor de carga 0.75.

        - LinkedHashSet(int initialCapacity)
            → Crea un LinkedHashSet con la capacidad inicial especificada.

        - LinkedHashSet(int initialCapacity, float loadFactor)
            → Crea un LinkedHashSet con capacidad y factor de carga personalizados.

        - LinkedHashSet(Collection<? extends E> c)
            → Crea un LinkedHashSet que contiene los elementos de la colección dada,
              eliminando duplicados y manteniendo el orden de inserción.
     */


    /*                             KEY FEATURES                            */

    /*
        - Basado en tabla hash + lista doblemente enlazada.
        - No permite duplicados.
        - Permite un único valor null.
        - Mantiene el orden de inserción.
        - Rendimiento similar a HashSet (inserción/búsqueda O(1)).
        - No está sincronizado.
     */


    /*                            VENTAJAS                                 */

    /*
        - Mantiene el orden en que se insertaron los elementos.
        - Operaciones rápidas de búsqueda, inserción y eliminación (O(1)).
        - Permite un null.
     */


    /*                           DESVENTAJAS                               */

    /*
        - Uso extra de memoria por la lista doblemente enlazada.
        - No está sincronizado.
     */


    public static void main(String[] args) {

        // Crear un LinkedHashSet vacío
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();

        // add(E e): añade elemento al set, mantiene orden de inserción
        linkedHashSet.add("Java");
        linkedHashSet.add("Spring");
        linkedHashSet.add("Hibernate");
        linkedHashSet.add("Java"); // duplicado → no se añade
        System.out.println("LinkedHashSet tras add(): " + linkedHashSet);

        // contains(Object o): verifica si existe un elemento
        System.out.println("¿Contiene 'Spring'? " + linkedHashSet.contains("Spring"));

        // remove(Object o): elimina el elemento
        linkedHashSet.remove("Hibernate");
        System.out.println("Tras remove('Hibernate'): " + linkedHashSet);

        // size(): número de elementos
        System.out.println("Tamaño: " + linkedHashSet.size());

        // iterator(): recorre los elementos en orden de inserción
        System.out.print("Recorrido: ");
        for (String s : linkedHashSet) {
            System.out.print(s + " ");
        }
        System.out.println();

        // clear(): elimina todos los elementos
        linkedHashSet.clear();
        System.out.println("Tras clear(): vacío=" + linkedHashSet.isEmpty() + ", size=" + linkedHashSet.size());


        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - addAll(Collection c): añadir todos los elementos de otra colección.
        // - removeAll(Collection c): eliminar elementos de otra colección.
        // - retainAll(Collection c): conservar solo los elementos comunes.
        // - toArray(): convertir a array.
        // - clone(): devuelve una copia superficial del LinkedHashSet.
    }
}
