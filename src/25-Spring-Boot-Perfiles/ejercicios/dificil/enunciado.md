# Ejercicios — 25 Spring Boot: Perfiles

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Dynamic profile switching**
Implementa un `ProfileContext` que permite cambiar el perfil activo en runtime. Los beans registrados implementan una interfaz `ProfileAware` con método `onProfileChange(String newProfile)`. Al cambiar el perfil, el `ProfileContext` notifica a todos los beans registrados para que se reinicialicen. Demuestra el cambio de "dev" a "prod" con al menos 3 beans.

**Ejercicio 2 — Profile inheritance**
Diseña un sistema de herencia de perfiles donde: `prod-eu` hereda de `prod`, y `prod` hereda de `base`. Cada nivel define propiedades que se fusionan con prioridad: `prod-eu` > `prod` > `base`. Implementa la resolución de la cadena de herencia y muestra el config final resuelto para `prod-eu`.

**Ejercicio 3 — Feature flags via profiles**
Crea un `FeatureManager` con un mapa de `feature → Set<String> perfilesRequeridos`. `isEnabled(String feature)` devuelve true solo si el perfil activo intersecta con los perfiles requeridos. Define 5 features con distintos perfiles. Demuestra con 4 perfiles distintos activos qué features están habilitadas en cada caso.

**Ejercicio 4 — Environment abstraction**
Implementa una clase `Environment` con los métodos: `getProperty(String key)`, `getActiveProfiles()`, `getDefaultProfiles()`, y `acceptsProfiles(String... profiles)`. Los beans la inyectan (simulado) para adaptar su comportamiento. Define 3 beans que usan `Environment` de formas distintas: uno cambia su log level, otro su datasource, otro sus timeouts. Demuestra con perfiles "dev", "prod" y "test".

---

**Ejercicio 5 — Multi-environment config con precedencia y override**
Implementa `MultiEnvConfig` que acepta múltiples fuentes con `addSource(Map<String,String>, Priority)`. `Priority` es un enum con 6 niveles: `DEFAULTS < SHARED_ENV < PROFILE_ENV < ENV_VARS < SYSTEM_PROPS < CLI_ARGS`. `get(String key)` devuelve el valor de la fuente con mayor prioridad. `showPrecedence(String key)` imprime todas las fuentes que definen esa clave, marcando cuál gana. `buildForEnvironment(String profile)` popula las 6 capas con propiedades realistas para `dev`, `staging` y `prod`. Demo: tabla comparativa de 6 propiedades clave entre los 3 entornos, y análisis de precedencia para 5 propiedades en `prod` mostrando qué fuente gana y cuáles son overrideadas.

---
