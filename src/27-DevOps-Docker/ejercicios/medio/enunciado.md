# Ejercicios — 27 DevOps Docker

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Multi-stage build**
Crea `BuildStage(name, baseImage, commands, artifacts)`. El stage final puede copiar artefactos de stages previos con una operación `copyFrom(stageName, artifact)`. Simula una build de dos stages: "builder" produce `app.jar` (350 MB), "runtime" copia solo el jar y queda en 80 MB. Muestra la reducción de tamaño entre el stage builder y el final.

---

**Ejercicio 2 — docker-compose startup order**
Crea `Service(name, image, dependsOn, ports)`. A partir de una lista de servicios con dependencias (`db`, `cache`, `api` depende de db y cache, `web` depende de api), calcula el orden topológico de startup. Si existe un ciclo de dependencias, lanza un error descriptivo. Muestra el orden de inicio calculado.

---

**Ejercicio 3 — Layer cache**
Modela una build de imagen con 5 layers. Al hacer un rebuild, si un layer tiene el mismo comando que en la build anterior, márcalo como `CACHED`. Cuando un layer cambia, invalida ese layer y todos los siguientes. Demo: los layers 1-2 son iguales, el layer 3 cambia (nuevo comando), los layers 4-5 deben ser `REBUILT`. Muestra el resultado de cada layer en el rebuild.

---

**Ejercicio 4 — Health check**
Crea `HealthCheck(command, intervalMs, timeoutMs, retries)`. Simula que el container ejecuta el health check en un bucle: algunos intentos fallan (`Supplier<Boolean>` configurable). Si se acumulan `retries` fallos consecutivos, el container pasa a estado `UNHEALTHY`. Demo con 5 intentos donde el 2.º, 3.º y 4.º fallan (3 consecutivos → UNHEALTHY).

---

**Ejercicio 5 — Network bridge**
Crea `DockerNetwork(name)` con lista de containers. Implementa `canCommunicate(c1, c2)`: devuelve `true` solo si ambos containers están en la misma red. Demo con 3 redes: `frontend` (web, api), `backend` (api, db), `isolated` (monitor). Verifica que web↔api=true, web↔db=false, api↔db=true.
