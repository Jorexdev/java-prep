<div align="center">
  <a href="#"><img src="../../assets/modules/banner-16-hilos-virtuales-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Los **Virtual Threads** (Java 21, JEP 444) son hilos ligeros gestionados por la JVM, no por el sistema operativo. Permiten crear millones de hilos concurrentes sin la sobrecarga de los hilos de plataforma tradicionales.

Un hilo de plataforma tradicional tiene un costo fijo (~1MB de stack, syscall para creación). Un virtual thread es muchísimo más ligero: la JVM los gestiona internamente usando un pool de **carrier threads** (hilos del OS) sobre los que los virtual threads se montan y desmontan automáticamente.

```java
// Crear un virtual thread
Thread.ofVirtual().start(() -> hacerTarea());

// ExecutorService con virtual threads
try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
    exec.submit(() -> llamadaRed());
}
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Virtual Thread vs Hilo de Plataforma:**

| | Plataforma | Virtual |
|---|---|---|
| Gestionado por | OS | JVM |
| Costo de creación | Alto (~1MB stack) | Muy bajo |
| Número máximo | Miles | Millones |
| Ideal para | CPU-bound | I/O-bound |
| Bloqueo | Bloquea el OS thread | Solo bloquea el virtual thread |

**Concepto clave — Pinning:**
Un virtual thread queda "pinned" (anclado a su carrier thread) cuando:
- Está dentro de un bloque `synchronized`.
- Ejecuta código nativo.

El pinning reduce la escalabilidad. La solución es usar `ReentrantLock` en lugar de `synchronized` cuando sea posible en código que usa virtual threads.

**Virtual Threads NO mejoran tareas CPU-bound:** para procesamiento intensivo, el límite es el número de cores del hardware — más hilos no ayudan. Son ideales para tareas I/O-bound donde el hilo pasa la mayor parte del tiempo esperando.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Escalabilidad masiva para aplicaciones I/O-bound (servidores web, microservicios).
- Código bloqueante sin penalización: escribes código secuencial simple y escala.
- Compatible con el ecosistema existente de Java.
- Sin cambio de paradigma: mismo modelo de hilos, sin callbacks ni reactive.

Ver [ExpVirtualThreads.java](ExpVirtualThreads.java) para ejemplos de creación, comparación con hilos de plataforma y uso con ExecutorService.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
