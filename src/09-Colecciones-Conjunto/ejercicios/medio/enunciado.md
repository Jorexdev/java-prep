# Colecciones Conjunto — Ejercicios Medio

Operaciones avanzadas de conjuntos, TreeSet con Comparator, hashCode/equals custom, NavigableSet.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Unión, intersección y diferencia simétrica

Implementa los tres: unión (addAll), intersección (retainAll), y diferencia simétrica (elementos en A o B pero no en ambos). Demuestra con dos conjuntos de enteros.

## Ejercicio 2 — TreeSet con Comparator de objetos

Crea un TreeSet<Empleado> con Comparator que ordene por salario. Verifica que los empleados se mantienen ordenados al insertar.

## Ejercicio 3 — hashCode + equals correcto en HashSet

Crea clase Producto(nombre, precio) con hashCode y equals basados en nombre. Demuestra que dos objetos con el mismo nombre son tratados como el mismo elemento en HashSet.

## Ejercicio 4 — Elementos únicos que aparecen una sola vez

Dada una lista de enteros con repeticiones, encuentra los que aparecen exactamente una vez usando Set.

## Ejercicio 5 — LinkedHashSet como historial de visitados

Implementa un historial de URLs visitadas: LinkedHashSet de capacidad limitada (elimina el más antiguo si supera el límite). Simula visitar 6 URLs con límite de 4.

## Ejercicio 6 — containsAll para verificar subconjunto

Verifica si el conjunto A es subconjunto de B usando containsAll. Prueba con varios casos: subconjunto real, parcial y sin relación.
