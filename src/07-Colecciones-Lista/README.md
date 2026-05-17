<div align="center">
  <a href="#"><img src="../../assets/modules/banner-07-colecciones-lista-v1.svg" width="100%" alt=""/></a>
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

`List` es una colección **ordenada** (mantiene el orden de inserción) que **permite duplicados** y ofrece acceso por índice. Es la colección más usada en Java.

La elección de implementación importa: `ArrayList` y `LinkedList` tienen perfiles de rendimiento muy distintos según el tipo de operación.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

| Implementación | Acceso por índice | Inserción/borrado extremos | Inserción/borrado medio | Notas |
|---|---|---|---|---|
| `ArrayList` | O(1) | O(n) amortizado | O(n) | Array dinámico. La opción por defecto. |
| `LinkedList` | O(n) | O(1) | O(n) con iterador | Lista doblemente enlazada. |
| `Vector` | O(1) | O(n) | O(n) | Legacy. Sincronizado. Evitar. |
| `Stack` | O(n) | O(1) | O(n) | Legacy. Usa `ArrayDeque` como stack. |

**ArrayList** es la opción por defecto para la mayoría de casos: acceso aleatorio rápido, iteración secuencial eficiente.

**LinkedList** solo es mejor que ArrayList cuando insertas/borras con frecuencia en los extremos **y** no necesitas acceso por índice. En la práctica, `ArrayDeque` suele ser mejor para ese caso.

```java
List<String> lista = new ArrayList<>();  // inmutable: List.of("a","b")
lista.add("Java");
lista.get(0);
lista.remove(0);
lista.sort(Comparator.naturalOrder());
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Orden de inserción garantizado.
- Acceso por índice (`get(i)`) en O(1) con ArrayList.
- API rica: `sort`, `subList`, `listIterator`, `removeIf`, `replaceAll`.
- Interoperabilidad con Streams y lambdas.

Ver las implementaciones en [ExpArrayList.java](ExpArrayList.java), [ExpLinkedList.java](ExpLinkedList.java) y el resto de archivos para comparar comportamientos.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
