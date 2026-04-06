package base.colecciones.conjunto.implementaciones;

import java.util.HashSet;

public class ExpHashSet {

    /*
        En Java, los HashSet son una implementación de Set que almacenan los elementos
        en una tabla hash. No permiten duplicados y no garantizan el orden de los elementos.
     */


    /*                           CONSTRUCTORES                             */

    /*
        - HashSet()
            → Crea un HashSet vacío con capacidad inicial de 16 y factor de carga 0.75.

        - HashSet(int initialCapacity)
            → Crea un HashSet con capacidad inicial especificada y factor de carga 0.75.

        - HashSet(int initialCapacity, float loadFactor)
            → Crea un HashSet con capacidad y factor de carga personalizados.

        - HashSet(Collection<? extends E> c)
            → Crea un HashSet que contiene los elementos de la colección dada
              (elimina duplicados automáticamente).
     */


    /*                             KEY FEATURES                            */

    /*
        - Basado en tabla hash.
        - No permite elementos duplicados.
        - Permite un único valor null.
        - No garantiza el orden de los elementos.
        - Acceso e inserción promedio en O(1).
        - No está sincronizado (usar Collections.synchronizedSet() para seguridad en hilos).
     */


    /*                            VENTAJAS                                 */

    /*
        - Operaciones de búsqueda, inserción y eliminación muy rápidas (O(1) promedio).
        - Ideal para cuando necesitas garantizar que no haya duplicados.
        - Permite un null.
     */


    /*                           DESVENTAJAS                               */

    /*
        - No mantiene orden de inserción.
        - Iteración puede ser más lenta que en listas si necesitas acceso posicional.
        - No está sincronizado.
     */


    public static void main(String[] args) {

        // Crear un HashSet vacío
        HashSet<String> hashSet = new HashSet<>();

        // add(E e): añade un elemento al conjunto (si existe no lo añade)
        hashSet.add("Java");
        hashSet.add("Spring");
        hashSet.add("Hibernate");
        hashSet.add("Java"); // duplicado → no se añade
        System.out.println("HashSet tras add(): " + hashSet);

        // contains(Object o): devuelve true si existe el elemento
        System.out.println("¿Contiene 'Spring'? " + hashSet.contains("Spring"));

        // remove(Object o): elimina el elemento si existe
        hashSet.remove("Hibernate");
        System.out.println("Tras remove('Hibernate'): " + hashSet);

        // size(): devuelve el número de elementos
        System.out.println("Tamaño: " + hashSet.size());

        // isEmpty(): devuelve true si está vacío
        System.out.println("¿Está vacío? " + hashSet.isEmpty());

        // iterator(): permite recorrer los elementos
        System.out.print("Elementos del HashSet: ");
        for (String s : hashSet) {
            System.out.print(s + " ");
        }
        System.out.println();

        // clear(): elimina todos los elementos
        hashSet.clear();
        System.out.println("Tras clear(): vacío=" + hashSet.isEmpty() + ", size=" + hashSet.size());


        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - addAll(Collection c): añade todos los elementos de otra colección.
        // - removeAll(Collection c): elimina los elementos que están en la colección dada.
        // - retainAll(Collection c): conserva solo los elementos que están también en otra colección.
        // - toArray(): convierte el set en un array Object[].
        // - clone(): devuelve una copia superficial del HashSet.
    }
}
