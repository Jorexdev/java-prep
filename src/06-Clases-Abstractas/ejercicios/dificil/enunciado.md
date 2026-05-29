# Clases Abstractas — Ejercicios Difícil

Ejercicios avanzados: Strategy con abstract class, sistema de notificaciones, abstract+interface, hooks completos.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Strategy con abstract class
`EstrategiaOrdenamiento` abstracta con campo `nombre` y abstracto `ordenar(List<Integer>)`. Implementa `BubbleSort` e `InsertionSort`. Comenta cuándo preferir abstract class sobre interface para Strategy.

## Ejercicio 2 — Sistema de notificaciones
`Notificacion` abstracta con campos comunes (destinatario, asunto, timestamp) y método concreto `log()`. Abstracto `enviar()`. Subclases `NotificacionEmail`, `NotificacionSMS`, `NotificacionPush`.

## Ejercicio 3 — Abstract class + interface
`Empleado` abstracta (qué ES: nombre, salario). Interface `Evaluable` (qué PUEDE: `evaluarRendimiento()`). `EmpleadoTecnico extends Empleado implements Evaluable`.

## Ejercicio 4 — Hooks opcionales completos
`Pipeline` abstracta con `ejecutar()` que llama `antes()` (hook vacío), `proceso()` (abstracto), `despues()` (hook vacío). `PipelineBasico` solo implementa proceso. `PipelineConLog` sobreescribe los tres.

## Ejercicio 5 — Mini framework de plugins
`Plugin` abstracto con métodos abstractos `nombre()`, `version()`, `ejecutar(Contexto)` y método concreto `prioridad()` (por defecto 0). `Contexto` transporta un mapa de datos compartidos. `PluginRegistry` registra plugins y los ejecuta ordenados por prioridad descendente. Implementa tres plugins concretos: `PluginLogger` (prioridad 10, registra eventos en el contexto), `PluginTransformador` (prioridad 5, transforma un valor del contexto a mayúsculas), `PluginAuditoria` (prioridad 1, imprime el estado final del contexto).
