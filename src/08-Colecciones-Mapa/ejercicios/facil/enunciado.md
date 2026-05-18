# Colecciones Mapa — Ejercicios Fácil

Operaciones básicas con HashMap, LinkedHashMap, TreeMap: put/get/remove, getOrDefault, iteración, frecuencia.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — HashMap básico con getOrDefault
Crea un `HashMap<String, Integer>` con nombres y edades. Busca una clave existente y una inexistente usando `getOrDefault`.

## Ejercicio 2 — Iterar con entrySet y forEach
Crea un mapa de países y capitales. Itera con `entrySet()` en un for-each y también con `forEach(BiConsumer)`.

## Ejercicio 3 — Frecuencia de palabras
Dado un array de palabras, cuenta cuántas veces aparece cada una usando un HashMap.

## Ejercicio 4 — putIfAbsent
Demuestra `putIfAbsent`: inicializa un contador solo si la clave no existe. Intenta añadir la misma clave dos veces y verifica que el valor no se sobreescribió.

## Ejercicio 5 — TreeMap ordenado por clave
Crea un TreeMap con 5 palabras como claves. Muestra las entradas en orden alfabético natural.

## Ejercicio 6 — Convertir dos listas a Map
Dadas dos listas paralelas (nombres, edades), conviértelas a `Map<String, Integer>` con un Stream y `Collectors.toMap`.

## Ejercicio 7 — Eliminar entradas con removeIf
Dado un `Map<String, Integer>`, elimina todas las entradas donde el valor sea menor que 5 usando `entrySet().removeIf()`.

## Ejercicio 8 — HashMap vs LinkedHashMap
Inserta 5 elementos en un HashMap y en un LinkedHashMap. Muestra ambos e indica qué diferencia se observa en el orden.
