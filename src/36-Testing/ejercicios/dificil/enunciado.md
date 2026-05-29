# Ejercicios — 36 Testing

## Difícil

**Ejercicio 1 — Test de concurrencia**
Implementa `ContadorConcurrente` dos veces: primero sin sincronización (con race condition) y luego con `AtomicInteger`. Lanza 100 threads × 1000 incrementos con `CountDownLatch` para que arranquen simultáneamente. Verifica que el resultado esperado es 100 000. Ejecuta ambas versiones en `main` y muestra la diferencia de resultado entre la versión sin sincronizar y la sincronizada.

**Ejercicio 2 — Los 5 tipos de Test Doubles**
Implementa los cinco tipos de test doubles sobre la interfaz `PagoService`: `Dummy` (nunca se usa, solo satisface la firma), `Stub` (devuelve respuestas fijas predefinidas), `Fake` (implementación funcional simplificada con lógica real mínima), `Mock` (verifica que se llamó exactamente con los argumentos esperados) y `Spy` (envuelve la implementación real y registra las llamadas). Demuestra cada uno en un contexto distinto dentro de `ProcesamientoPedido`.

**Ejercicio 3 — Test de rendimiento con estadísticas**
Implementa `PerformanceTest.benchmark(String nombre, Runnable fn, int iteraciones)` que realice un warmup del 10% de las iteraciones y luego mida las restantes. Devuelve estadísticas: min, max, media y p95 en nanosegundos. Compara dos implementaciones de `buscarElemento`: búsqueda lineal en `List` O(n) frente a búsqueda en `HashSet` O(1), ambas con 100 000 elementos. Imprime una tabla comparativa con los resultados.

**Ejercicio 4 — Test de integración multicapa (sin mocks)**
Conecta tres capas reales sin ningún mock: `ProductoRepository` (almacenamiento en `HashMap`), `ProductoService` (lógica de negocio: sin duplicados, stock ≥ 0) y `ProductoController` (valida la request antes de llamar al servicio). Escribe tests de integración que ejerciten flujos completos: crear un producto, leer por id, actualizar stock, intentar crear un duplicado (debe fallar) e intentar poner stock negativo (debe fallar). Reporta `PASS`/`FAIL` por caso.

**Ejercicio 5 — Test de arquitectura: validar dependencias entre capas**
Implementa un `ArchitectureValidator` que use reflexión para inspeccionar las clases de un módulo y validar reglas de dependencia. Las reglas son: (1) las clases en el paquete simulado `service` no deben importar directamente clases de `repository`; (2) las clases en `controller` solo pueden importar de `service`, nunca de `repository`; (3) las clases en `repository` no pueden importar de `controller` ni de `service`. Simula las clases como `ArchClass(String nombre, String capa, List<String> imports)`. El validator evalúa una lista de `ArchRule` sobre el conjunto de clases y reporta las violaciones encontradas. El `main` define 8 clases con algunas violaciones intencionadas y muestra cuáles reglas se cumplen y cuáles no.
