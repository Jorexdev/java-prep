<div align="center">
  <a href="#"><img src="../../assets/modules/banner-09-colecciones-conjunto-v1.svg" width="100%" alt=""/></a>
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

`Set` es una colección que **no permite duplicados**. Usa `equals()` y `hashCode()` para detectar si un elemento ya existe antes de insertarlo.

```java
Set<String> set = new HashSet<>();
set.add("Java");
set.add("Java");   // ignorado — duplicado
set.size();        // 1
```

Implementar correctamente `equals()` y `hashCode()` en tus clases es esencial para que los Sets funcionen correctamente.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

| Implementación | Orden | Complejidad | Notas |
|---|---|---|---|
| `HashSet` | Ninguno | O(1) | Opción por defecto. Backed by HashMap. |
| `LinkedHashSet` | Inserción | O(1) | Orden predecible. |
| `TreeSet` | Natural/Comparator | O(log n) | Siempre ordenado. |
| `EnumSet` | Declaración enum | O(1) | Solo enums. Usa bits. Muy rápido. |
| `ConcurrentSkipListSet` | Natural | O(log n) | Thread-safe ordenado. |

**HashSet** es la opción por defecto para unicidad sin orden.

**TreeSet** implementa `NavigableSet`: además de `SortedSet`, permite operaciones como `floor()`, `ceiling()`, `headSet()`, `tailSet()` — útil para rangos.

**EnumSet** es la implementación más eficiente para conjuntos de valores de un enum (representación bit a bit).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Garantía de unicidad automática — sin lógica de deduplicación manual.
- HashSet: O(1) para contains, add, remove.
- TreeSet: iteración siempre ordenada + operaciones de rango.
- EnumSet: rendimiento excepcional para flags de enum.

Ver cada implementación en los archivos `Exp*.java` para comparar comportamientos.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
