# Ejercicios — 17 Recolector de Basura

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Reachability simulation**
Implementa un grafo de objetos con nodos identificados por nombre. Define una lista de GC roots (nodos raíz). Realiza la fase de marcado (mark phase): visita en profundidad todos los nodos alcanzables desde las roots y márcalos como "vivos". Los nodos no marcados son "basura". Imprime cuáles objetos están vivos y cuáles serían recolectados.

---

**Ejercicio 2 — WeakReference**
Crea un objeto `String` y envuélvelo en una `WeakReference<String>`. Antes de llamar a `System.gc()`, muestra que `weakRef.get()` devuelve el valor. Luego elimina la referencia fuerte (asigna null), llama a `System.gc()` y verifica si `weakRef.get()` devuelve null. Compara con una referencia fuerte que nunca se pierde.

---

**Ejercicio 3 — SoftReference**
Crea una `SoftReference<byte[]>` apuntando a un array de 1MB. Accede al objeto varias veces sin presión de memoria: debe seguir disponible. Luego llama a `System.gc()` y verifica si sigue disponible. Explica con mensajes en consola por qué las soft references son útiles para caches.

---

**Ejercicio 4 — Generaciones**
Simula una generación Young (capacidad 10 objetos) y una Old (capacidad 50 objetos). Cada objeto tiene un contador de "supervivencias". Al ejecutar un minor GC simulado, los objetos de Young con supervivencias >= 2 se promueven a Old; el resto se descartan. Ejecuta 5 ciclos añadiendo 8 objetos por ciclo. Muestra el estado de cada región después de cada ciclo.

---

**Ejercicio 5 — Object lifecycle**
Crea una clase `Recurso` con un nombre. En el constructor imprime `"[CREATED] nombre"`. Sobrescribe `finalize()` para imprimir `"[FINALIZED] nombre"`. Crea 3 instancias, asígnalas a null, llama a `System.gc()` y `System.runFinalization()`. Observa el ciclo de vida completo con los mensajes.

---

**Ejercicio 6 — Memory leak simple**
Crea una clase `Leak` con una lista estática `List<byte[]>`. El método `leak()` añade arrays de 10KB a la lista sin eliminarlos nunca. Llama a `leak()` 20 veces y muestra después de cada llamada el tamaño de la lista y la memoria usada con `Runtime.getRuntime()`. Observa cómo crece el "heap" sin liberarse.
