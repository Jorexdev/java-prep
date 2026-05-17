import java.util.TreeSet;

public class ExpTreeSet {



















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
