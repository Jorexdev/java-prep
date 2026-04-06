package base.colecciones.utilidades;

/*
    UTILIDADES DEL JCF - Introducción

    ¿Qué son?
    Clases e interfaces de soporte del Java Collections Framework.
    No son colecciones en sí mismas, sino herramientas para trabajar con ellas.

    Clase Collections
    - Métodos estáticos de utilidad: sort, binarySearch, reverse, shuffle,
      min, max, frequency, disjoint, unmodifiableX, synchronizedX, emptyX...
    - La navaja suiza del JCF.

    Iterable
    - Superinterfaz de todas las colecciones. Define iterator().
    - Permite usar for-each sobre cualquier colección.

    Iterator
    - Cursor para recorrer colecciones de forma segura.
    - hasNext(), next(), remove().
    - remove() es la única forma segura de eliminar durante la iteración.

    ListIterator
    - Extiende Iterator para listas.
    - Recorrido bidireccional y operaciones add/set durante la iteración.

    Comparable
    - Interfaz que implementa una clase para definir su orden natural.
    - Método: compareTo(T otro).
    - Necesaria para usar TreeSet/TreeMap o Collections.sort() sin Comparator.

    Comparator
    - Interfaz funcional para definir criterios de ordenación externos.
    - Permite múltiples órdenes para la misma clase sin modificarla.
    - Se usa con lambdas: Comparator.comparing(), thenComparing(), reversed()...

    Enumeration
    - Interfaz legacy anterior al JCF (pre-Java 2).
    - Usada en Vector y Hashtable. Prefiere Iterator en código nuevo.

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre Collections.sort() y List.sort()?
    - ¿Qué devuelve Collections.unmodifiableList()? ¿Es inmutable de verdad?
    - ¿Qué diferencia hay entre Comparable y Comparator?
    - ¿Qué diferencia hay entre Iterator y Enumeration?
*/
