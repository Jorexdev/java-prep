# Ejercicios — 27 DevOps Docker

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Container resource limits**
Crea `Container(name, cpuShares, memoryMB)` y `Node(name, cpuTotal, memoryMB)` con lista de containers. Implementa `scheduleContainer(container)` que valida si caben los recursos. Si la suma de memoria de todos los containers supera la capacidad del nodo, detecta OOM y simula el OOM kill eliminando el container con mayor memoria. Muestra el estado del nodo antes y después del OOM kill.

---

**Ejercicio 2 — Image registry**
Crea `Registry` con estructura `Map<String, Map<String, DockerImage>>` (repositorio → tag → imagen). Implementa `push(repo, tag, image)`, `pull(repo, tag)`, `listTags(repo)` y `delete(repo, tag)`. Detecta imágenes "dangling": imágenes que pierden su tag al hacer push de una nueva imagen con el mismo repositorio y tag (la tag queda sin referencia). Muestra las imágenes dangling al final.

---

**Ejercicio 3 — Layer deduplication**
Múltiples imágenes pueden compartir layers con el mismo hash. Crea `LayerStore(Map<String, Layer>)` donde la clave es el hash del layer. Implementa `addImage(name, layers)` y `diskUsage()` que calcula el espacio real (sin contar layers duplicados). Demo con 3 imágenes que comparten 2 layers entre ellas. Muestra el uso de disco real vs el uso sin deduplicación.

---

**Ejercicio 4 — Rolling update**
Crea `Service(name, replicas, currentImage)` con lista de `Container`. Implementa `rollingUpdate(newImage, healthCheckFn)`: reemplaza las replicas una a una — levanta nueva, espera health check (80% éxito simulado con `Random`), elimina la vieja. Si el health check falla, realiza rollback completo al estado anterior. Demo con 3 replicas donde la replica 2 falla el health check y se activa el rollback.

---

**Ejercicio 5 — Multi-stage build pipeline**
Simula un multi-stage build: la etapa `builder` compila el código y produce artefactos (jar, dependencias). La etapa `runner` parte de una imagen base limpia y copia únicamente los artefactos necesarios del builder. Implementa `BuildStage(name, baseImage, List<String> commands)` con `copyFrom(sourceStage, artifact)`. El stage final solo debe contener los artefactos copiados, no las herramientas de compilación. Demo con etapa builder (jdk:21, 450 MB + artefactos) y etapa runner (jre:21-slim, 85 MB) que copia solo `app.jar`. Calcula y muestra la reducción de tamaño final respecto al builder.

---
