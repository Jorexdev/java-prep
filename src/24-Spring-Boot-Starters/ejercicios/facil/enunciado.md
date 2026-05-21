# Ejercicios — 24 Spring Boot: Starters
## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — @ConditionalOnProperty**

Implementa `ConditionalContainer` que solo registra un bean `DataSource` si la propiedad
`"db.enabled"` tiene valor `"true"` en el Map de configuración.
Si la propiedad es `"false"` o está ausente, el bean no se registra.
Demo: ejecutar con `db.enabled=true` (registra) y con `db.enabled=false` (no registra).

---

**Ejercicio 2 — @ConditionalOnMissingBean**

Implementa un contenedor que registra un bean de fallback `DefaultMessageService`
solo si no hay ya un bean del mismo tipo registrado.
Demo en dos escenarios: sin bean previo (registra fallback) y con bean previo
`CustomMessageService` ya registrado (no registra fallback).

---

**Ejercicio 3 — @ConditionalOnClass**

Simula `@ConditionalOnClass` usando `Class.forName(...)`.
Intenta cargar `"com.example.Optional"` (no existe) y `"java.util.Optional"` (existe).
Si la clase existe, registrar un bean asociado; si no, omitirlo con un mensaje.
Demo mostrando ambos casos.

---

**Ejercicio 4 — Auto-config order**

Define 3 auto-configs: cada una tiene un nombre y una lista de dependencias
(otras auto-configs que deben ejecutarse antes).
Implementa el ordenamiento y ejecución en el orden correcto.
Demo: `WebConfig` depende de `JacksonConfig`, `JacksonConfig` no depende de nada,
`SecurityConfig` depende de `WebConfig`.

---

**Ejercicio 5 — Starter composition**

Implementa `StarterActivator` donde el starter `"web"` activa automáticamente
los starters `"jackson"` y `"tomcat"` al ser activado.
Cada starter tiene una lista de dependencias que también se activan recursivamente.
Demo: activar `"web"` y mostrar todos los starters activos incluyendo los transitivos.

---

**Ejercicio 6 — Default beans**

Implementa `WebMvcAutoConfig` que registra `ObjectMapper`, `MessageConverter`
y `ExceptionResolver` en un contenedor solo si no existen ya.
Demo en dos escenarios: sin beans propios (registra los 3 defaults) y con
`ObjectMapper` ya registrado (registra los otros 2, pero no sobreescribe ObjectMapper).
