<div align="center">
  <a href="#"><img src="../../assets/modules/banner-23-spring-config-v1.svg" width="100%" alt=""/></a>
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

Spring Boot externaliza la configuración en ficheros de propiedades, variables de entorno o argumentos de línea de comandos. El objetivo: el mismo artefacto (JAR) funciona en cualquier entorno cambiando solo la configuración.

```yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/midb
    username: ${DB_USER}
    password: ${DB_PASS}
app:
  config:
    timeout: 30
    retry-count: 3
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Fuentes de configuración por orden de precedencia** (mayor a menor):

1. Argumentos de línea de comandos (`--server.port=9090`)
2. Variables de entorno (`SPRING_DATASOURCE_URL`)
3. `application-{profile}.properties`
4. `application.properties` / `application.yml`
5. Valores por defecto en `@Value`

**`@Value` vs `@ConfigurationProperties`:**

```java
// @Value — propiedad individual
@Value("${app.config.timeout}")
private int timeout;

// @ConfigurationProperties — grupo de propiedades (recomendado)
@ConfigurationProperties(prefix = "app.config")
@Component
public class AppConfig {
    private int timeout;
    private int retryCount;  // app.config.retry-count → camelCase automático
    // getters y setters...
}
```

`@ConfigurationProperties` es preferible para grupos de propiedades: validación con `@Validated`, autocompletado en IDEs y mapeo tipo-seguro.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Configuración sin recompilar: cambia el entorno cambiando propiedades.
- `@ConfigurationProperties` con validación (`@NotNull`, `@Min`) para configuración robusta.
- Variables de entorno y CLI args tienen mayor precedencia — útil para override en producción.
- Spring Cloud Config y Vault para configuración centralizada y secreta en microservicios.

Ver [ExpConfig.java](ExpConfig.java), [ExpPropertiesBinding.java](ExpPropertiesBinding.java), [ExpEnvironment.java](ExpEnvironment.java) y [ExpValidation.java](ExpValidation.java) para ejemplos ejecutables con `@ConfigurationProperties`, `@Value`, validación y fuentes de configuración por precedencia.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
