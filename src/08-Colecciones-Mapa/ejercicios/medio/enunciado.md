# Colecciones Mapa — Ejercicios Medio

Ejercicios intermedios: computeIfAbsent, merge, inversión de mapa, compute, hashCode/equals custom, caché LRU.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — computeIfAbsent para agrupar
Usa `computeIfAbsent` para agrupar palabras por su primera letra en un `Map<Character, List<String>>`.

## Ejercicio 2 — merge para combinar frecuencias
Tienes dos mapas de frecuencias de palabras. Usa `merge()` para combinarlos sumando los valores de claves comunes.

## Ejercicio 3 — Invertir mapa
Dado `Map<String, Integer>` nombre→edad, invierte a `Map<Integer, List<String>>` edad→[nombres] (varios nombres pueden tener la misma edad).

## Ejercicio 4 — compute para contador de visitas
Usa `compute(key, (k,v) -> ...)` para actualizar un contador de visitas a URLs. Si la clave no existe, inicializa en 1.

## Ejercicio 5 — hashCode/equals y HashMap
Crea clase `Punto(int x, int y)` con `equals` correcto pero SIN `hashCode`. Demuestra que el HashMap no funciona correctamente. Luego añade `hashCode` y muestra que funciona.

## Ejercicio 6 — Caché LRU con LinkedHashMap
Implementa una caché LRU de tamaño 3 usando `LinkedHashMap` con `accessOrder=true` y `removeEldestEntry`. Demuestra la política de evicción.

## Ejercicio 7 — Clave con valor máximo
Dado `Map<String, Integer>` persona→puntuación, encuentra la persona con mayor puntuación usando Stream sobre entrySet.

## Ejercicio 8 — TreeMap con Comparator por longitud
Crea un `TreeMap<String, Integer>` con un `Comparator` que ordene las claves por su longitud (y alfabéticamente si igual longitud). Inserta palabras de diferente longitud y muestra el orden.
