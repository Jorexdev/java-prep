<div align="center">
  <a href="#"><img src="../../assets/modules/banner-03-opcional-v1.svg" width="100%" alt=""/></a>
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

`Optional<T>` es un contenedor que puede o no contener un valor no nulo. Su propósito es modelar explícitamente la **ausencia de valor** sin recurrir a `null`, eliminando `NullPointerException` y documentando la intención en la firma del método.

Introducido en Java 8, `Optional` es especialmente útil como tipo de retorno de métodos que pueden no encontrar un resultado (repositorios, búsquedas...).

> Regla de oro: usa `Optional` como tipo de retorno. **Nunca** como parámetro de método, campo de clase o en colecciones.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Creación:**

```java
Optional<String> lleno    = Optional.of("valor");       // lanza NPE si null
Optional<String> nullable = Optional.ofNullable(valor); // acepta null → Optional.empty()
Optional<String> vacío    = Optional.empty();
```

**Extracción segura:**

| Método | Comportamiento |
|---|---|
| `orElse(T)` | Devuelve el valor o el default (siempre evalúa el default) |
| `orElseGet(Supplier)` | Devuelve el valor o invoca el Supplier (lazy) |
| `orElseThrow(Supplier)` | Devuelve el valor o lanza excepción |
| `ifPresent(Consumer)` | Ejecuta si hay valor |
| `isPresent()` | Comprueba si hay valor |

**Transformación:**

```java
optional.map(String::toUpperCase)        // transforma si presente
        .filter(s -> s.length() > 3)     // filtra si presente
        .flatMap(s -> buscarOtro(s))     // cuando map devuelve Optional
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

-  Elimina `NullPointerException` de forma explícita.
- Documenta en la firma que un método puede no devolver valor.
- Composición fluida con `map`, `filter`, `flatMap`.
- Fuerza al llamador a manejar el caso ausente.

Ver [ExpOptional.java](ExpOptional.java), [ExpOptionalChaining.java](ExpOptionalChaining.java), [ExpOptionalVsException.java](ExpOptionalVsException.java) y [ExpOptionalAntipatterns.java](ExpOptionalAntipatterns.java) para ejemplos ejecutables con `of`, `ofNullable`, `map`, `flatMap`, `filter`, `orElse`, `orElseGet` y `orElseThrow`.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
