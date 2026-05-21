# Ejercicios — 25 Spring Boot: Perfiles

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Profile básico**
Define una interfaz `DataSource` con un método `getUrl()`. Crea dos implementaciones: `H2DataSource` (perfil "dev") y `PostgresDataSource` (perfil "prod"). Implementa una clase `ProfileSelector` que, dado un String con el perfil activo, instancia y muestra la implementación correcta.

**Ejercicio 2 — Default profile**
Añade una tercera implementación `DefaultDataSource` que se usa cuando no hay perfil activo. Demuestra los tres casos: perfil "dev", perfil "prod", y sin perfil (null o vacío).

**Ejercicio 3 — Profile-specific config**
Crea dos mapas de configuración: `configDev` y `configProd` con claves como `db.url`, `db.pool`, `cache.enabled`. Implementa `ProfileConfig.getConfig(String profile)` que devuelve el mapa correcto según el perfil. Ejecuta mostrando ambos perfiles.

**Ejercicio 4 — Multiple active profiles**
Simula una lista de perfiles activos (`["dev", "debug"]`). Define una clase `FeatureFlags` con perfiles requeridos para activarse. Demuestra varios combos: flags que se activan con "dev", con "debug", con ambos, y con "prod".

**Ejercicio 5 — @Profile negación simulada**
Implementa `ProfileCondition(String profile, boolean negate)`. Si `negate` es false, el bean se activa cuando el perfil coincide. Si `negate` es true, se activa cuando el perfil NO coincide. Demuestra con el perfil "prod" y sus negaciones.

**Ejercicio 6 — SPRING_PROFILES_ACTIVE**
Lee el perfil activo desde `System.getProperty("spring.profiles.active")` con fallback a "dev" si la propiedad no está definida. Según el perfil resuelto, muestra qué bean se activaría. Demuestra los tres caminos: con la propiedad definida, sin ella, y con valor "prod".
