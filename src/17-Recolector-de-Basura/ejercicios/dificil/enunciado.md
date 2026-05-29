# Ejercicios — 17 Recolector de Basura

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Generational GC simulation**
Implementa `GenerationalGC` con Young (capacidad 10) y Old (capacidad 50). El método `allocate(String name)` añade objetos a Young. Un `minor GC` mueve los objetos "vivos" de Young a Old y descarta los muertos (simula con una lista de nombres de objetos vivos). Un `major GC` recoge también Old. Simula 20 allocations intercalando minor y major GCs, mostrando el estado de cada región tras cada operación.

---

**Ejercicio 2 — GC tuning flags simulation**
Crea `JvmConfig` con: `heapInitial` (MB), `heapMax` (MB), `newRatio` (Old/Young), `survivorRatio` (Eden/Survivor). A partir de esos valores, calcula los tamaños de: Heap total, Young generation, Old generation, Eden space, Survivor space (x2), Metaspace (fijo 256MB). Muestra la distribución completa. Implementa un método `validate()` que detecta configuraciones incoherentes (ej. heapMax < heapInitial).

---

**Ejercicio 3 — Escape analysis simulation**
Define 4 métodos que crean objetos `Point(x, y)`:
1. El objeto se devuelve (escapa por return)
2. El objeto se almacena en un campo de instancia (escapa al heap)
3. El objeto se pasa a otro método que lo almacena (escapa)
4. El objeto solo se usa localmente y se descarta (NO escapa — candidato a stack allocation)

Implementa un analizador `EscapeAnalyzer` que, dado el código de cada método (como String descriptivo), clasifica los objetos como `ESCAPES` o `NO_ESCAPES`. Muestra el resultado y explica qué optimización hace la JVM con los objetos que no escapan.

---

**Ejercicio 4 — Off-heap simulation**
Implementa `OffHeapBuffer` usando `java.nio.ByteBuffer.allocateDirect()`. El buffer almacena 1000 integers. Implementa `write(int index, int value)` y `read(int index)`. Mide el tiempo de escritura+lectura de 1000 integers en un buffer directo vs un `int[]` en heap. Imprime la diferencia. Explica por qué los buffers directos no tienen presión de GC y cuándo usarlos.

---

**Ejercicio 5 — Memory leak detector con WeakReference + ReferenceQueue**
Implementa `LeakDetector<T>` que rastrena objetos mediante `WeakReference<T>` asociadas a una `ReferenceQueue<T>`. `track(T obj, String name)` registra el objeto. `processQueue()` drena la queue y reporta qué objetos fueron recogidos por el GC. `printReport()` muestra totales: registrados, recogidos, vivos, posibles leaks. Escenario 1: 5 objetos `CachedResult` sin referencia fuerte; después de `System.gc()` deben aparecer en la queue. Escenario 2: mismos 5 objetos retenidos por un `CacheSinControl` (HashMap); el GC NO los recoge (leak demostrado). Escenario 3: vaciar el cache y confirmar que ahora sí se recogen.

---
