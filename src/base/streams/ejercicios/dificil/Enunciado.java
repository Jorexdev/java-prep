package base.streams.ejercicios.dificil;

/*
    STREAMS - Ejercicios Hard

    Ejercicios de nivel avanzado que requieren combinar múltiples operaciones complejas:
    thenComparing, reducing, Collector personalizado, mapas inversos, nullsLast...

    Las soluciones están en: hard/soluciones/Ejercicio1.java ... Ejercicio10.java
*/
public class Enunciado {

    /*
        Ejercicio 1 - Ordenamiento compuesto con thenComparing

        Dada una lista de empleados con nombre, edad y salario, ordena primero por salario
        descendente y, en caso de empate, por edad ascendente. Luego obtén los primeros 10.
    */

    /*
        Ejercicio 2 - Agrupamiento con reducción

        Dada una lista de ventas con cliente, monto y fecha, agrupa por cliente y calcula
        la venta de mayor monto usando Collectors.reducing().
    */

    /*
        Ejercicio 3 - Agrupamiento y colección personalizada

        Dada una lista de productos con nombre, categoría y etiquetas (List<String>),
        agrupa por categoría y obtén un conjunto plano de todas las etiquetas distintas
        por categoría.
    */

    /*
        Ejercicio 4 - Mapeo inverso con fusión

        Dada una lista de objetos Palabra con texto e idioma, construye un mapa donde la clave
        sea el texto y el valor sea una lista de idiomas en los que aparece esa palabra.
    */

    /*
        Ejercicio 5 - Ordenar agrupaciones

        Dada una lista de cursos con nombre, área y duración, agrupa por área y ordena
        los cursos de cada área por duración ascendente.
    */

    /*
        Ejercicio 6 - Detección del más frecuente

        Dada una lista de logs con tipo de evento (INFO, WARN, ERROR), encuentra el tipo
        de evento más frecuente y su conteo.
    */

    /*
        Ejercicio 7 - Aplanar estructuras anidadas complejas

        Dada una lista de usuarios donde cada uno tiene una lista de órdenes, y cada orden
        una lista de productos, obtén una lista única de productos distintos comprados
        por todos los usuarios.
    */

    /*
        Ejercicio 8 - Agrupamiento por rango de edad

        Dada una lista de personas con nombre, edad y ciudad, agrúpalas según el rango:
        - "Jovenes" (menos de 30)
        - "Adultos" (30 a 60)
        - "Mayores" (más de 60)
    */

    /*
        Ejercicio 9 - Comparador múltiple con nulls

        Dada una lista de documentos con título, autor y fecha, ordénalos primero por fecha
        (más recientes primero), luego por autor (alfabéticamente). Los campos null van al final.
    */

    /*
        Ejercicio 10 - Reducción múltiple con resumen estadístico

        Dada una lista de evaluaciones con estudiante, curso y nota, genera un mapa con el
        nombre del curso y un resumen estadístico (media, max, min, count) de las notas.
    */
}
