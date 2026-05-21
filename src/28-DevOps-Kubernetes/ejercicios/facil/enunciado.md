# Ejercicios — 28 DevOps Kubernetes

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Pod lifecycle**
Crea `Pod(name, namespace, image, Map<String,String> labels)` con estados `PENDING→RUNNING→SUCCEEDED/FAILED/CRASHLOOPBACKOFF`. Implementa las transiciones y simula el ciclo de vida de 4 pods distintos, incluyendo uno que falla y uno que entra en CrashLoopBackOff.

---

**Ejercicio 2 — ReplicaSet**
Crea `ReplicaSet(name, desired, podTemplate)` con una lista de pods activos. Implementa `reconcile()`: si hay menos pods que el deseado, crea nuevos; si hay más, elimina el exceso. Simula la eliminación de un pod y verifica que `reconcile()` lo repone automáticamente.

---

**Ejercicio 3 — Service load balancer**
Crea `KubeService(name, selector, port)` con un selector de labels (`Map<String,String>`). Dados varios pods con labels, el servicio filtra los pods que coincidan con el selector. Implementa `route()` con round-robin entre los pods `RUNNING`. Demo con 3 pods donde uno está en estado `PENDING`.

---

**Ejercicio 4 — ConfigMap**
Crea `ConfigMap(name, Map<String,String> data)`. Un Pod puede montar el ConfigMap de dos formas: (a) como variables de entorno (cada clave del ConfigMap se convierte en env var) o (b) como archivos simulados (cada clave es una ruta, el valor es el contenido). Demo ambos modos de montaje.

---

**Ejercicio 5 — Resource fit**
Crea `Pod(name, cpuRequest, memRequest)` y `Node(name, cpuCapacity, memCapacity)` con lista de pods en ejecución. Implementa `canSchedule(pod)` que retorna `true` si los recursos libres del nodo son suficientes. Demo con un nodo al 80% de capacidad y varios pods de distintos tamaños intentando ser schedulados.

---

**Ejercicio 6 — Namespace isolation**
Crea `Namespace(name)` con lista de pods. Implementa `canCommunicate(pod1, pod2)`: retorna `false` si los pods están en namespaces distintos y no existe un `KubeService` que los conecte. Demo con 2 namespaces (`frontend` y `backend`) y una comunicación bloqueada y otra permitida vía servicio.
