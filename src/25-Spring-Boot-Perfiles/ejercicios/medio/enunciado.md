# Ejercicios — 25 Spring Boot: Perfiles

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Profile groups**
Implementa un `ProfileGroupRegistry` donde el perfil "prod" expande automáticamente a `["prod-db", "prod-cache", "prod-security"]`. Al activar "prod", todos sus sub-perfiles deben considerarse activos. Demuestra la expansión y verifica qué beans se activan.

**Ejercicio 2 — Config overlay**
Simula un `application.properties` base con claves como `app.name`, `app.timeout`, `app.debug`. Crea un `application-dev.properties` que sobreescribe `app.timeout` y `app.debug`. Implementa la fusión: las propiedades del perfil activo ganan sobre las base. Demuestra con perfil "dev" y sin perfil.

**Ejercicio 3 — Beans por perfil con herencia**
Define `BaseServicio` con un método `procesar()`. Crea `DevServicio` (perfil "dev") y `ProdServicio` (perfil "prod") que extienden `BaseServicio` con comportamientos distintos. Implementa un contenedor que registra la implementación correcta según el perfil y permite cambiarla en runtime.

**Ejercicio 4 — Test profile**
Simula un `EmailService` real que "envía emails" (imprime con detalle). Crea `FakeEmailService` para perfil "test" que solo loguea el intento sin enviar. Implementa un `ServiceContainer` que usa el servicio correcto según perfil. Demuestra que en perfil "test" no se "envían" emails reales.

**Ejercicio 5 — Profile expression**
Implementa el soporte para expresiones de perfil del estilo `"prod & !debug"`: un bean se activa si el perfil contiene "prod" Y NO contiene "debug". Parsea expresiones con operadores `&` y `!`. Demuestra con los combos: `["prod"]`, `["prod","debug"]`, `["dev"]`, `["prod","extra"]`.
