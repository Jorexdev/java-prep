package base.colecciones.lista;

/*
    LIST - Introducción

    ¿Qué es?
    Colección ordenada que permite valores duplicados y acceso posicional por índice.
    Mantiene el orden de inserción.

    ¿Para qué sirve?
    Cuando necesitas una secuencia de elementos donde el orden importa
    y puedes tener repetidos.

    Implementaciones principales:

    ArrayList
    - Array dinámico redimensionable.
    - Acceso aleatorio O(1), inserción/borrado en el medio O(n).
    - La opción por defecto en la mayoría de casos.

    LinkedList
    - Lista doblemente enlazada.
    - Inserción/borrado al inicio y al final O(1), acceso aleatorio O(n).
    - También implementa Deque.

    Vector
    - Como ArrayList pero sincronizado (thread-safe).
    - Legacy: preferir Collections.synchronizedList() o CopyOnWriteArrayList.

    Stack
    - Extiende Vector. Implementa LIFO (Last In, First Out).
    - Legacy: preferir Deque (ArrayDeque) como pila moderna.

    Preguntas típicas de entrevista:
    - ¿Cuándo usarías ArrayList vs LinkedList?
    - ¿Cuál es la complejidad de get(), add() y remove() en ArrayList vs LinkedList?
    - ¿Por qué Stack se considera legacy en Java moderno?
    - ¿Qué es el capacity de un ArrayList y cuándo se redimensiona?
*/
