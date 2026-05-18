# Ejercicios — Streams (Difícil)

Las soluciones están en [soluciones/](soluciones/).

## Ejercicio 1 — Collector personalizado
Implementa un `Collector<String, ?, Map<Integer, List<String>>>` que agrupe strings por longitud.
Usa `Collector.of()` con supplier, accumulator, combiner y finisher.

## Ejercicio 2 — Stream paralelo con rendimiento
Compara la suma de un millón de enteros con stream() vs parallelStream(). Mide tiempo con System.nanoTime(). Implementa también una suma con reduce() y con IntStream.sum(). Explica cuándo parallelStream puede ser más lento.

## Ejercicio 3 — Streams anidados: matriz transpuesta
Dada una matriz (List<List<Integer>>), transpónla usando streams. La transpuesta de una matriz NxM es una matriz MxN donde elemento[i][j] → elemento[j][i].

## Ejercicio 4 — Estadísticas con IntSummaryStatistics
Dada una lista de transacciones (tipo, importe), usa IntStream/DoubleSummaryStatistics para calcular min, max, suma, media, conteo — todo en una pasada. Agrupa también por tipo con Collectors.summarizingDouble.

## Ejercicio 5 — teeing Collector (Java 12+)
Usa Collectors.teeing() para dividir una lista de números en dos resultados simultáneamente: los que son pares (lista) y la suma de los impares (int). Todo en una sola pasada del stream.

## Ejercicio 6 — Pipeline complejo: índice invertido
Dado un List<String> donde cada string es "docId:palabra1 palabra2 ...", construye un índice invertido Map<String, List<String>> donde la clave es la palabra y el valor es la lista de docIds que la contienen. Usa flatMap, groupingBy y sorting.
