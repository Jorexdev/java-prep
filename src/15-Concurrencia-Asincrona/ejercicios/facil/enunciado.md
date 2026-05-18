# Ejercicios — Concurrencia Asíncrona (Fácil)

Las soluciones están en [soluciones/](soluciones/).

## Ejercicio 1 — ExecutorService básico
Crea un ExecutorService con Executors.newFixedThreadPool(3). Envía 6 tareas que impriman su nombre y espera a que todas terminen con shutdown() + awaitTermination().

## Ejercicio 2 — Callable + Future
Usa Callable<Integer> para calcular la suma de 1 a N en un hilo separado. Envía 3 tareas con distintos N y recupera los resultados con Future.get().

## Ejercicio 3 — invokeAll
Usa executor.invokeAll() para ejecutar 5 tareas en paralelo y recoger todos los resultados de una vez. Calcula el tiempo total vs secuencial.

## Ejercicio 4 — CompletableFuture.supplyAsync + thenApply
Simula una llamada a API remota con CompletableFuture.supplyAsync (sleep 200ms). Encadena thenApply para transformar el resultado, thenAccept para consumirlo.

## Ejercicio 5 — CompletableFuture.thenCompose
Encadena dos operaciones asíncronas dependientes: buscarUsuario(id) devuelve CompletableFuture<Usuario>, luego obtenerPedidos(usuario) devuelve CompletableFuture<List<String>>. Usa thenCompose para encadenarlas.

## Ejercicio 6 — CompletableFuture.exceptionally
Crea un CompletableFuture que puede fallar. Usa exceptionally() para proporcionar un valor por defecto cuando falla, y whenComplete para loggear siempre el resultado final.
