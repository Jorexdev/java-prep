package base.colecciones.coladoble;

public class DequeIntro {

    /*
        En este paquete se encuentra la introducción a las Deque junto a sus
        implementaciones más relevantes, con explicaciones y ejemplos.
        No se incluirán todos los métodos de cada implementación, pero sí los más importantes.
     */


    /*                              DEQUE IMPLEMENTATIONS                              */

    /*
        Una Deque (Double Ended Queue) es una cola de doble extremo que permite
        inserción y eliminación de elementos tanto por el frente como por la cola.

        Puede usarse como:
        - Cola (FIFO: First In, First Out).
        - Pila (LIFO: Last In, First Out).

        INTERFACE BASE
        - Deque              → extiende Queue; define operaciones en ambos extremos.

        IMPLEMENTACIONES COMUNES
        - ArrayDeque         → basada en array circular, alto rendimiento; no permite null.
        - LinkedList         → lista doblemente enlazada; también implementa Deque.
        - ConcurrentLinkedDeque → implementación concurrente, no bloqueante (lock-free).

        OTROS RELACIONADOS
        - BlockingDeque (en java.util.concurrent) → soporta operaciones bloqueantes.

        En este paquete encontrarás clases de ejemplo con uso y diferencias:
        - ExpArrayDeque         (más rápida en la mayoría de los casos)
        - ExpLinkedListDeque    (Deque basado en nodos enlazados)
        - ExpConcurrentLinkedDeque (versión thread-safe, lock-free)
    */
}
