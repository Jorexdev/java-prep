<div align="center">
  <a href="#"><img src="../../assets/modules/banner-12-colecciones-concurrentes-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Cuál es la diferencia entre HashMap y ConcurrentHashMap?**
HashMap no es thread-safe: acceso concurrente puede corromper la estructura (buckets en estado inconsistente). ConcurrentHashMap es thread-safe con granularidad fina: en Java 8+ usa CAS y synchronized por bucket, no bloquea todo el mapa. Además, no permite null en clave ni valor.

---

**¿Cuándo usarías CopyOnWriteArrayList?**
En escenarios donde hay muchas lecturas y muy pocas escrituras: listas de event listeners, configuración dinámica, registros de observadores. Las lecturas no necesitan lock y son muy rápidas; las escrituras son caras (O(n) por la copia). No apto para escrituras frecuentes.

---

**¿Qué diferencia hay entre ConcurrentLinkedQueue y BlockingQueue?**
ConcurrentLinkedQueue es no bloqueante (lock-free con CAS): las operaciones poll/offer devuelven inmediatamente null/false si no hay elementos/espacio. BlockingQueue bloquea el hilo: `take()` espera hasta que haya elementos, `put()` espera hasta que haya espacio. BlockingQueue es mejor para productor-consumidor; ConcurrentLinkedQueue para casos no bloqueantes.

---

**¿Es ConcurrentHashMap 100% consistente?**
No en el sentido de atomicidad compuesta. Operaciones individuales (put, get) son atómicas, pero secuencias de operaciones no lo son sin sincronización adicional. Por eso existen `putIfAbsent`, `computeIfAbsent` y `merge` — para operaciones compuestas atómicas.

---

**¿Qué pasa con null en ConcurrentHashMap?**
Ni las claves ni los valores pueden ser null — lanza NullPointerException. La razón: `get()` devuelve null cuando la clave no existe, y si los valores también pudieran ser null, no podrías distinguir "clave ausente" de "clave con valor null". HashMap acepta este trade-off; ConcurrentHashMap no.

---

**¿Qué garantía de atomicidad ofrece `ConcurrentHashMap.compute()`?**
`compute(key, remappingFn)` ejecuta la función de remapeo de forma atómica sobre la clave: la lectura del valor actual, la aplicación de la función y la escritura del nuevo valor ocurren como una operación indivisible. Esto elimina el patrón no seguro de "get + lógica + put". Si la función devuelve null, la clave se elimina. Útil para contadores: `map.compute("k", (k, v) -> v == null ? 1 : v + 1)`.

---

**¿Cuándo es apropiado usar CopyOnWriteArrayList y cuándo no?**
Es apropiada cuando las lecturas son muy frecuentes y las escrituras son raras y no críticas en latencia: listas de listeners, configuraciones dinámicas, registros de suscriptores. No es apropiada si las escrituras son frecuentes porque cada mutación copia el array completo (O(n) memoria y tiempo). Tampoco garantiza consistencia fuerte entre iteradores y escrituras concurrentes posteriores.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
