# Colecciones Conjunto — Ejercicios Difícil

SetMultimap, broken hashCode, NavigableSet API, power set.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — SetMultimap

Implementa SetMultimap<K,V> usando Map<K,Set<V>>. Métodos: put(K,V), get(K)→Set<V>, remove(K,V), containsEntry(K,V). Demuestra que no permite duplicados para la misma clave.

## Ejercicio 2 — Broken hashCode sin equals

Crea clase Punto(int x, int y) con equals pero SIN hashCode. Demuestra que HashSet no detecta duplicados aunque equals diga que son iguales. Luego añade hashCode y muestra que funciona.

## Ejercicio 3 — NavigableSet API

Usa TreeSet como NavigableSet: floor(x), ceiling(x), headSet(x), tailSet(x), subSet(a,b), pollFirst(), pollLast(). Demuestra con un TreeSet de enteros.

## Ejercicio 4 — Power set de un Set

Dado un Set<Integer> pequeño, genera su power set (todos los subconjuntos posibles) como Set<Set<Integer>>. Para {1,2,3} el resultado tiene 8 subconjuntos.
