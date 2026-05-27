<div align="center">
  <a href="#"><img src="../../assets/modules/banner-26-spring-logging-v1.svg" width="100%" alt=""/></a>
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

Spring Boot usa **SLF4J** (Simple Logging Facade for Java) como capa de abstracción y **Logback** como implementación por defecto. El código siempre programa contra SLF4J, lo que permite cambiar la implementación sin tocar el código.

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PedidoService {
    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    public void crearPedido(Pedido p) {
        log.info("Creando pedido: {}", p.getId());
        log.debug("Detalles del pedido: {}", p);  // solo visible en DEBUG
    }
}
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

**Niveles de log** (de menor a mayor severidad):

```
TRACE < DEBUG < INFO < WARN < ERROR
```

El nivel configurado filtra todos los mensajes por debajo: con `INFO`, no se muestran TRACE ni DEBUG.

**Configuración de niveles:**
```yaml
# application.yml
logging:
  level:
    root: INFO                           # nivel global
    com.ejemplo.servicio: DEBUG          # paquete específico
    org.springframework.web: WARN        # reduce ruido de Spring
```

**MDC (Mapped Diagnostic Context):**
```java
MDC.put("requestId", UUID.randomUUID().toString());
// Todos los logs del hilo incluirán requestId automáticamente
MDC.clear();  // limpiar al final de la request
```

**Logging estructurado (JSON) para producción:**
```yaml
logging:
  structured:
    format:
      console: ecs  # Elastic Common Schema (Spring Boot 3.4+)
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  SLF4J como fachada: cambias Logback por Log4j2 sin tocar el código.
- Niveles configurables por paquete: DEBUG solo donde lo necesitas, no globalmente.
- MDC para correlacionar logs de una misma request (requestId, userId...).
- Logging estructurado JSON para ingesta en ELK, Splunk, Datadog.

Ver [ExpLogging.java](ExpLogging.java), [ExpLogLevels.java](ExpLogLevels.java), [ExpMDC.java](ExpMDC.java) y [ExpStructuredLogging.java](ExpStructuredLogging.java) para ejemplos ejecutables con SLF4J, niveles de log, MDC y logging estructurado JSON.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
