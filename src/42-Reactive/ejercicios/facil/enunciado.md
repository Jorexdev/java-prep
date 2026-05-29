# Reactive — Ejercicios Fácil

Ejercicios básicos con Java Flow API, publishers, subscribers y operadores reactivos simples.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Publisher simple de enteros
Implementa un `Publisher<Integer>` usando `SubmissionPublisher` que emite los números del 1 al 10.
Implementa un `Subscriber<Integer>` personalizado que:
- En `onSubscribe` solicite todos los elementos (`request(Long.MAX_VALUE)`)
- En `onNext` imprima cada número
- En `onComplete` imprima "Stream completado. Total recibidos: N"
- En `onError` imprima el mensaje del error

## Ejercicio 2 — Pipeline strings: filter → map → print
Implementa una cadena reactiva sobre una lista de strings usando `SubmissionPublisher` y un `Processor`.
El pipeline debe: recibir strings → mantener solo las de longitud > 3 → convertir a mayúsculas → imprimir.
Datos de entrada: `["hi", "java", "go", "reactive", "streams", "ok", "flow"]`.

## Ejercicio 3 — Mono con éxito y con error
Simula la clase `Mono<T>` con éxito y con error (no uses Project Reactor, usa callbacks propios).
Crea `Mono.just("usuario-42")` y demuestra el flujo: `onNext → onComplete`.
Crea `Mono.error(new RuntimeException("no encontrado"))` y demuestra el flujo: `onError`.
Muestra que `onErrorReturn("anonimo")` transforma el error en un valor por defecto.

## Ejercicio 4 — SubmissionPublisher con múltiples subscribers
Usa `SubmissionPublisher<String>` y suscribe **tres** subscribers independientes.
Emite 5 mensajes: `"msg-1"` hasta `"msg-5"`.
Cada subscriber debe imprimirse con su propio prefijo (`[Sub-A]`, `[Sub-B]`, `[Sub-C]`).
Muestra que los tres reciben exactamente los mismos mensajes de forma independiente.

## Ejercicio 5 — Combinar dos publishers con zip
Implementa una operación `zip` entre dos listas:
- Lista A: `["Ana", "Bob", "Carlos", "Diana"]`
- Lista B: `[85, 92, 78, 95]` (puntuaciones)
El zip debe producir pares `"Ana → 85"`, `"Bob → 92"`, etc.
Si las listas tienen distinta longitud, el zip para en el más corto.

## Ejercicio 6 — Timeout con valor por defecto
Implementa un publisher que simula una operación lenta: espera `sleepMs` milisegundos antes de emitir un valor.
Implementa un método `withTimeout(long limitMs, T defaultValue)` que:
- Si el publisher emite antes del límite: retorna el valor real
- Si se supera el límite: retorna el valor por defecto
Prueba con sleep=50ms / timeout=100ms (debe devolver valor real) y sleep=200ms / timeout=100ms (debe devolver default).
