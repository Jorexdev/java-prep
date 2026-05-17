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

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
