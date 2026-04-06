package base.streams;

/*
    STREAMS - Introducción

    ¿Qué es?
    API introducida en Java 8 para procesar colecciones de datos de forma declarativa y funcional.
    Un Stream no almacena datos, describe un "pipeline" de operaciones sobre una fuente
    (colección, array, generador...).

    ¿Para qué sirve?
    Para reemplazar bucles imperativos por operaciones encadenadas más legibles.
    En vez de "recorro la lista, si cumple la condición la añado a otra lista",
    escribes directamente lo que quieres: filter, map, collect.

    ¿Cuándo usarlo?
    - Transformaciones y filtrados sobre colecciones.
    - Agrupaciones, estadísticas, reducciones.
    - Cuando quieres aprovechar paralelismo fácilmente con parallelStream().

    ¿Cuándo NO usarlo?
    - Si necesitas modificar la colección original durante el recorrido.
    - Si el bucle tiene efectos secundarios complejos que dependen del orden.
    - Para operaciones muy simples donde un for es más claro.

    Tipos de operaciones:

    Intermedias - devuelven otro Stream, son lazy (no se ejecutan hasta la terminal)
        filter, map, flatMap, sorted, distinct, limit, skip, peek

    Terminales - cierran el stream y producen un resultado
        collect, reduce, forEach, count, anyMatch, allMatch, findFirst, min, max

    Preguntas típicas de entrevista:
    - ¿Qué significa que los streams son lazy?
    - ¿Qué diferencia hay entre map y flatMap?
    - ¿Cuándo usarías parallelStream()? ¿Qué riesgos tiene?
    - ¿Puedes reutilizar un stream una vez consumido?
    - ¿Qué diferencia hay entre findFirst() y findAny() en paralelo?
*/
