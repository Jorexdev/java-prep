# Ejercicios — 28 DevOps Kubernetes

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Scheduler**
Crea `Node(name, cpuFree, memFree, labels)`. Para cada `Pod` pendiente: (1) filtra los nodos que no cumplen los recursos o las reglas de afinidad, (2) puntúa los nodos restantes (más CPU libre = más puntos), (3) asigna el pod al nodo con mayor puntuación. Demo con 3 nodos y 5 pods con distintos requisitos.

---

**Ejercicio 2 — StatefulSet ordering**
Crea `StatefulSet(name, replicas)`. La creación de pods debe ser en orden: pod-0 espera estar `RUNNING` antes de crear pod-1, y así sucesivamente. La eliminación es en orden inverso. Simula el ciclo completo con 3 replicas usando `Thread` con sleeps cortos (50ms). Muestra los timestamps de cada evento.

---

**Ejercicio 3 — NetworkPolicy**
Crea `NetworkPolicy(podSelector, ingressRules, egressRules)`. Implementa `isAllowed(srcPod, dstPod, port)` que evalúa las políticas aplicables. Demo con 3 pods: `pod-a` puede hablar con `pod-b` en el puerto 8080, pero `pod-a` no puede hablar con `pod-c` en ningún puerto.

---

**Ejercicio 4 — Operator reconciler**
Crea `CustomResource(kind, name, spec)` con un estado deseado. El `Reconciler` compara el estado deseado (`desired`) con el estado actual (`actual`) y emite una lista de `Action(CREATE/UPDATE/DELETE, resourceName, detail)`. Simula 3 ciclos de reconciliación donde entre ciclos se añaden, modifican y eliminan recursos del estado deseado.
