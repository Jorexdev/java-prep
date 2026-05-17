<div align="center">
  <a href="#"><img src="../../assets/modules/banner-13-colecciones-utilidades-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

La clase `java.util.Collections` proporciona métodos de utilidad estáticos para operar sobre colecciones. Complementa a la API de colecciones con algoritmos y vistas especiales.

Las interfaces `Iterable`, `Iterator`, `Comparable` y `Comparator` forman la base del sistema de iteración y ordenación de Java.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**`Collections` — métodos clave:**

```java
Collections.sort(lista);              // ordena in-place
Collections.sort(lista, comparator);  // con comparador
Collections.reverse(lista);           // invierte
Collections.shuffle(lista);           // aleatoriza
Collections.min(col) / max(col);      // extremos
Collections.frequency(col, elem);     // ocurrencias
Collections.unmodifiableList(lista);  // vista inmutable
Collections.synchronizedList(lista);  // vista sincronizada
```

**Comparable vs Comparator:**

| | `Comparable<T>` | `Comparator<T>` |
|---|---|---|
| Dónde | En la clase | Fuera de la clase |
| Método | `compareTo(T o)` | `compare(T a, T b)` |
| Uso | Ordenación natural | Ordenación alternativa |
| Encadenamiento | No | `thenComparing()` |

**Iterator vs ListIterator:**
- `Iterator`: solo avanza (`next`, `hasNext`, `remove`).
- `ListIterator`: avanza y retrocede, soporta `set` y `add` en posición actual.

**Iterable vs Iterator:**
- `Iterable`: cualquier clase que "puede iterarse" — implementa `iterator()`.
- `Iterator`: el cursor que realiza la iteración.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  `Collections.unmodifiableList()` para exponer colecciones sin mutación.
- `Comparator.comparing().thenComparing()` para ordenación multicriteria fluida.
- `Iterator` para eliminación segura durante iteración.
- `Comparable` para el orden natural del dominio.

Ver [ExpCollections.java](ExpCollections.java), [ExpComparable.java](ExpComparable.java), [ExpComparator.java](ExpComparator.java), [ComparableVsComparator.java](ComparableVsComparator.java) y [IterableVSIterator.java](IterableVSIterator.java).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
