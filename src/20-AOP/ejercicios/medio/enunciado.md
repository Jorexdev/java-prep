# Ejercicios — 20 AOP
## Medio
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — @Around advice**
Define la interfaz `AroundAdvice` con el método `Object invoke(String method, Object[] args, Callable<Object> proceed)`.
Implementa `ProxyFactory.wrap(T target, AroundAdvice advice)`.
Demo con 3 casos usando el mismo proxy:
1. Ejecutar el método normalmente (llamar `proceed.call()`).
2. Bloquear la ejecución si args[0] es negativo (no llamar `proceed`).
3. Modificar el resultado (multiplicar por 10 el valor retornado).

---

**Ejercicio 2 — Aspect chaining**
Implementa `LoggingAspect` y `TimingAspect` como `AroundAdvice`.
`LoggingAspect` imprime `[LOG] before` y `[LOG] after`.
`TimingAspect` imprime `[TIMING] before` y `[TIMING] Xms`.
Aplica ambos al mismo `Calculadora` en orden: logging primero, timing segundo.
Demuestra que el orden de ejecución es: LOG-before, TIMING-before, método, TIMING-after, LOG-after.

---

**Ejercicio 3 — JoinPoint**
Define la clase `JoinPoint` con `getMethodName()`, `getArgs()` y `getTargetClass()`.
El advice recibe el `JoinPoint` como parámetro.
Imprime todos los datos del join point: clase, método, argumentos.
Demo: 3 métodos con distintos argumentos.

---

**Ejercicio 4 — @Auditar anotación**
Define la anotación `@Auditar` (retención RUNTIME).
Implementa un proxy que usa reflection para detectar si el método tiene `@Auditar`.
Solo intercepta los métodos anotados; los demás pasan directo.
Demo: interfaz con 4 métodos, 2 con `@Auditar` y 2 sin ella.

---

**Ejercicio 5 — @Transactional simulado**
Define la interfaz `TransactionManager` con `begin()`, `commit()` y `rollback()`.
Implementa un `TransactionalAspect` que:
- Llama `begin()` antes del método.
- Llama `commit()` si el método termina sin excepción.
- Llama `rollback()` si lanza excepción.
Demo con un servicio que tiene un método exitoso y uno que falla.

---

**Ejercicio 6 — Retry aspect con backoff exponencial**
Implementa `RetryAspect(maxAttempts, initialDelayMs, multiplier)` que envuelve cualquier `Callable<T>` con lógica de reintento automático. En cada intento fallido, espera `delay` ms y lo multiplica por `multiplier` (backoff exponencial). Si se agotan los intentos, propaga la última excepción. `RetryProxy<T>` delega al aspecto cada llamada a `calcular(int)` y `obtenerDato(String)`. Demo con 3 escenarios: servicio con 65% fallo (suele recuperarse), servicio con 95% fallo (agota intentos) y tabla de delays esperados (50ms → 100ms → 200ms → 400ms).

---
