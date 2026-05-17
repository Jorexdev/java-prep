<div align="center">
  <a href="#"><img src="../../assets/modules/banner-17-recolector-basura-v1.svg" width="100%" alt=""/></a>
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

El **Garbage Collector (GC)** de la JVM libera automáticamente la memoria de objetos que ya no tienen referencias vivas. El programador no gestiona la memoria manualmente — el GC la recupera cuando considera necesario.

El heap se organiza en **generaciones** basándose en la hipótesis generacional: *la mayoría de objetos mueren jóvenes*.

```
Heap
├── Young Generation
│   ├── Eden Space         ← nuevos objetos
│   └── Survivor Spaces    ← objetos que sobreviven GC menor
└── Old Generation (Tenured) ← objetos de larga vida
Metaspace                  ← metadata de clases (fuera del heap, Java 8+)
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

**Tipos de GC:**

| Evento | Ámbito | Frecuencia | Impacto |
|---|---|---|---|
| Minor GC | Young Generation | Frecuente | Rápido |
| Major GC | Old Generation | Infrecuente | Más lento |
| Full GC | Todo el heap | Poco frecuente | Stop-the-world largo |

**Algoritmos de GC:**

| GC | Desde | Característica |
|---|---|---|
| Serial GC | Java 1 | Un hilo. Solo apps pequeñas. |
| Parallel GC | Java 5 | Multi-hilo. Throughput. |
| G1GC | Java 9 (default) | Regiones. Predecible. |
| ZGC | Java 15+ | Pausas <10ms. Low-latency. |
| Shenandoah | Java 12+ | Pausas mínimas. Red Hat. |

**Stop-the-world:** durante la recolección, la JVM pausa todos los hilos de aplicación. G1 y ZGC minimizan estas pausas.

**Flags JVM útiles:**
```
-Xms512m -Xmx2g      # heap mínimo/máximo
-XX:+UseG1GC         # fuerza G1
-XX:+UseZGC          # fuerza ZGC (Java 15+)
-verbose:gc          # log de GC
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Sin gestión manual de memoria: no hay `free()` ni `delete`.
- G1GC equilibra throughput y latencia con pausas predecibles.
- ZGC para aplicaciones que necesitan latencias muy bajas (<10ms).
- Entender el GC ayuda a evitar memory leaks (referencias innecesariamente largas).

Ver [ExpGarbageCollector.java](ExpGarbageCollector.java) para ejemplos de ciclo de vida de objetos, referencias y comportamiento del GC.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
