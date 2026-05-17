<div align="center">
  <a href="#"><img src="../../assets/modules/banner-08-colecciones-mapa-v1.svg" width="100%" alt=""/></a>
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

**¿Cuándo usarías HashMap vs TreeMap vs LinkedHashMap?**
HashMap para búsquedas/inserciones O(1) sin importar el orden. TreeMap cuando necesitas las claves siempre ordenadas (range queries, NavigableMap). LinkedHashMap cuando necesitas mantener el orden de inserción (ej. LRU cache con `removeEldestEntry`).

---

**¿Qué pasa si dos objetos tienen el mismo `hashCode()` pero distinto `equals()`?**
Van al mismo bucket (colisión). La estructura los trata como claves distintas porque `equals` es la distinción final. El rendimiento degrada a O(n) si hay muchas colisiones. En Java 8+, los buckets con muchas colisiones se convierten en árboles (O(log n)).

---

**¿Puede un HashMap tener null como clave?**
Sí, exactamente una clave null (se almacena en el bucket 0). Como valor puede tener null sin restricciones. TreeMap no admite null como clave porque necesita comparar. ConcurrentHashMap tampoco admite null ni en clave ni en valor.

---

**¿Qué diferencia hay entre HashMap y ConcurrentHashMap?**
HashMap no es thread-safe. ConcurrentHashMap es thread-safe sin bloquear todo el mapa: usa segmentación (Java 7) o CAS/synchronized por bucket (Java 8+). No permite null en clave ni valor. Preferible a `Collections.synchronizedMap()` bajo alta concurrencia.

---

**¿Qué hace `computeIfAbsent()`?**
Si la clave no existe (o su valor es null), calcula el valor con el Supplier/Function y lo inserta. Si la clave existe, no hace nada. Patrón ideal para cachés: `cache.computeIfAbsent(key, k -> calcularValorCostoso(k))`.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
