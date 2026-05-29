# Reactive — Ejercicios Medio

Ejercicios intermedios: hot publishers, backpressure con subscriber lento, flatMap concurrente, retry con backoff, pipelines async, Processor.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Hot publisher simulado
Implementa un `HotPublisher<T>` donde los subscribers que se registran tarde **solo reciben eventos futuros**, no los pasados.
- El publisher tiene un método `emit(T item)` que distribuye a todos los subscribers registrados **en ese momento**
- Registra Sub-1 antes de emitir los primeros 3 eventos
- Registra Sub-2 después de los primeros 3 eventos
- Emite 3 eventos más (Sub-1 y Sub-2 deben recibir estos 3)
- Imprime cuántos eventos recibió cada subscriber y explica la diferencia con un cold publisher

## Ejercicio 2 — Backpressure: subscriber lento vs publisher rápido
Simula un publisher que emite 10 elementos, uno cada 20ms (productor rápido).
El subscriber procesa 1 elemento por segundo (consumidor lento).
Implementa la gestión de backpressure con estrategia BUFFER (capacidad 5):
- El subscriber solicita 1 elemento a la vez
- Cuando el buffer se llena (capacidad 5) los elementos sobrantes se descartan con aviso
- Al final imprime: recibidos, descartados, tiempo total

## Ejercicio 3 — FlatMap concurrente con merge de resultados
Dado un Flux de IDs `[1, 2, 3, 4, 5]`, por cada ID lanza una "consulta async" (simulada con `CompletableFuture` + sleep aleatorio 10-50ms) que devuelve `"resultado-N"`.
Implementa `flatMapConcurrent` que:
- Lanza todas las sub-consultas en paralelo (no esperar a que termine una para empezar la siguiente)
- Hace merge de los resultados en el orden en que llegan
- Al final imprime el orden de llegada y el tiempo total (debe ser ~50ms, no 250ms)

## Ejercicio 4 — Retry con backoff exponencial
Implementa una llamada a un "servicio externo" que falla las 3 primeras veces y tiene éxito a la 4ª.
Usa retry con backoff exponencial: espera 100ms, 200ms, 400ms entre intentos.
Implementa `retryWithBackoff(Supplier<T> operation, int maxRetries, long initialDelayMs)`.
Muestra: intento número, espera antes del reintento, resultado final.
Si se agotan los reintentos, propaga la última excepción.

## Ejercicio 5 — Pipeline de transformación async
Implementa un pipeline de 4 etapas donde cada una puede ser async:
1. **Leer**: genera una lista de "eventos" `{id, tipo, payload}` (pueden ser objetos simples)
2. **Parsear**: valida que el payload no sea nulo/vacío; descarta los inválidos
3. **Enriquecer**: añade un campo `timestamp = System.currentTimeMillis()` a cada evento
4. **Guardar**: imprime `"Guardado: [evento enriquecido]"` simulando persistencia

Cada etapa usa `CompletableFuture` o `Flow.Processor`. Al final imprime cuántos eventos pasaron cada etapa.

## Ejercicio 6 — Processor que transforma y filtra
Implementa un `Flow.Processor<Integer, String>` llamado `FizzBuzzProcessor` que:
- Recibe enteros del 1 al 20
- Transforma: si divisible por 15 → `"FizzBuzz"`, por 3 → `"Fizz"`, por 5 → `"Buzz"`, resto → `String.valueOf(n)`
- Solo pasa al downstream los elementos que contienen la letra `"z"` (Fizz, Buzz, FizzBuzz)
- El subscriber final imprime los resultados filtrados con su posición original
