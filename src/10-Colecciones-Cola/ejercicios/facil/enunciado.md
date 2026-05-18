# Colecciones Cola — Ejercicios Fácil

Operaciones básicas con Queue (ArrayDeque como FIFO), PriorityQueue, diferencia poll vs remove.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Queue FIFO con ArrayDeque

Usa ArrayDeque como Queue FIFO: offer para añadir, poll para extraer, peek para ver sin extraer. Añade 5 elementos y extráelos todos mostrando el orden FIFO.

## Ejercicio 2 — Cola de atención al cliente

Simula una cola de atención: los clientes llegan con offer() y son atendidos con poll(). Muestra el orden de atención.

## Ejercicio 3 — PriorityQueue orden natural

Inserta 6 enteros en una PriorityQueue. Extráelos todos con poll(). Muestra que salen en orden ascendente (min-heap).

## Ejercicio 4 — poll() vs remove()

Demuestra la diferencia: poll() retorna null en una Queue vacía, remove() lanza NoSuchElementException.

## Ejercicio 5 — Iterar Queue sin vaciarla

Dado un Queue con 5 elementos, itéralos con for-each para imprimirlos sin modificar la cola. Luego verifica que todos los elementos siguen presentes.

## Ejercicio 6 — PriorityQueue orden descendente

Crea una PriorityQueue con Comparator.reverseOrder() para extraer los elementos de mayor a menor.
