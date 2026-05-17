<div align="center">
  <a href="#"><img src="../../assets/modules/banner-04-genericos-v1.svg" width="100%" alt=""/></a>
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

Los **genéricos** permiten definir clases, métodos e interfaces parametrizados por tipo. En lugar de trabajar con `Object` y hacer casting manual, el compilador garantiza type safety en tiempo de compilación.

```java
// Sin genéricos — ClassCastException en runtime
List lista = new ArrayList();
lista.add("texto");
String s = (String) lista.get(0);

// Con genéricos — error en compilación
List<String> lista = new ArrayList<>();
lista.add("texto");
String s = lista.get(0);  // sin casting
```

**Type erasure:** en tiempo de ejecución, los tipos genéricos se borran y se reemplazan por `Object` (o el bound superior). Esto significa que `List<String>` y `List<Integer>` son el mismo tipo en runtime.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Wildcards y la regla PECS:**

| Wildcard | Uso | Regla |
|---|---|---|
| `<? extends T>` | Leer (Producer) | Solo lectura del tipo T |
| `<? super T>` | Escribir (Consumer) | Solo escritura del tipo T |
| `<?>` | Sin restricción | Solo lectura de Object |

**PECS** = **P**roducer **E**xtends, **C**onsumer **S**uper:
```java
// Producer: lees elementos de la lista → extends
void imprimir(List<? extends Number> numeros) { ... }

// Consumer: añades elementos a la lista → super
void añadir(List<? super Integer> lista) { lista.add(42); }
```

**Tipos genéricos acotados:**
```java
<T extends Comparable<T>>  // T debe implementar Comparable
<T extends Number & Cloneable>  // múltiples bounds
```

**Métodos genéricos:**
```java
public <T> List<T> repetir(T elemento, int n) { ... }
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

-  Type safety en compilación: elimina `ClassCastException` en runtime.
- Código reutilizable sin duplicación para distintos tipos.
- APIs más expresivas y auto-documentadas.
- Elimina el casting manual.

Ver [ExpGenericsBasics.java](ExpGenericsBasics.java), [Comodines.java](Comodines.java) y [GenericsCheatSheet.java](GenericsCheatSheet.java) para ejemplos de wildcards, PECS y métodos genéricos.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
