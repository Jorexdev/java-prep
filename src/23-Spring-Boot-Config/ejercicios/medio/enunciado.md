# Ejercicios — 23 Spring Boot: Config
## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — @ConfigurationProperties nested**

Simula el binding de `@ConfigurationProperties` con objetos anidados.
Define `DatabaseConfig { String url; int poolSize; }` y `CacheConfig { boolean enabled; long ttlSeconds; }`.
Define `AppConfig { DatabaseConfig db; CacheConfig cache; }`.
Implementa un binder que pueble `AppConfig` desde un `Map<String, String>` con claves
`app.db.url`, `app.db.poolSize`, `app.cache.enabled`, `app.cache.ttlSeconds` usando reflection.

---

**Ejercicio 2 — Property source priority chain (4 fuentes)**

Amplía el ejercicio fácil 5 a 4 fuentes:
`defaultValues` < `propertiesFile` < `envVars` < `cliArgs` (los args del main).
Implementa `PropertySourceChain` con estas 4 fuentes.
Demuestra con la misma clave (`server.port`) definida en las 4 fuentes: la de CLI siempre gana.
Simula CLI args parseando `["--server.port=443", "--app.name=cli-app"]`.

---

**Ejercicio 3 — Config validation**

Define `@Min`, `@Max`, `@NotNull` como anotaciones personalizadas.
Implementa `ConfigValidator.validate(Object)` que usa reflection para revisar los campos
anotados. Si hay errores, lanza `ConfigValidationException(List<String> errors)`.
Demo con `ServerConfig { @NotNull String host; @Min(1) @Max(65535) int port; }`:
validación exitosa con config válida e inválida (port=0 y port=70000).

---

**Ejercicio 4 — Profile-specific overlay**

Implementa `ProfileAwareConfig` que recibe un `String profile` y dos maps:
`baseProps` (siempre activo) y un map de perfiles `Map<String, Map<String,String>>`.
`load(profile)` fusiona: las propiedades del perfil sobreescriben las base.
Demo cargando "dev" y "prod" sobre la misma base. Mostrar las diferencias.

---

**Ejercicio 5 — Relaxed binding**

Implementa `RelaxedBinder.normalize(String envVar)` que convierte env vars estilo
`MY_APP_DB_HOST` al formato de propiedades `myApp.db.host`.
Regla: dividir por `_`, primera palabra en minúsculas con camelCase del resto dentro de cada token,
unir con `.`.
Demo con 5 env vars: `MY_APP_DB_HOST`, `SERVER_PORT`, `SPRING_DATASOURCE_URL`,
`APP_FEATURE_FLAG_ENABLED`, `LOG_LEVEL`.
