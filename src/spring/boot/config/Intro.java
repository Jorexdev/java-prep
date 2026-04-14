package spring.boot.config;

/*
    CONFIGURACIÓN EXTERNALIZADA — application.properties / application.yml

    ► ¿Por qué externalizar la configuración?
      Siguiendo el principio 12-factor app, la configuración no debe estar
      hardcodeada en el código fuente. Se externaliza para:
        - Cambiar comportamiento entre entornos (dev, staging, prod).
        - Gestionar secretos de forma segura.
        - Escalar sin recompilar el binario.

    ── FORMATOS ───────────────────────────────────────────────────────────────

    ► application.properties (formato clave=valor)

        server.port=8080
        spring.application.name=mi-servicio
        spring.datasource.url=jdbc:postgresql://localhost:5432/midb
        spring.datasource.username=usuario
        spring.datasource.password=secret
        spring.jpa.hibernate.ddl-auto=validate

    ► application.yml (formato YAML, más legible para estructuras anidadas)

        server:
          port: 8080

        spring:
          application:
            name: mi-servicio
          datasource:
            url: jdbc:postgresql://localhost:5432/midb
            username: usuario
            password: secret
          jpa:
            hibernate:
              ddl-auto: validate

      Ambos son equivalentes. Se prefiere YAML para configuraciones complejas.

    ── INYECCIÓN DE PROPIEDADES ────────────────────────────────────────────────

    ► @Value (inyección simple)

        @Service
        public class EmailService {

            @Value("${mail.host}")
            private String host;

            @Value("${mail.port:25}")   // valor por defecto si no está definido
            private int port;

            @Value("${app.admins}")     // lista separada por comas
            private List<String> admins;
        }

    ► @ConfigurationProperties (inyección tipada — RECOMENDADA)

      Agrupa propiedades relacionadas en una clase de configuración,
      con validación y autocompletion en el IDE.

        # application.yml
        app:
          email:
            host: smtp.gmail.com
            port: 587
            timeout: 5000
            admins:
              - admin@empresa.com
              - ops@empresa.com

        @ConfigurationProperties(prefix = "app.email")
        @Component  // o activarlo desde @SpringBootApplication con @EnableConfigurationProperties
        public class EmailProperties {

            private String host;
            private int port;
            private int timeout;
            private List<String> admins = new ArrayList<>();

            // getters y setters (o usar record en Java 17+)
        }

        @Service
        public class EmailService {

            private final EmailProperties props;

            public EmailService(EmailProperties props) {
                this.props = props;
            }
        }

    ── ORDEN DE PRECEDENCIA ────────────────────────────────────────────────────

      Spring Boot aplica las propiedades en este orden (de menor a mayor prioridad):

        1. application.properties en el classpath (src/main/resources).
        2. application-{profile}.properties.
        3. application.properties fuera del JAR (mismo directorio).
        4. Variables de entorno del sistema operativo.
        5. Argumentos de línea de comandos (--server.port=9090).

      Los valores con mayor prioridad sobreescriben los de menor prioridad.

    ── CONFIGURACIÓN SEGURA ────────────────────────────────────────────────────

    ► Variables de entorno (recomendado en producción)

      Spring Boot mapea variables de entorno a propiedades automáticamente:
        DB_PASSWORD=secret  →  spring.datasource.password=secret

      Convención: puntos y guiones se convierten a guion bajo en mayúsculas.

    ► Jasypt / Secrets Manager
      Para cifrar propiedades sensibles en el fichero:

        spring.datasource.password=ENC(aBcDefGhIjKl...)

      En cloud: AWS Secrets Manager, Azure Key Vault, HashiCorp Vault.

    ── VALIDACIÓN CON @Validated ──────────────────────────────────────────────

        @ConfigurationProperties(prefix = "app")
        @Validated
        public class AppProperties {

            @NotBlank
            private String apiKey;

            @Min(1) @Max(100)
            private int maxConnections;
        }

      Si la validación falla, la aplicación no arranca.

    ── FICHEROS ADICIONALES ───────────────────────────────────────────────────

    ► spring.config.import (Spring Boot 2.4+)
      Permite importar ficheros adicionales de configuración:

        spring.config.import=optional:file:./config/extra.yml
        spring.config.import=vault://secret/mi-app

    ► Configuración programática

        @SpringBootApplication
        public class Application {
            public static void main(String[] args) {
                SpringApplication app = new SpringApplication(Application.class);
                app.setDefaultProperties(Map.of("server.port", "8080"));
                app.run(args);
            }
        }

    ► Preguntas típicas de entrevista
      - ¿Qué diferencia hay entre @Value y @ConfigurationProperties?
      - ¿Cómo sobreescribirías una propiedad sin recompilar el JAR?
      - ¿Qué precauciones tomarías con credenciales en application.properties?
      - ¿Cómo funciona la precedencia de propiedades en Spring Boot?
      - ¿Qué ventaja tiene YAML frente a .properties?
*/
public class Intro {}
