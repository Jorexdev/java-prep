<div align="center">
  <a href="#"><img src="../../assets/modules/banner-03-opcional-v1.svg" width="100%" alt=""/></a>
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

**¿Cuándo usas Optional y cuándo no?**
Lo uso como tipo de retorno en métodos que pueden no encontrar un resultado (ej. `findById`). No lo uso como parámetro de método (es más claro con sobrecarga), como campo de clase (no es serializable), ni dentro de colecciones (una List de Optional es redundante — usa List vacía).

---

**¿Cuál es la diferencia entre `Optional.of()` y `Optional.ofNullable()`?**
`of()` lanza `NullPointerException` si el valor es null — se usa cuando sabes que el valor no puede ser nulo. `ofNullable()` acepta null y devuelve `Optional.empty()` — se usa cuando el valor puede ser null.

---

**¿Cuál es la diferencia entre `orElse()` y `orElseGet()`?**
`orElse(T)` siempre evalúa la expresión del argumento aunque el Optional tenga valor (puede ser costoso si es una llamada de método). `orElseGet(Supplier)` solo invoca el Supplier si el Optional está vacío (lazy) — preferible cuando el valor por defecto es caro de calcular.

---

**¿Puedes usar Optional en colecciones o como campo de clase?**
Técnicamente sí, pero no se recomienda. No es `Serializable`, lo que puede causar problemas con JPA/Hibernate. En colecciones, `List<Optional<T>>` es confusa: es mejor `List<T>` vacía para representar ausencia de resultados.

---

**¿Qué diferencia hay entre `map()` y `flatMap()` en Optional?**
`map()` aplica una función que devuelve un valor normal y lo envuelve en Optional. `flatMap()` aplica una función que ya devuelve un Optional, evitando `Optional<Optional<T>>`. Se usa cuando la transformación en sí puede devolver Optional.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
