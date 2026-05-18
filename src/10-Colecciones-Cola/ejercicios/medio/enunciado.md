# Colecciones Cola — Ejercicios Medio

PriorityQueue con objetos, top-K elementos, BFS, usar dos Queues como Stack.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Sistema de tareas con prioridad

Crea clase Tarea(nombre, prioridad) que implementa Comparable (mayor prioridad = sale antes). Usa PriorityQueue para procesar tareas en orden de prioridad.

## Ejercicio 2 — Top K elementos más pequeños

Dado un array de enteros, usa PriorityQueue para encontrar los K elementos más pequeños de forma eficiente.

## Ejercicio 3 — Planificador round-robin

Simula un planificador de procesos round-robin: cada proceso en la cola recibe un quantum de tiempo. Si no termina, vuelve al final de la cola.

## Ejercicio 4 — BFS en grafo simple

Implementa BFS (búsqueda en anchura) en un grafo no dirigido usando Queue. El grafo tiene 6 nodos y varias aristas. Muestra el orden de visita desde el nodo inicial.

## Ejercicio 5 — PriorityQueue no garantiza orden al iterar

Demuestra que iterar una PriorityQueue con for-each NO garantiza el orden de prioridad. El orden correcto solo se obtiene con poll() sucesivos.

## Ejercicio 6 — Stack con dos Queues

Implementa una Stack LIFO usando dos Queues (sin usar Stack ni Deque). push() y pop() deben funcionar correctamente.
