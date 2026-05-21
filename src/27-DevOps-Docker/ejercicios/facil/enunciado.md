# Ejercicios — 27 DevOps Docker

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Image layers**
Crea la clase `Layer(name, command, sizeMB)` y `DockerImage(List<Layer>)`. Simula una imagen con las capas FROM→RUN→COPY→RUN. Muestra cada capa con su tamaño en MB y el tamaño total de la imagen.

---

**Ejercicio 2 — Container lifecycle**
Crea `Container(image, name)` con estados `CREATED→RUNNING→PAUSED→STOPPED→REMOVED`. Implementa las transiciones con validación: no se puede pausar un container en estado STOPPED, no se puede iniciar uno ya RUNNING, etc. Demo con ciclo de vida completo.

---

**Ejercicio 3 — Port mapping**
Crea `PortMapping(hostPort, containerPort, protocol)` y un `Container` que acepta una lista de mappings. Detecta conflictos: dos containers no pueden tener el mismo `hostPort` activo. Muestra la tabla de puertos de cada container y señala los conflictos.

---

**Ejercicio 4 — Environment variables**
Crea `DockerImage` con `Map<String,String> defaultEnv` y `Container` con `Map<String,String> envOverrides`. Simula herencia: el container hereda las env vars de la imagen pero puede sobreescribirlas. Muestra el entorno resuelto final del container.

---

**Ejercicio 5 — Volume mounts**
Crea `VolumeMount(hostPath, containerPath, readOnly)`. Modela varios containers, cada uno con su lista de mounts. Detecta conflictos: dos containers no pueden montar el mismo `hostPath` en modo escritura al mismo tiempo. Muestra los conflictos encontrados.

---

**Ejercicio 6 — Dockerfile parser**
Parsea un String multi-línea que simula un Dockerfile con instrucciones FROM, RUN, COPY, EXPOSE, ENV y CMD. Construye un objeto `DockerImage` con la lista de capas generadas por cada instrucción. Demo con un Dockerfile de 8 líneas.
