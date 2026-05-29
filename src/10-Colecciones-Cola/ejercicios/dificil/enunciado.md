# Colecciones Cola — Ejercicios Difícil

Scheduler por tiempo, buffer limitado, Dijkstra simplificado, cola multinivel.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Scheduler de tareas por tiempo

Implementa un scheduler: las tareas tienen un tiempo de ejecución planificado (timestamp). Usa PriorityQueue ordenada por timestamp. Simula el paso del tiempo y ejecuta las tareas en el momento correcto.

## Ejercicio 2 — Buffer de mensajes con capacidad limitada

Implementa un buffer de mensajes de capacidad N usando ArrayDeque. Los nuevos mensajes se añaden al final. Si el buffer está lleno, elimina el mensaje más antiguo antes de añadir el nuevo.

## Ejercicio 3 — Dijkstra simplificado

Implementa el algoritmo de Dijkstra para encontrar el camino más corto desde un nodo origen en un grafo ponderado de 5 nodos. Usa PriorityQueue<int[]> donde int[0]=nodo, int[1]=distancia.

## Ejercicio 4 — Cola multinivel (HIGH/MEDIUM/LOW)

Implementa una cola de prioridad con 3 niveles usando 3 ArrayDeque internas. offer(msg, prioridad) añade al deque correcto. poll() retorna siempre del nivel HIGH primero, luego MEDIUM, luego LOW.

## Ejercicio 5 — TaskScheduler con prioridad y delay

Implementa `TaskScheduler` con `PriorityQueue<Task>` donde `Task` tiene `nombre`, `prioridad` (int, mayor = antes) y `executeAt` (timestamp en ms). `schedule(task, delayMs)` calcula el timestamp y encola la tarea. `runPending()` ejecuta todas las tareas cuyo `executeAt <= System.currentTimeMillis()`, en orden de prioridad descendente. Demo: programa 10 tareas con distintas prioridades y delays (0-500ms), llama `runPending()` en un bucle hasta que todas se ejecuten, imprimiendo nombre, prioridad y retardo real.
