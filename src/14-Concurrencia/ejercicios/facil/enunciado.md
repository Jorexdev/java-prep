# Ejercicios — Concurrencia (Fácil)

Las soluciones están en [soluciones/](soluciones/).

## Ejercicio 1 — Crear y arrancar hilos
Crea 3 hilos con Thread y Runnable que impriman su nombre y un contador del 1 al 3. Demuestra que el orden de ejecución es no determinista.

## Ejercicio 2 — synchronized: contador seguro
Implementa un contador con método `incrementar()` sin sincronización y demuestra la race condition. Luego corrígelo con `synchronized`. Usa 10 hilos que incrementen 1000 veces cada uno.

## Ejercicio 3 — volatile
Implementa un flag de parada `volatile boolean corriendo = true`. Un hilo lee el flag en bucle, el hilo principal lo pone a false tras 100ms. Sin volatile, el hilo podría no ver el cambio.

## Ejercicio 4 — wait/notify: productor-consumidor básico
Implementa un buffer de un solo elemento con `wait()` y `notify()`. El productor espera si hay un elemento; el consumidor espera si está vacío.

## Ejercicio 5 — Thread.sleep vs Thread.join
Crea 3 hilos que tarden tiempos distintos (200ms, 100ms, 300ms). Usa join() para esperar a que todos terminen antes de imprimir "todos completados".

## Ejercicio 6 — AtomicInteger
Reimplementa el contador del ejercicio 2 usando AtomicInteger. Compara con synchronized en claridad y rendimiento.
