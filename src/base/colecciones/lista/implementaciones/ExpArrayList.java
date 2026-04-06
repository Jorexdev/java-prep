package base.colecciones.listaa.implementaciones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpArrayList {

    /*
        En Java, los ArrayList son arrays redimensionables en los cuales, a diferencia
        de los arrays normales, no necesitas especificar el tamaño por adelantado,
        puede crecer o encoger dinamicamente mientras se añaden o eliminan valores

     */


    /*                             KEY FEATURES                            */

    /*
        - ArrayList es un array redimensionable dinamico
        - Acceso indexado; se puede acceder a los valores mediante indices
        - Suporte de genericos; Asegura la seguridad del tipo en el momento de compilacion (type-safety)
        - No esta sincronizado; ArrayList usa Collections.syncronizedList() para seguridad de hilos
        - Permite nulos y duplicados
        - Matiene orden de insercion
     */

    /*                            VENTAJAS                                 */

    /*
        - Tamaño dinamico
        - Facil de usar
        - Acceso rapido
        - Collection ordenada
        - Soporta nulos
     */


    public static void main(String[] args) {

        // Para crear un ArrayList debemos especificar el objeto que almacenará dentro de <>
        // Desde Java 7 ya no es necesario repetir el tipo en el segundo <>
        ArrayList<String> arrayList = new ArrayList<>();

        // Añadimos valores del mismo tipo de dato con .add(valor)
        arrayList.add("programando");
        arrayList.add("con");
        arrayList.add("Jorge");

        // Podemos imprimir directamente los valores y aparecerán entre []
        System.out.println(arrayList); // [programando, con, Jorge]

        // Eliminar por índice
        arrayList.remove(2); // elimina "Jorge"
        System.out.println(arrayList); // [programando, con]

        // Eliminar por valor (primera ocurrencia)
        arrayList.remove("con");
        System.out.println(arrayList); // [programando]

        // Modificar por índice
        arrayList.set(0, "cocinando");
        System.out.println(arrayList); // [cocinando]

        // add(int index, E element): insertar en posición concreta
        arrayList.add(1, "Java");
        System.out.println(arrayList); // [Java, cocinando]

        // addAll(Collection<? extends E>): añadir una colección al final
        List<String> mas = List.of("con", "Jorge", "y", "amigos", "amigos");
        arrayList.addAll(mas);
        System.out.println(arrayList); // [Java, cocinando, con, Jorge, y, amigos, amigos]

        // addAll(int index, Collection): insertar colección desde un índice
        arrayList.addAll(2, List.of("en", "YouTube"));
        System.out.println(arrayList); // [Java, cocinando, en, YouTube, con, Jorge, y, amigos, amigos]

        // contains(Object o)
        System.out.println("¿Contiene 'Java'? " + arrayList.contains("Java")); // true

        // get(int index)
        System.out.println("Elemento en 3: " + arrayList.get(3)); // YouTube

        // indexOf / lastIndexOf
        System.out.println("Primera 'amigos': " + arrayList.indexOf("amigos"));     // índice de la primera
        System.out.println("Última 'amigos': " + arrayList.lastIndexOf("amigos"));  // índice de la última

        // isEmpty / size
        System.out.println("Vacía: " + arrayList.isEmpty() + ", tamaño: " + arrayList.size());

        // subList(from, to): VISTA del mismo respaldo (cambios se reflejan, cambias de una lista se cambia en ambas)
        List<String> vista = arrayList.subList(2, 5); // [en, YouTube, con]
        System.out.println("subList(2,5): " + vista);
        vista.set(0, "EN"); // modifica también arrayList
        System.out.println("Vista modificada: " + vista);
        System.out.println("ArrayList tras modificar la vista: " + arrayList);

        // removeIf(Predicate): borrar por condición
        // Eliminamos strings de longitud 1 (ej: "y")
        arrayList.removeIf(s -> s.length() == 1);
        System.out.println("Tras removeIf(len==1): " + arrayList);

        // toArray(): obtener array Object[]
        Object[] arreglo = arrayList.toArray();
        System.out.println("toArray() -> length: " + arreglo.length);

        // toArray(T[]): obtener array tipado
        String[] arregloTipado = arrayList.toArray(new String[0]);
        System.out.println("toArray(new String[0]) -> " + Arrays.toString(arregloTipado));

        // ensureCapacity(): reservar capacidad mínima (optimización)
        arrayList.ensureCapacity(100);

        // clear(): eliminar todos los elementos
        arrayList.clear();
        System.out.println("Tras clear(): vacío=" + arrayList.isEmpty() + ", size=" + arrayList.size());

        // trimToSize(): ajustar capacidad al tamaño actual (0)
        arrayList.trimToSize();


        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - removeAll(Collection c) / retainAll(Collection<?> c):
        //   útiles para operaciones de conjuntos sobre listas (diferencia e intersección).
        // - listIterator(int index): igual que listIterator() pero empezando en un índice concreto.
        // - spliterator(): para procesamientos avanzados/streams paralelos; poco usado en ejemplos básicos.
        // - removeRange(int from, int to): en ArrayList es protected (NO puedes llamarlo directamente).
        // - En Java 21+: ArrayList hereda métodos de SequencedCollection como addFirst(), addLast(),
        //   getFirst(), getLast(), removeFirst(), removeLast(); si usas versión anterior, NO existen.
    }
}
