package base.colecciones.conjunto;

/*
    SET - Introducción

    ¿Qué es?
    Colección que no permite valores duplicados.
    No ofrece acceso posicional. El orden depende de la implementación.

    ¿Para qué sirve?
    Cuando necesitas unicidad: eliminar duplicados, comprobar pertenencia rápida,
    o mantener un conjunto de elementos únicos.

    Implementaciones principales:

    HashSet
    - Basado en tabla hash. Sin orden definido.
    - Búsqueda, inserción y borrado O(1) amortizado.
    - La opción por defecto cuando solo te importa la unicidad.

    LinkedHashSet
    - HashSet + lista doblemente enlazada.
    - Mantiene el orden de inserción.
    - Útil cuando necesitas unicidad y reproducibilidad de orden.

    TreeSet
    - Basado en árbol rojo-negro.
    - Mantiene los elementos ordenados (orden natural o Comparator).
    - Operaciones O(log n). Implementa NavigableSet.

    EnumSet
    - Especializado para enums. Internamente usa un bitset: muy eficiente.
    - Siempre usar EnumSet cuando el conjunto es de valores de un enum.

    SortedSet / NavigableSet
    - Interfaces para conjuntos ordenados con navegación:
      floor, ceiling, headSet, tailSet, subSet, descendingSet...

    ConcurrentSkipListSet
    - Implementación concurrente de NavigableSet basada en skip list.
    - Thread-safe y ordenada. O(log n).

    Preguntas típicas de entrevista:
    - ¿Cómo determina HashSet si dos objetos son iguales?
    - ¿Qué pasa si dos objetos tienen el mismo hashCode pero equals() devuelve false?
    - ¿Cuándo usarías TreeSet sobre HashSet?
    - ¿Qué contrato deben cumplir equals() y hashCode() para que HashSet funcione bien?
*/
