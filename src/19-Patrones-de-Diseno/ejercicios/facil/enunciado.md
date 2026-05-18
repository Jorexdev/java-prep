# Ejercicios — Patrones de Diseño (Fácil)

## Ejercicio 1 — Singleton
Implementa un `ConfiguracionApp` Singleton que almacene pares clave-valor.
Usa inicialización lazy con `synchronized`. Demuestra que dos llamadas a `getInstance()`
devuelven la misma instancia.

## Ejercicio 2 — Factory Method
Crea una jerarquía `Boton` (abstract) con `render()` y `onClick()`.
Implementa `BotonWindows` y `BotonMac`.
`FabricaDialog` (abstract) con `crearBoton()` como factory method.
Implementa `DialogWindows` y `DialogMac`.

## Ejercicio 3 — Builder
Diseña un `Pizza` con campos: masa (obligatorio), salsa, queso, ingredientes (lista).
Usa Builder: `Pizza.Builder` con métodos encadenables. `build()` valida que la masa no sea vacía.
Demuestra creación de varias pizzas con diferente configuración.

## Ejercicio 4 — Adapter
Tienes `SistemaAntiguo` con método `getDataXML()` que devuelve XML simulado.
El sistema nuevo espera interfaz `DataProvider` con `getData()` que devuelve JSON.
Crea `AdaptadorXMLaJSON` que adapte `SistemaAntiguo` a `DataProvider`.

## Ejercicio 5 — Strategy
Implementa un `Ordenador<T>` que ordena una lista usando diferentes estrategias:
`OrdenarAscendente`, `OrdenarDescendente`, `OrdenarPorLongitud` (para strings).
La estrategia se inyecta por constructor y se puede cambiar en runtime.

## Ejercicio 6 — Observer
Implementa un sistema de eventos: `EventBus` con `suscribir(String evento, Observer)` y
`publicar(String evento, Object datos)`. `Observer` tiene `onEvento(String, Object)`.
Demuestra múltiples observers para el mismo evento.
