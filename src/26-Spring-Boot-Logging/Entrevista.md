<div align="center">
  <a href="#"><img src="../../assets/modules/banner-26-spring-logging-v1.svg" width="100%" alt=""/></a>
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

**¿Qué es SLF4J y por qué es una fachada?**
SLF4J (Simple Logging Facade for Java) es una API de logging que no implementa logging por sí misma — delega a una implementación en el classpath (Logback, Log4j2, java.util.logging). El código programa contra SLF4J, lo que permite cambiar la implementación de logging sin cambiar el código fuente.

---

**¿Cuál es el nivel de log por defecto en Spring Boot?**
`INFO` para el root logger. Esto significa que se muestran INFO, WARN y ERROR, pero no DEBUG ni TRACE. El log de arranque de Spring Boot (listado de beans, port, etc.) es visible porque Spring lo emite a nivel INFO.

---

**¿Qué es MDC y para qué sirve?**
Mapped Diagnostic Context es un mapa de key-value asociado al hilo actual. Permite añadir contexto a todos los logs de una misma request (requestId, userId, correlationId) sin pasarlo explícitamente a cada método. Se configura una vez al inicio de la request (ej. en un filtro HTTP) y se limpia al final.

---

**¿Cómo configuras el nivel de log de un paquete específico?**
En application.yml: `logging.level.com.ejemplo.servicio: DEBUG`. Esto activa DEBUG para ese paquete sin afectar el nivel global. Útil para investigar problemas en producción sin habilitar DEBUG globalmente (que generaría demasiado ruido).

---

**¿Cuándo usarías logging estructurado (JSON)?**
En producción cuando los logs van a un sistema centralizado (ELK, Splunk, Datadog). El logging estructurado emite JSON en lugar de texto plano, lo que facilita la búsqueda, filtrado y creación de alertas. En desarrollo, el formato texto legible por humanos es más práctico.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
