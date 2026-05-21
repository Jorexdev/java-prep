# Ejercicios — 20 AOP
## Difícil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — java.lang.reflect.Proxy**
Implementa `ProxyFactory.createProxy(Class<?> iface, InvocationHandler handler)`.
El `handler` debe encadenar 3 comportamientos:
1. Security check: si el método se llama `"admin"`, lanzar `SecurityException`.
2. Logging: imprimir entrada y salida.
3. Timing: medir tiempo de ejecución.
Demo con una interfaz de 3 métodos: uno normal, uno admin, uno rápido.
Demuestra que admin lanza excepción y los otros se ejecutan con log y timing.

---

**Ejercicio 2 — AOP registry**
Implementa `AspectRegistry` con `register(Predicate<Method> pointcut, Object aspect)`.
El `aspect` es un objeto con un método `invoke(Method m, Object target, Object[] args)`.
El `Weaver` aplica todos los aspects cuyo pointcut devuelve `true` para el método.
Demo: 2 aspects (logging, security) y 4 métodos (algunos interceptados por uno, otros por ambos).

---

**Ejercicio 3 — Caching aspect**
Define la anotación `@Cacheable` (retención RUNTIME).
El aspect cachea el resultado en `ConcurrentHashMap<String, Object>` usando `método + Arrays.toString(args)` como clave.
Segunda llamada con mismos args devuelve del cache sin ejecutar el método real.
Lleva un contador de ejecuciones reales.
Demo: método de cálculo costoso (sleep 100ms). Llama 5 veces con mismos args, 2 con args distintos.
Imprime: ejecuciones reales, hits del cache, tiempo total.

---

**Ejercicio 4 — Rate limiting aspect**
Define la anotación `@RateLimit(int maxCalls, long windowMs)`.
El aspect mantiene un `ConcurrentHashMap<String, Deque<Long>>` (método → timestamps).
En cada llamada: añade el timestamp actual, elimina los timestamps fuera de la ventana, verifica el límite.
Si supera `maxCalls` en `windowMs`, lanza `RateLimitException`.
Demo: `@RateLimit(maxCalls=3, windowMs=1000)`. Llama 5 veces rápidamente. Las 2 últimas deben fallar.
