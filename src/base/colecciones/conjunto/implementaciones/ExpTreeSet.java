package base.colecciones.conjunto.implementaciones;

import java.util.TreeSet;

public class ExpTreeSet {

    /*
        En Java, los TreeSet son una implementación de Set basada en un árbol rojo-negro
        (Red-Black Tree). Mantiene los elementos en orden ascendente de forma natural
        o según un comparador definido.
     */


    /*                           CONSTRUCTORES                             */

    /*
        - TreeSet()
            → Crea un TreeSet vacío que ordena los elementos por orden natural.

        - TreeSet(Collection<? extends E> c)
            → Crea un TreeSet que contiene los elementos de la colección dada,
              ordenados naturalmente.

        - TreeSet(Comparator<? super E> comparator)
            → Crea un TreeSet que usará un comparador personalizado para ordenar.
     */


    /*                             KEY FEATURES                            */

    /*
        - Basado en un árbol rojo-negro (balanced binary search tree).
        - No permite duplicados.
        - No permite null si hay más de un elemento (NullPointerException en comparación).
        - Mantiene los elementos ordenados:
            → Por orden natural (Comparable).
            → O por un Comparator proporcionado.
        - Operaciones logarítmicas: búsqueda, inserción y eliminación O(log n).
        - No está sincronizado.
     */


    /*                            VENTAJAS                                 */

    /*
        - Mantiene elementos siempre ordenados.
        - Ideal para operaciones de rango y búsqueda por orden.
        - Permite comparadores personalizados.
     */


    /*                           DESVENTAJAS                               */

    /*
        - Más lento que HashSet/LinkedHashSet (O(log n) vs O(1)).
        - No permite nulls si se requiere comparación.
        - No está sincronizado.
     */


    public static void main(String[] args) {

        // Crear un TreeSet vacío
        TreeSet<String> treeSet = new TreeSet<>();

        // add(E e): añade elementos en orden natural
        treeSet.add("Spring");
        treeSet.add("Java");
        treeSet.add("Hibernate");
        treeSet.add("Java"); // duplicado → no se añade
        System.out.println("TreeSet tras add(): " + treeSet); // [Hibernate, Java, Spring]

        // contains(Object o): verifica si existe un elemento
        System.out.println("¿Contiene 'Spring'? " + treeSet.contains("Spring"));

        // first(): obtiene el primer elemento (mínimo)
        System.out.println("Primer elemento: " + treeSet.first());

        // last(): obtiene el último elemento (máximo)
        System.out.println("Último elemento: " + treeSet.last());

        // higher(E e): devuelve el siguiente elemento mayor que el dado
        System.out.println("higher('Java'): " + treeSet.higher("Java"));

        // lower(E e): devuelve el elemento inmediatamente menor
        System.out.println("lower('Java'): " + treeSet.lower("Java"));

        // subSet(from, to): devuelve un subconjunto entre dos valores
        System.out.println("subSet('Hibernate', 'Spring'): " + treeSet.subSet("Hibernate", "Spring"));

        // remove(Object o): elimina el elemento
        treeSet.remove("Hibernate");
        System.out.println("Tras remove('Hibernate'): " + treeSet);

        // clear(): elimina todos los elementos
        treeSet.clear();
        System.out.println("Tras clear(): vacío=" + treeSet.isEmpty() + ", size=" + treeSet.size());


        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - headSet(E toElement): devuelve todos los elementos menores a toElement.
        // - tailSet(E fromElement): devuelve todos los elementos mayores o iguales a fromElement.
        // - descendingSet(): devuelve una vista del conjunto en orden inverso.
        // - comparator(): devuelve el Comparator usado, o null si usa orden natural.
    }
}
