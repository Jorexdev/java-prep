# Ejercicios — 23 Spring Boot: Config
## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Properties reader**

Implementa `PropertiesSource` que recibe un `Map<String, String>` en su constructor.
Expone dos métodos: `get(String key)` que devuelve el valor o lanza excepción si no existe,
y `get(String key, String defaultValue)` que devuelve el valor o el default.
Demo con 5 propiedades incluyendo llamadas con claves que no existen.

---

**Ejercicio 2 — @Value simulado**

Crea una `@interface Value` personalizada con atributo `String value()`.
Implementa `ValueInjector` que usa reflection para encontrar los campos de una clase
anotados con `@Value` e inyectar el valor correspondiente del Map de propiedades.
Demo con clase `AppConfig` que tiene 4 campos anotados.

---

**Ejercicio 3 — Type conversion**

Implementa `TypedConfig` que recibe un `Map<String, String>` y expone:
- `getInt(key)` → String a Integer
- `getBoolean(key)` → String a Boolean ("true"/"false")
- `getList(key)` → String a `List<String>` separando por coma
- `getDuration(key)` → String a segundos: "5s" → 5, "2m" → 120

Demo los 4 tipos con valores reales.

---

**Ejercicio 4 — Property placeholder**

Implementa `resolvePlaceholders(String template, Map<String, String> props)` que
sustituye `${key}` por el valor del mapa. Soporta que un valor puede referenciar
a otra propiedad (resolver de forma recursiva hasta estabilizar).
Demo con `"${app.name}-${app.version}"` y `app.name=mi-app`, `app.version=1.0`.

---

**Ejercicio 5 — Config precedence**

Implementa `PropertySourceChain` con 3 fuentes de propiedades en orden de prioridad:
`defaults` (más baja) < `propertiesFile` < `envVars` (más alta).
`get(key)` devuelve el valor de la fuente más prioritaria que contenga la clave.
Demo con la misma clave definida en distintas fuentes mostrando cuál gana.

---

**Ejercicio 6 — Prefix binding**

Implementa `PrefixBinder.bind(String prefix, Object target, Map<String, String> props)`.
Dado un prefijo como `"server"`, busca en el mapa claves como `server.host`, `server.port`,
`server.timeout` y las inyecta por reflection en los campos `host`, `port`, `timeout`
de la instancia `target`. Demo con clase `ServerConfig` con esos 3 campos.
