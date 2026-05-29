<div align="center">
  <a href="#"><img src="../../assets/modules/banner-41-java-moderno-v1.svg" width="100%" alt="41 - Java Moderno"/></a>
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

**¿Qué es un Record y en qué se diferencia de una clase normal con Lombok `@Value`?**

Un Record es una feature del lenguaje, no una librería. El compilador genera el constructor canónico, accessors (sin prefijo `get`), `equals`, `hashCode` y `toString` directamente, sin procesamiento de anotaciones. Las diferencias clave respecto a `@Value` de Lombok: los records son `final` implícitamente y no permiten herencia de otras clases (solo implementar interfaces); los componentes del record forman parte de la API pública y aparecen en el bytecode como `RecordComponent`, permitiendo introspección en runtime; el compact constructor permite validar sin repetir los parámetros. Además, los records se integran con pattern matching como deconstruction patterns, algo imposible con Lombok.

---

**¿Para qué sirven las Sealed Classes y cómo se relacionan con Pattern Matching?**

Las sealed classes cierran una jerarquía: declaran explícitamente con `permits` qué subclases están permitidas. Esto da al compilador información completa sobre todas las variantes posibles. La relación con pattern matching es directa: cuando usas una sealed class en un switch expression con pattern matching, el compilador puede verificar exhaustiveness —que todos los casos posibles están cubiertos— sin necesidad de un `default`. Si añades una nueva subclase a la sealed class, todos los switches que la usan fallarán en compilación, actuando como un sistema de alerta proactivo. Sin sealed, el compilador no sabe cuántas subclases pueden existir y no puede garantizar exhaustiveness.

---

**¿Qué es Pattern Matching for instanceof y cómo elimina casts explícitos?**

Antes de Java 16, el patrón clásico era: `if (obj instanceof String) { String s = (String) obj; ... }`. Con pattern matching, el test y el binding se unifican: `if (obj instanceof String s) { ... }`. La variable `s` es de tipo `String` y solo existe en el scope donde el test es verdadero (incluyendo la condición del `&&`). El cast es innecesario porque el compilador lo inserta y lo garantiza seguro. En Java 21, este mismo mecanismo se extiende a switch con guarded patterns: `case String s when s.length() > 5 -> ...`, donde `when` actúa como condición adicional sobre la variable ya enlazada.

---

**¿Cuál es la diferencia entre switch expression y switch statement en Java 14+?**

El switch statement es el clásico: ejecuta código con side effects, requiere `break` para evitar fall-through, y no produce un valor. El switch expression produce un valor que puede asignarse o devolverse directamente. Usa sintaxis de flecha `->` por defecto (sin fall-through posible) o `case X: yield valor;` cuando el caso necesita lógica. El compilador obliga a que todos los casos estén cubiertos en un switch expression (exhaustiveness), y el tipo del resultado debe ser el mismo en todos los brazos. En resumen: si necesitas un valor y quieres seguridad, usa expression; si solo ejecutas efectos, el statement sigue siendo válido.

---

**¿Cuándo usar Record vs clase inmutable tradicional?**

Usa Record cuando el tipo es principalmente un agregador de datos sin lógica de dominio compleja: DTOs, value objects, tuplas, resultados de métodos. Los records son ideales cuando la igualdad estructural (por valores) es la semántica correcta. Usa una clase inmutable tradicional cuando: necesitas herencia de otra clase (los records no pueden extender clases), los campos internos no deben ser parte de la API pública (en un record todos los componentes son accesibles), necesitas constructores con nombres semánticos (los records solo tienen el canónico), o la igualdad debe ser por identidad o usar solo algunos campos. Nunca uses records para entidades JPA (Hibernate requiere constructores sin argumentos y mutable state).

---

**¿Qué limitaciones tienen los Records?**

Los records son implícitamente `final`: no pueden ser extendidos. Solo pueden implementar interfaces, no extender otras clases. Todos sus componentes son `private final` y forman parte de la API pública (no puedes tener componentes "privados" del record). No pueden declarar campos de instancia adicionales fuera de los componentes (sí pueden tener campos estáticos). El constructor canónico no puede lanzar checked exceptions que no declare la propia clase. No son compatibles con JPA/Hibernate por la falta de constructor sin argumentos y la inmutabilidad. No son serializables de forma estándar sin implementar `Serializable` explícitamente.

---

**¿Cómo funciona el exhaustiveness check en switch con sealed classes?**

Cuando el tipo del selector en un switch expression o switch statement con pattern matching es una sealed class, el compilador analiza el grafo de subclases declaradas con `permits`. Para que el switch sea exhaustivo, debe haber un case que cubra cada subclase concreta posible. Las subclases `sealed` intermedias requieren que sus propias subclases también estén cubiertas. Si falta alguna variante, el compilador emite un error en tiempo de compilación, no en runtime. Esto es más seguro que el `default` clásico porque añadir una nueva subclase a la sealed class convierte automáticamente todos los switches sin esa variante en errores de compilación, forzando al desarrollador a tratar el nuevo caso explícitamente.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
