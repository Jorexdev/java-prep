# Streams — Ejercicios Nivel Medio

Ejercicios de nivel intermedio que combinan varias operaciones de Streams:
`groupingBy` anidado, `partitioningBy`, `toMap`, `reducing`, `DoubleSummaryStatistics`...

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Agrupamiento y promedio por categoría

Dada una lista de productos con nombre, categoría y precio, agrupa los productos por categoría y calcula el precio promedio de cada categoría.

## Ejercicio 2 — Agrupamiento anidado

Dada una lista de empleados con nombre, departamento y cargo, agrupa a los empleados primero por departamento y luego por cargo.

## Ejercicio 3 — Top N elementos por grupo

Dada una lista de estudiantes con nombre, curso y nota, obtén los 3 estudiantes con mayor nota por cada curso.

## Ejercicio 4 — Detectar duplicados

Dada una lista de enteros, encuentra todos los números que aparecen más de una vez usando Streams.

## Ejercicio 5 — Aplanar estructura anidada y agrupar

Dada una lista de personas donde cada una tiene una lista de libros leídos, genera un mapa con cada libro y la cantidad de personas que lo han leído.

## Ejercicio 6 — Resumen estadístico

Dada una lista de números decimales, genera un resumen con la media, suma, mínimo, máximo y cantidad total de elementos.

## Ejercicio 7 — Concatenar campos únicos

Dada una lista de empleados, concatena en un solo String los nombres de los departamentos únicos, separados por coma.

## Ejercicio 8 — Agrupación con condición

Dada una lista de personas con edad, agrúpalas en dos grupos: mayores de edad y menores de edad.

## Ejercicio 9 — Transformación de mapa con recolección avanzada

Dada una lista de pedidos con cliente y monto, construye un mapa donde la clave sea el nombre del cliente y el valor sea el total acumulado de sus pedidos.

## Ejercicio 10 — Lista ordenada de elementos únicos por campo

Dada una lista de personas con nombre y edad, obtén una lista ordenada (por edad descendente) de nombres únicos de las personas mayores de 30 años.

## Ejercicio 11 — Producto más caro por categoría

Dada una lista de productos con nombre, categoría y precio, encuentra el producto más caro de cada categoría. Si una categoría no tiene productos, debe reflejarse como ausente (usa Optional).
