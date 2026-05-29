# Ejercicios — 23 Spring Boot: Config
## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Remote config source**

Implementa `RemoteConfigSource` que simula descargar propiedades con 50ms de delay
(usando `Thread.sleep`). Integra esta fuente en `PropertySourceChain`.
Agrega un método `refresh()` que recarga las propiedades (simula un nuevo fetch remoto).
Demo: mostrar la latencia al cargar por primera vez y al hacer refresh.

---

**Ejercicio 2 — Encrypted properties**

Implementa `EncryptedPropertySource` que detecta valores con prefijo `ENC(...)`.
Los valores encriptados se desencriptan usando ROT13. Los demás pasan tal cual.
Implementa también `encrypt(String plain)` para generar los valores ENC(...).
Demo con 3 propiedades: 2 encriptadas (`db.password`, `api.key`), 1 plain (`app.name`).
Mostrar el proceso de encriptar y desencriptar.

---

**Ejercicio 3 — Config watcher**

Implementa `ConfigWatcher` que recibe dos snapshots de `Map<String, String>`:
el estado anterior y el nuevo. Compara ambos y emite `PropertyChangedEvent(key, oldVal, newVal)`
para cada propiedad que haya cambiado (valor diferente, clave añadida, o clave eliminada).
Demo con 5 propiedades donde 3 cambian entre snapshot 1 y snapshot 2.

---

**Ejercicio 4 — Deep merge YAML**

Representa una estructura YAML como `Map<String, Object>` anidada donde los valores
pueden ser `String` u otros `Map<String, Object>`.
Implementa `deepMerge(Map base, Map override)`: las claves del override sobreescriben base,
pero si ambas tienen un `Map` en la misma clave, se fusionan recursivamente (en lugar de reemplazar).
Las ramas que solo existen en base se heredan.
Demo con estructuras de 3 niveles de profundidad.

---

**Ejercicio 5 — ConfigurationProperties con listas, mapas anidados y refresh dinámico**
Implementa `ComplexBinder` que puebla `AppProperties` desde un `Map<String,String>` con patrones: lista simple (`prefix[0]`, `prefix[1]`...), lista de objetos (`prefix[0].host`, `prefix[0].port`...), mapa de strings (`prefix.key`), mapa de booleanos y mapa de listas (`prefix.role[0]`). `AppProperties` contiene `List<String> allowedOrigins`, `List<ServiceEndpoint> endpoints`, `Map<String,String> customHeaders`, `Map<String,List<String>> rolePermissions`, `DataSourceProperties` (con su propia lista y mapa), y `FeatureFlags` (con mapa de experimentos). `RefreshableConfig` envuelve el binder y permite `load(props)` con notificación a listeners al refrescar (simula `@RefreshScope`). Demo: carga `dev` y luego hace refresh a `prod`, mostrando las diferencias clave.

---
