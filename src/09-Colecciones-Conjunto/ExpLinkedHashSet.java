import java.util.LinkedHashSet;

public class ExpLinkedHashSet {



















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
