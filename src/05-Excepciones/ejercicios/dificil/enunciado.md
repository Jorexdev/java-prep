# Excepciones — Ejercicios Difícil

Ejercicios avanzados: patrón Result, reintentos, suppressed exceptions, handler global, validación acumulativa.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Patrón Result<T>
Implementa clase genérica `Resultado<T>` con factories `Resultado.exito(T valor)` y `Resultado.error(String mensaje)`. Métodos: `esExito()`, `getValor()`, `getError()`. Crea método `parsear(String s)` que retorna `Resultado<Integer>`. El caller no necesita try-catch.

## Ejercicio 2 — Reintentos automáticos
Implementa `<T> T ejecutarConReintentos(java.util.function.Supplier<T> op, int maxIntentos)` que reintente hasta maxIntentos veces ante excepción. Si todos fallan, lanza la última excepción. Prueba con una operación que falla las 2 primeras veces y acierta en la tercera.

## Ejercicio 3 — Suppressed exceptions
En try-with-resources, si tanto el body como el close() lanzan excepción, la del body es la principal y la del close() queda suprimida en `e.getSuppressed()`. Crea un ejemplo que lo demuestre con un Recurso cuyo close() lanza RuntimeException.

## Ejercicio 4 — Handler global de hilos
Usa `Thread.setDefaultUncaughtExceptionHandler` para capturar excepciones no manejadas en hilos. Lanza una RuntimeException sin capturar en un hilo nuevo y verifica que el handler la recibe.

## Ejercicio 5 — Validación acumulativa
Implementa `List<String> validar(Pedido p)` que acumule TODOS los errores (nombre vacío, cantidad <= 0, precio <= 0, email sin @) en lugar de lanzar en el primero. Prueba con un pedido con 3 errores simultáneos.
