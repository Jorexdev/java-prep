<div align="center">
  <a href="#"><img src="../../assets/modules/banner-05-excepciones-v1.svg" width="100%" alt=""/></a>
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

**¿Cuál es la diferencia entre checked y unchecked exceptions?**
Las checked (hijas directas de Exception que no son RuntimeException) deben declararse en la firma con `throws` o capturarse. El compilador lo obliga. Las unchecked (RuntimeException y sus hijas) no requieren declaración — representan errores de programación como NullPointerException o IllegalArgumentException.

---

**¿Cuándo deberías crear una excepción personalizada?**
Cuando quieres comunicar un error del dominio de negocio específico (ej. `PedidoNoEncontradoException`), cuando necesitas añadir información extra (código de error, contexto), o cuando quieres distinguir semánticamente entre distintos tipos de error en los catch.

---

**¿Qué hace `finally`? ¿Se ejecuta siempre?**
Se ejecuta siempre, incluso si hay un `return` en el try o catch. Las únicas excepciones son: `System.exit()`, un error fatal de JVM o que el hilo sea interrumpido de forma abrupta. `finally` es ideal para limpiar recursos, aunque `try-with-resources` es preferible para eso.

---

**¿Qué es `try-with-resources`?**
Una construcción de Java 7 que cierra automáticamente cualquier recurso que implemente `AutoCloseable` (InputStream, Connection, etc.) al salir del bloque try, ya sea de forma normal o por excepción. Equivale a un finally con close() pero más limpio y seguro.

---

**¿Puedes lanzar una excepción desde un bloque `finally`?**
Sí, pero es una mala práctica: enmascarará cualquier excepción que se estuviera propagando desde el try o catch. Si hay una excepción en finally, la original se pierde. `try-with-resources` maneja esto correctamente con las "suppressed exceptions".

---

**¿Por qué las APIs modernas de Java (Streams, Optional, CompletableFuture) prefieren unchecked exceptions?**

Las checked exceptions son incompatibles con las interfaces funcionales estándar (`Function`, `Consumer`, `Predicate`): sus métodos abstractos no declaran `throws`, por lo que no puedes lanzar una checked dentro de una lambda sin capturarla. Esto lleva a try/catch anidados que destruyen la legibilidad. Las APIs modernas optan por unchecked (normalmente `RuntimeException` o subclases específicas) o por tipos como `CompletableFuture` que encapsulan el error en el valor. Frameworks como Spring también usan esta filosofía: `DataAccessException` es unchecked.

---

**¿Qué es el exception chaining y cuándo deberías usarlo?**

El exception chaining consiste en envolver una excepción original dentro de otra con `new MiExcepcion("mensaje", causaOriginal)`. Se usa cuando traduces una excepción de infraestructura a una de dominio — por ejemplo, capturar `SQLException` y relanzar `RepositorioException` — preservando la causa raíz para el stack trace. Sin chaining perderías el contexto del error original, dificultando el diagnóstico en producción. `Throwable.getCause()` permite recuperar la cadena completa.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
