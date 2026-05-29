# Ejercicios — 28 DevOps Kubernetes

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Rolling update**
Crea `Deployment(name, replicas, image)`. Implementa rolling update con `maxSurge=1` y `maxUnavailable=0`: por cada replica, primero crea el nuevo pod y espera que esté `RUNNING`, luego elimina el viejo. Repite para cada replica. Demo con 3 replicas actualizando de `v1.0` a `v2.0`.

---

**Ejercicio 2 — Liveness probe**
Crea `LivenessProbe(failureThreshold, periodMs)`. Simula que el pod ejecuta la probe en un bucle. Si falla `failureThreshold` veces consecutivas, el pod se reinicia (vuelve a estado `RUNNING`). Demo con un pod que falla de forma intermitente durante 10 ciclos.

---

**Ejercicio 3 — HPA (Horizontal Pod Autoscaler)**
Crea `HPA(minReplicas=1, maxReplicas=5, targetCPUPercent=70)`. Recibe una lista de métricas de CPU simuladas (porcentajes). Si la CPU promedio > 70%, escala up (añade replica). Si < 30%, escala down (elimina replica). Simula 10 ciclos con métricas variables y muestra cómo el número de replicas cambia.

---

**Ejercicio 4 — Secrets**
Crea `Secret(name, Map<String,String> data)` donde los valores se almacenan "encriptados" como el string al revés. El pod los lee y los decodifica al montarlos. Demo con 3 credenciales (usuario, contraseña, token API) que el pod monta y muestra decodificadas.

---

**Ejercicio 5 — Pod affinity**
Crea `NodeAffinityRule(labelKey, values, operator)` donde operator puede ser `IN` o `NOT_IN`. El scheduler solo asigna el pod a nodos que cumplen todas las reglas. Demo con 3 nodos (diferentes zones y tipos) y 2 pods con reglas de afinidad distintas. Muestra en qué nodo queda schedulado cada pod.

---

**Ejercicio 6 — HPA con cooldown**
Extiende `HPA(minReplicas, maxReplicas, targetCPUPercent, scaleUpCooldownMs, scaleDownCooldownMs)`. Implementa que tras un scale-up no se puede volver a escalar hasta que haya pasado el cooldown de scale-up, y lo mismo para scale-down. Simula 12 ciclos con métricas de CPU que provocan scale-up y scale-down. Muestra si la acción se ejecutó o fue bloqueada por cooldown.

---
