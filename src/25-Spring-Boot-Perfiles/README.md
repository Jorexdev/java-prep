<div align="center">
  <a href="#"><img src="../../assets/modules/banner-25-spring-perfiles-v1.svg" width="100%" alt=""/></a>
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

Los **perfiles** de Spring Boot permiten tener configuraciones distintas para distintos entornos (dev, test, prod) activadas dinámicamente, sin cambiar el código ni recompilar.

```
src/main/resources/
├── application.yml           ← configuración base (todos los entornos)
├── application-dev.yml       ← sobrescribe para dev
├── application-test.yml      ← sobrescribe para test
└── application-prod.yml      ← sobrescribe para producción
```

La configuración específica del perfil activo **sobrescribe** la base.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Activar un perfil:**

```bash
# Variable de entorno
SPRING_PROFILES_ACTIVE=prod java -jar app.jar

# Argumento de línea de comandos
java -jar app.jar --spring.profiles.active=prod

# application.properties
spring.profiles.active=dev
```

**`@Profile` en beans:**
```java
@Bean
@Profile("dev")
public DataSource h2DataSource() { ... }  // solo activo en dev

@Bean
@Profile("!dev")   // activo en todo excepto dev
public DataSource postgresDataSource() { ... }

@Bean
@Profile({"qa", "staging"})  // activo en qa O staging
public DataSource qaDataSource() { ... }
```

**En tests:**
```java
@SpringBootTest
@ActiveProfiles("test")
class MiServicioTest { ... }
```

**Varios perfiles simultáneos:**
`spring.profiles.active=base,cloud,prod` — los perfiles se acumulan y el último tiene mayor precedencia.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Un único artefacto para todos los entornos.
- Configuración de entorno sin modificar el código.
- `@ActiveProfiles` en tests para configuración específica de testing.
- Soporte para perfiles anidados y acumulativos.

Este módulo es solo teoría — los perfiles se activan en tiempo de despliegue.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
