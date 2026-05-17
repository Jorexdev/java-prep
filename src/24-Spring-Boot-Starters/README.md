<div align="center">
  <a href="#"><img src="../../assets/modules/banner-24-spring-starters-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Los **starters** son dependencias Maven/Gradle que agrupan todo lo necesario para una funcionalidad concreta. Al añadir un starter, Spring Boot detecta las clases en el classpath y aplica **autoconfiguración** por defecto.

```xml
<!-- Solo esto es necesario para una app web completa -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Spring Boot detecta que Tomcat está en el classpath → configura automáticamente el servidor embebido, Jackson para JSON, DispatcherServlet, etc.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Starters más comunes:**

| Starter | Qué incluye |
|---|---|
| `starter-web` | Tomcat, Spring MVC, Jackson |
| `starter-data-jpa` | Hibernate, Spring Data JPA, pool de conexiones |
| `starter-security` | Spring Security con autenticación básica por defecto |
| `starter-test` | JUnit 5, Mockito, AssertJ, Spring Test |
| `starter-actuator` | Endpoints de monitoreo y salud |
| `starter-validation` | Hibernate Validator (Bean Validation) |

**`@SpringBootApplication` = 3 anotaciones en una:**
```java
@SpringBootApplication
// equivale a:
@Configuration         // clase de configuración Spring
@EnableAutoConfiguration  // activa la autoconfiguración
@ComponentScan         // escanea beans en el paquete y subpaquetes
```

**Condicional de autoconfiguración:**
Spring Boot usa `@Conditional` para activar configuración solo cuando se cumplen condiciones:
- `@ConditionalOnClass(DataSource.class)` — si la clase está en el classpath
- `@ConditionalOnMissingBean(DataSource.class)` — si no hay un bean ya definido
- `@ConditionalOnProperty("app.feature.enabled")` — si la propiedad está activa

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Zero configuration para el 80% de casos comunes.
- Convención sobre configuración: funciona por defecto, personalizable cuando necesario.
- `@ConditionalOnMissingBean`: define tu propio bean para sobrescribir el por defecto.
- `spring-boot-actuator` para observabilidad con mínima configuración.

Este módulo es solo teoría — la autoconfiguración ocurre en el classpath al arrancar la aplicación.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
