<div align="center">
  <a href="#"><img src="../../assets/modules/banner-19-patrones-diseno-v1.svg" width="100%" alt=""/></a>
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

**¿Cuándo usarías Builder vs Factory?**
Factory cuando la creación es simple y el cliente solo necesita indicar *qué tipo* crear. Builder cuando el objeto tiene muchos parámetros opcionales o su construcción requiere varios pasos en orden. Builder evita constructores telescópicos y hace el código más legible.

---

**¿Cómo implementarías un Singleton thread-safe?**
La forma recomendada: inicialización mediante el class loader (Initialization-on-demand holder). El inner class `Holder` se carga solo cuando se accede a `getInstance()`, garantizando inicialización lazy y thread-safe sin synchronized:
```java
private static class Holder { static final Singleton INST = new Singleton(); }
public static Singleton getInstance() { return Holder.INST; }
```

---

**¿Qué diferencia hay entre Adapter y Decorator?**
Adapter convierte una interfaz en otra para compatibilidad entre código incompatible (structural translation). Decorator envuelve un objeto de la misma interfaz añadiendo comportamiento adicional sin cambiar la interfaz. Adapter resuelve incompatibilidades; Decorator añade funcionalidad.

---

**¿Cómo implementarías el patrón Strategy?**
Define una interfaz con el método del algoritmo, implementa las variantes como clases (o lambdas), e inyecta la estrategia en el contexto. En Java moderno la estrategia suele ser una interfaz funcional y la inyección se hace con lambda: `new Ordenador(lista -> lista.sort(Comparator.reverseOrder()))`.

---

**¿Qué diferencia hay entre Observer y Event Bus?**
Observer es una relación directa entre Subject y sus Observers (el Subject los conoce). Event Bus desacopla emisores y receptores completamente — nadie se conoce; los eventos viajan por un intermediario. Observer es más simple; Event Bus escala mejor con muchos productores y consumidores.

---

**¿Cuál es la diferencia entre los patrones Strategy y State?**
Ambos encapsulan comportamiento variable en objetos intercambiables, pero el cliente que los controla es diferente. En Strategy, el cliente elige y fija la estrategia externamente; el objeto contexto no cambia de estrategia por sí solo. En State, el propio contexto (o el estado actual) decide cuándo transicionar a otro estado: el cambio de comportamiento es automático e interno, dirigido por el estado del sistema (ej. un semáforo que avanza de Rojo → Verde → Amarillo según su lógica interna).

---

**¿Cuándo usarías Decorator en lugar de herencia para añadir funcionalidad?**
Cuando necesitas combinar comportamientos en runtime de forma flexible o cuando no controlas la clase base. La herencia es estática (se define en compilación) y genera explosión de subclases si hay múltiples combinaciones posibles. Decorator envuelve un objeto de la misma interfaz y añade comportamiento antes/después: `new BufferedInputStream(new GZIPInputStream(new FileInputStream(path)))` apila tres decoradores sin crear subclases específicas para cada combinación. Prefiere Decorator cuando el número de combinaciones potenciales crece.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
