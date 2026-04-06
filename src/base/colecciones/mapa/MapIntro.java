package base.colecciones.mapa;

/*
    MAP - Introducción

    ¿Qué es?
    Colección de pares clave-valor (key - value).
    Cada clave debe ser única, los valores pueden repetirse.
    No hereda de Collection, pero forma parte del JCF.

    ¿Para qué sirve?
    Búsquedas rápidas por clave. Es la estructura de datos más usada
    cuando necesitas asociar un identificador a un valor.

    Implementaciones principales:

    HashMap
    - Basado en tabla hash. Sin orden definido.
    - Operaciones O(1) amortizado.
    - Permite una clave null y múltiples valores null.
    - La opción por defecto.

    LinkedHashMap
    - HashMap + lista enlazada.
    - Mantiene el orden de inserción (o el de acceso si se configura).
    - Útil para implementar una caché LRU sobreescribiendo removeEldestEntry().

    TreeMap
    - Basado en árbol rojo-negro.
    - Mantiene las claves ordenadas (orden natural o Comparator).
    - Operaciones O(log n). Implementa NavigableMap.

    WeakHashMap
    - Las claves son referencias débiles: si no hay otra referencia a la clave,
      el GC puede eliminar la entrada. Útil para cachés donde la clave puede desaparecer.

    IdentityHashMap
    - Compara claves con == en lugar de equals().
    - Útil cuando necesitas identidad de objeto, no igualdad de contenido.

    Hashtable
    - Legacy. Como HashMap pero sincronizado. Prefiere ConcurrentHashMap.

    Preguntas típicas de entrevista:
    - ¿Cómo funciona internamente HashMap? (hashing, buckets, colisiones)
    - ¿Qué es el load factor y cuándo se redimensiona HashMap?
    - ¿Cuándo usarías LinkedHashMap vs HashMap?
    - ¿Cómo implementarías una caché LRU en Java?
    - ¿Qué diferencia hay entre HashMap y ConcurrentHashMap?
*/
