<div align="center">
  <a href="#"><img src="../../assets/modules/banner-17-recolector-basura-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Cuáles son las generaciones del heap de Java?**
Young Generation (Eden + dos Survivor spaces), Old Generation (Tenured) y Metaspace (fuera del heap, para metadata de clases). La hipótesis generacional asume que la mayoría de objetos mueren en la Young Generation — por eso se recoge más frecuentemente y con menos coste.

---

**¿Qué diferencia hay entre Minor GC y Major GC?**
Minor GC recoge solo la Young Generation: rápido y frecuente. Major GC (o Full GC) recoge también la Old Generation: más lento y puede causar pausas largas (stop-the-world). Un Full GC se puede desencadenar cuando la Old Generation está casi llena.

---

**¿Qué es G1GC y en qué mejora respecto al GC clásico?**
G1 (Garbage-First) divide el heap en regiones en lugar de zonas fijas. Recoge primero las regiones con más basura (Garbage-First). Ofrece pausas más predecibles y configurables con `-XX:MaxGCPauseMillis`. Es el GC por defecto desde Java 9.

---

**¿Puedes forzar la ejecución del GC?**
`System.gc()` y `Runtime.getRuntime().gc()` son sugerencias — la JVM puede ignorarlas. No hay garantía de que el GC se ejecute inmediatamente. En producción no se recomienda llamarlo manualmente; interfiere con las heurísticas del GC.

---

**¿Qué son las "stop-the-world" pauses?**
Pausas en las que la JVM detiene todos los hilos de aplicación para realizar trabajo de recolección de forma segura (el heap no puede cambiar mientras se recolecta). GC modernos como ZGC y Shenandoah reducen las stop-the-world a menos de 10ms incluso en heaps de varios GB.

---

**¿Qué diferencia hay entre heap y metaspace?**
El heap almacena instancias de objetos y arrays. Metaspace (Java 8+, reemplaza al PermGen) almacena metadata de clases: bytecode, descriptores de métodos, constant pool. Metaspace está fuera del heap Java y su tamaño por defecto es ilimitado (limitado por la memoria del OS).

---

**¿Cuándo elegirías ZGC en lugar de G1GC en producción?**
G1GC es el mejor punto de partida para la mayoría de aplicaciones: buen balance entre throughput y latencia, pausas predecibles y bien soportado. ZGC (Java 15+ stable) se elige cuando los requisitos de latencia son extremos: pausas por debajo de 1ms incluso con heaps de cientos de GB. Usa ZGC en servicios donde el p99 de latencia es crítico (APIs real-time, gaming, trading) y el heap es grande. En heaps pequeños o si el throughput es prioritario sobre la latencia, G1GC suele ganar.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
