# Colecciones Mapa — Ejercicios Difícil

Ejercicios avanzados: índice invertido, broken hashCode, merge recursivo, tabla bidireccional, MultiMap, rendimiento.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Índice invertido
Dado un array de documentos (cada uno con id y lista de palabras), construye un índice invertido `Map<String, List<Integer>>` donde la clave es la palabra y el valor es la lista de IDs de documentos que la contienen.

## Ejercicio 2 — Broken hashCode mutable
Crea una clase `Clave` con campo `int valor` mutable y `hashCode` basado en ese valor. Inserta en HashMap, muta el objeto, intenta buscarlo. Demuestra por qué falla y explica la solución en comentarios.

## Ejercicio 3 — Merge recursivo de mapas
Implementa `Map<String,Object> mergeRecursivo(Map<String,Object> base, Map<String,Object> override)` que combine mapas anidados: si el valor es un Map en ambos, fusiona recursivamente; si no, el override gana.

## Ejercicio 4 — Tabla de frecuencias bidireccional
Dado un texto, construye `Map<Character, Map<Character, Integer>>` donde la primera clave es la primera letra de cada palabra y la segunda clave es la última letra, con el conteo de palabras que cumplen esa combinación.

## Ejercicio 5 — MultiMap<K, V>
Implementa clase genérica `MultiMap<K, V>` con `put(K,V)`, `get(K)→List<V>`, `getAll()→Map<K,List<V>>`, `remove(K,V)`. Internamente usa `HashMap<K, List<V>>`.

## Ejercicio 6 — Rendimiento HashMap vs TreeMap
Mide el tiempo de 100.000 inserciones y 100.000 búsquedas en HashMap vs TreeMap. Muestra los resultados e indica cuándo cada uno es preferible.
