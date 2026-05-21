# Ejercicios — 12 Colecciones Concurrentes
## Fácil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — ConcurrentHashMap basics**
Crea un `ConcurrentHashMap<String, Integer>` con las claves "a", "b" y "c".
Lanza 5 threads, cada uno incrementa cada clave 1000 veces usando `merge(key, 1, Integer::sum)`.
Al finalizar, imprime los valores de las tres claves y verifica que cada una suma exactamente 5000.

---

**Ejercicio 2 — putIfAbsent**
Simula una cache de usuarios: `ConcurrentHashMap<Integer, String>`.
3 threads intentan insertar el usuario con id=1 al mismo tiempo con nombres distintos ("Alice", "Bob", "Carol").
Usa `putIfAbsent` para que solo uno tenga éxito.
Imprime qué nombre quedó en la cache y cuántas inserciones reales ocurrieron.

---

**Ejercicio 3 — computeIfAbsent**
Tienes una `ConcurrentHashMap<String, List<String>>` que agrupa items por categoría ("A", "B", "C").
10 threads añaden items a las categorías usando `computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(value)`.
Cada thread añade 3 items (uno por categoría).
Imprime el tamaño de cada lista al final (debe ser 10 cada una).

---

**Ejercicio 4 — CopyOnWriteArrayList**
Crea una `CopyOnWriteArrayList<String>` con 5 observers iniciales.
Lanza 3 threads que iteran la lista e imprimen cada elemento con un sleep(1ms) entre elementos.
Simultáneamente, otro thread añade un nuevo observer mientras los otros iteran.
Demuestra que no se lanza `ConcurrentModificationException`.

---

**Ejercicio 5 — ConcurrentLinkedQueue**
Crea una `ConcurrentLinkedQueue<Integer>`.
Un thread producer añade los números del 1 al 20 con pequeñas pausas (sleep(5ms) entre cada uno).
Un thread consumer hace `poll()` en bucle hasta haber consumido los 20 elementos.
Imprime cada elemento consumido en orden y el total al final.

---

**Ejercicio 6 — AtomicInteger**
Compara dos contadores: uno `AtomicInteger` y uno `int` normal (con campo `volatile`).
10 threads incrementan cada contador 1000 veces.
Imprime los valores finales de ambos.
El `AtomicInteger` debe dar exactamente 10000; el `int` normal probablemente dará un valor menor.
