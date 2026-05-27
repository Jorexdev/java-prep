<div align="center">
  <a href="#"><img src="../../assets/modules/banner-25-spring-perfiles-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Cómo activas un perfil en Spring Boot?**
Tres formas por orden de precedencia: (1) variable de entorno `SPRING_PROFILES_ACTIVE=prod`, (2) argumento de línea de comandos `--spring.profiles.active=prod`, (3) propiedad en application.properties `spring.profiles.active=dev`. La variable de entorno tiene mayor precedencia y es la forma estándar en producción/containers.

---

**¿Puede un bean estar activo en múltiples perfiles?**
Sí. `@Profile({"qa", "staging"})` activa el bean cuando cualquiera de esos perfiles está activo (OR lógico). Con `@Profile("!prod")` el bean está activo en cualquier perfil excepto prod.

---

**¿Cómo usas perfiles en tests?**
Con `@ActiveProfiles("test")` en la clase de test (junto con `@SpringBootTest`). Spring cargará `application-test.yml` y activará los beans con `@Profile("test")`. Es el patrón estándar para usar una base de datos H2 en tests sin afectar la configuración de dev/prod.

---

**¿Qué diferencia hay entre `spring.profiles.active` y `spring.profiles.default`?**
`active` especifica qué perfiles activar. `default` especifica el perfil a usar cuando no hay ninguno activo — si se activa cualquier perfil, el default se ignora. `active` tiene precedencia sobre `default`.

---

**¿Puedes combinar varios perfiles simultáneamente?**
Sí. `spring.profiles.active=base,monitoring,prod` activa los tres. Las propiedades de cada perfil se superponen: el último en la lista tiene mayor precedencia sobre los anteriores. Es útil para perfiles composicionales (base + monitoreo + entorno específico).

---

**¿Qué son los profile groups introducidos en Spring Boot 2.4?**
Los profile groups permiten activar varios perfiles con un solo nombre lógico. Se definen en `application.properties` con `spring.profiles.group.production=cloud,monitoring,security`. Cuando activas `production`, Spring Boot activa automáticamente los tres perfiles incluidos. Simplifica la gestión en entornos donde cada entorno requiere un conjunto fijo de perfiles sin que el operador tenga que conocer todos los nombres internos.

---

**¿Cómo accedes programáticamente al perfil activo y a las propiedades de entorno?**
Inyectando `Environment` de Spring: `environment.getActiveProfiles()` devuelve el array de perfiles activos y `environment.getProperty("clave")` resuelve la propiedad con la precedencia correcta. También puedes inyectar `@Value("${spring.profiles.active:default}")` para obtenerlo como String. En código de test, `@ActiveProfiles("dev")` establece el perfil activo sin tocar variables de entorno.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
