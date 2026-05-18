# Ejercicios — Streams (Medio)

Las soluciones están en [soluciones/](soluciones/).

## Ejercicio 1 — flatMap
Dada una lista de listas de strings, aplana todas en una sola lista usando flatMap. También aplana una lista de frases en palabras individuales.

## Ejercicio 2 — Collectors.groupingBy
Agrupa una lista de personas (nombre, ciudad) por ciudad. Muestra cuántas personas hay por ciudad y los nombres de cada ciudad.

## Ejercicio 3 — Collectors.toMap
Convierte una lista de productos (nombre, precio) en un Map<String, Double> nombre→precio. Maneja el caso de claves duplicadas con mergeFunction.

## Ejercicio 4 — Collectors.joining
Une una lista de strings con separador ", " y con prefijo/sufijo "[" y "]". También genera un CSV de una lista de personas (nombre, edad).

## Ejercicio 5 — Collectors.counting + summingInt + averagingInt
Sobre una lista de pedidos (cliente, importe), calcula: número total de pedidos, suma de importes, media de importes, todo usando colectores.

## Ejercicio 6 — peek para debugging
Usa peek() para loggear el estado intermedio de un pipeline: filtra números pares, duplica, filtra > 10. Muestra los valores en cada etapa.

## Ejercicio 7 — Stream.of + Stream.iterate + Stream.generate
Crea streams infinitos: Stream.iterate para Fibonacci (primeros 10), Stream.generate para números aleatorios (5 distintos entre 1-100).

## Ejercicio 8 — Comparator con Streams
Ordena una lista de empleados (nombre, salario, departamento) por salario descendente, luego por nombre. Extrae top-3. Encuentra el empleado con mayor salario de cada departamento.
