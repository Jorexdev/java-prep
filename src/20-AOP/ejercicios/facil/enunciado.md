# Ejercicios — 20 AOP
## Fácil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Proxy de logging**
Define la interfaz `Calculadora` con métodos `sumar(a,b)`, `restar(a,b)` y `multiplicar(a,b)`.
Implementa `CalculadoraReal` que hace el cálculo.
Implementa `LoggingProxy` que delega a `CalculadoraReal` pero imprime entrada y salida de cada llamada.
Demo: llama los 3 métodos a través del proxy.

---

**Ejercicio 2 — Proxy de timing**
Implementa una clase `TimingProxy` con un método estático `timedRun(String nombre, Runnable tarea)`.
El método mide el tiempo de ejecución de la tarea en milisegundos y la imprime: `[nombre] -> Xms`.
Demo: envuelve 3 lambdas distintas (con distintos tiempos de ejecución).

---

**Ejercicio 3 — @Before simulado**
Define la interfaz `BeforeAdvice` con `before(String method, Object[] args)`.
Implementa `ProxyFactory.wrap(T target, BeforeAdvice advice)` que retorna un proxy dinámico de la interfaz `T`.
El proxy ejecuta el `advice.before(...)` antes de cada llamada al método real.
Demo: envuelve una `Calculadora` con un advice que loguea los argumentos.

---

**Ejercicio 4 — @AfterReturning simulado**
Define la interfaz `AfterReturningAdvice` con `afterReturning(Object result)`.
Implementa un `ProxyFactory` que aplica el advice después de cada llamada.
El advice de demo multiplica el resultado por 2 e imprime "audit: resultado original X -> transformado Y".
Demo: llama `sumar(3, 4)` y verifica que el resultado auditable es 14 (7×2).

---

**Ejercicio 5 — @AfterThrowing simulado**
Define la interfaz `AfterThrowingAdvice` con `afterThrowing(Exception e)`.
Implementa un `ProxyFactory` que aplica el advice solo cuando el método lanza excepción.
El advice imprime "excepcion capturada en [método]: [mensaje]".
Demo: método `dividir(a, b)` que lanza `ArithmeticException` cuando b=0. Llama con b=2 (sin excepción) y b=0 (con excepción).

---

**Ejercicio 6 — Pointcut por nombre**
Define una lista de métodos interceptables: `{"sumar", "restar"}`.
Implementa `NamedPointcutProxy` que solo aplica el advice si el nombre del método está en la lista.
Demo: 3 métodos en la interfaz (`sumar`, `restar`, `multiplicar`). Los dos primeros loguean, el tercero pasa sin log.
