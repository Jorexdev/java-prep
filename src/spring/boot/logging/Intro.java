package spring.boot.logging;

public class Intro {
/*
    LOGGING — SLF4J y Logback en Spring Boot

    ► ¿Por qué importa el logging?
      El logging es la principal herramienta de observabilidad en producción.
      Permite depurar errores, auditar operaciones y rastrear flujos de negocio
      sin necesidad de un debugger.

    ── SLF4J ──────────────────────────────────────────────────────────────────

    ► ¿Qué es SLF4J?
      Simple Logging Facade for Java. Es una fachada (API) que abstrae
      el sistema de logging concreto. Tu código escribe contra SLF4J;
      en runtime se usa la implementación configurada (Logback, Log4j2, JUL...).

      Ventaja: puedes cambiar la implementación sin tocar el código.

    ► Uso básico

        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

        @Service
        public class PedidoService {

            private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

            public void crearPedido(Pedido pedido) {
                log.debug("Iniciando creación de pedido: {}", pedido.getId());
                try {
                    procesarPedido(pedido);
                    log.info("Pedido {} creado correctamente", pedido.getId());
                } catch (Exception e) {
                    log.error("Error al crear pedido {}: {}", pedido.getId(), e.getMessage(), e);
                    throw e;
                }
            }
        }

    ► Con Lombok (evita el boilerplate)

        @Slf4j
        @Service
        public class PedidoService {

            public void crearPedido(Pedido pedido) {
                log.info("Pedido {} creado", pedido.getId());  // 'log' inyectado por Lombok
            }
        }

    ── NIVELES DE LOG ─────────────────────────────────────────────────────────

      TRACE   → nivel más detallado, para depuración muy fina (raramente en prod).
      DEBUG   → información de depuración (flujos internos, valores de variables).
      INFO    → eventos relevantes del negocio (inicio de operaciones, resultados).
      WARN    → situaciones anómalas que no detienen la aplicación.
      ERROR   → errores que deben investigarse (excepciones, fallos de integración).

      En producción: nivel INFO o WARN.
      En desarrollo: nivel DEBUG.

    ── LOGBACK ────────────────────────────────────────────────────────────────

    ► ¿Qué es Logback?
      Implementación de SLF4J usada por defecto en Spring Boot.
      Se incluye automáticamente con spring-boot-starter.

    ► Configuración mínima en application.properties

        # Nivel global
        logging.level.root=INFO

        # Nivel por paquete
        logging.level.com.miapp.service=DEBUG
        logging.level.org.hibernate.SQL=DEBUG        # ver queries SQL
        logging.level.org.springframework.web=DEBUG  # ver peticiones HTTP

        # Fichero de salida
        logging.file.name=logs/aplicacion.log

        # Patrón de formato
        logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

    ► Configuración avanzada: logback-spring.xml

        <!-- src/main/resources/logback-spring.xml -->
        <configuration>

            <springProfile name="dev">
                <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
                    <encoder>
                        <pattern>%d{HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
                    </encoder>
                </appender>
                <root level="DEBUG">
                    <appender-ref ref="CONSOLE"/>
                </root>
            </springProfile>

            <springProfile name="prod">
                <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
                    <file>logs/app.log</file>
                    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                        <fileNamePattern>logs/app-%d{yyyy-MM-dd}.log.gz</fileNamePattern>
                        <maxHistory>30</maxHistory>
                        <totalSizeCap>1GB</totalSizeCap>
                    </rollingPolicy>
                    <encoder>
                        <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger - %msg%n</pattern>
                    </encoder>
                </appender>
                <root level="INFO">
                    <appender-ref ref="FILE"/>
                </root>
            </springProfile>

        </configuration>

      Ventaja de logback-spring.xml frente a logback.xml:
        Permite usar <springProfile> para configurar por entorno.

    ── LOGGING ESTRUCTURADO (JSON) ────────────────────────────────────────────

    ► ¿Por qué JSON en producción?
      Los sistemas de centralización de logs (ELK, Loki, Datadog, Splunk)
      consumen logs en JSON para poder indexarlos y filtrarlos fácilmente.

    ► Con Logstash Encoder (logstash-logback-encoder)

        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>7.4</version>
        </dependency>

        <!-- logback-spring.xml -->
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
        </appender>

      Produce:
        {"@timestamp":"2024-01-15T10:30:00","level":"INFO","logger":"PedidoService","message":"Pedido 123 creado"}

    ► Spring Boot 3.4+ — Structured Logging nativo

        logging.structured.format.console=ecs  # Elastic Common Schema
        logging.structured.format.file=logstash

    ── MDC (Mapped Diagnostic Context) ───────────────────────────────────────

    ► ¿Qué es MDC?
      Mapa de clave-valor que se añade automáticamente a todos los logs
      emitidos por el thread actual. Útil para añadir requestId o userId.

        @Component
        public class LoggingFilter implements Filter {

            public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
                    throws IOException, ServletException {
                String requestId = UUID.randomUUID().toString();
                MDC.put("requestId", requestId);
                try {
                    chain.doFilter(req, res);
                } finally {
                    MDC.clear();  // importante: limpiar al final del request
                }
            }
        }

        # En el patrón de Logback:
        %d{HH:mm:ss} [%X{requestId}] %-5level %msg%n

    ── BUENAS PRÁCTICAS ───────────────────────────────────────────────────────

      ✓ Usa {} para parametrización (no concatenación de String): log.info("id: {}", id)
      ✓ No loguees datos sensibles (passwords, tokens, tarjetas).
      ✓ Usa el nivel adecuado: negocio=INFO, depuración=DEBUG, fallos=ERROR.
      ✓ Incluye contexto suficiente para diagnosticar sin acceso al servidor.
      ✓ En producción: usar rolling file con límite de retención.
      ✓ Centraliza logs con ELK, Loki o Datadog.

    ► Preguntas típicas de entrevista
      - ¿Qué diferencia hay entre SLF4J y Logback?
      - ¿Por qué usar {} en lugar de concatenar en los mensajes de log?
      - ¿Qué es MDC y para qué se usa?
      - ¿Cómo cambiarías el nivel de log en producción sin reiniciar? (Actuator /loggers)
      - ¿Qué ventaja tiene el logging estructurado en JSON?
*/
}
