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
