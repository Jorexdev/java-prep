package spring.boot.perfiles;

/*
    PERFILES DE ENTORNO — Spring Profiles

    ► ¿Qué son los Profiles?
      Mecanismo de Spring para activar distintas configuraciones según el entorno
      de ejecución (dev, test, staging, prod).

      Permiten:
        - Cargar distintos application.properties por entorno.
        - Activar o desactivar beans según el entorno.
        - Separar configuración de infraestructura (BD, colas, etc.).

    ── FICHEROS DE CONFIGURACIÓN POR PERFIL ───────────────────────────────────

    ► Convención de nombres: application-{profile}.properties / .yml

      application.properties          → propiedades comunes a todos los entornos
      application-dev.properties      → solo activo en perfil "dev"
      application-prod.properties     → solo activo en perfil "prod"
      application-test.properties     → solo activo en tests

    ► Ejemplo

      # application.properties (común)
      spring.application.name=mi-servicio
      app.max-reintentos=3

      # application-dev.properties
      server.port=8080
      spring.datasource.url=jdbc:h2:mem:devdb
      spring.jpa.show-sql=true
      logging.level.root=DEBUG

      # application-prod.properties
      server.port=80
      spring.datasource.url=jdbc:postgresql://prod-db:5432/midb
      spring.jpa.show-sql=false
      logging.level.root=WARN

    ── ACTIVAR UN PERFIL ──────────────────────────────────────────────────────

    ► En application.properties (no recomendado para prod)

        spring.profiles.active=dev

    ► Variable de entorno (recomendado)

        SPRING_PROFILES_ACTIVE=prod

    ► Argumento de línea de comandos

        java -jar app.jar --spring.profiles.active=prod

    ► En tests con @ActiveProfiles

        @SpringBootTest
        @ActiveProfiles("test")
        class IntegrationTest { ... }

    ── @Profile EN BEANS ──────────────────────────────────────────────────────

    ► Activar un bean solo en ciertos perfiles

        @Service
        @Profile("dev")
        public class MockEmailService implements EmailService {
            public void enviar(String msg) {
                System.out.println("[DEV] Email simulado: " + msg);
            }
        }

        @Service
        @Profile("prod")
        public class SmtpEmailService implements EmailService {
            public void enviar(String msg) {
                // envío real por SMTP
            }
        }

    ► Negación de perfil

        @Component
        @Profile("!prod")  // activo en todos los perfiles EXCEPTO prod
        public class DevDataLoader implements CommandLineRunner {
            public void run(String... args) {
                // carga datos de prueba
            }
        }

    ► Múltiples perfiles en un bean

        @Component
        @Profile({"dev", "test"})
        public class MockServicio { ... }

    ── PERFILES CON @Configuration ────────────────────────────────────────────

        @Configuration
        @Profile("prod")
        public class ProdConfig {

            @Bean
            public DataSource dataSource() {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(System.getenv("DB_URL"));
                config.setMaximumPoolSize(20);
                return new HikariDataSource(config);
            }
        }

        @Configuration
        @Profile("dev")
        public class DevConfig {

            @Bean
            public DataSource dataSource() {
                return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
            }
        }

    ── YAML CON MÚLTIPLES PERFILES EN UN FICHERO ──────────────────────────────

      En application.yml se pueden definir varios perfiles con separador ---:

        spring:
          application:
            name: mi-servicio

        ---
        spring:
          config:
            activate:
              on-profile: dev
          datasource:
            url: jdbc:h2:mem:devdb

        ---
        spring:
          config:
            activate:
              on-profile: prod
          datasource:
            url: jdbc:postgresql://prod-db:5432/midb

    ── PERFIL DEFAULT ─────────────────────────────────────────────────────────

      Si no se activa ningún perfil, Spring usa el perfil "default".
      Se puede configurar con:

        spring.profiles.default=dev

    ► Preguntas típicas de entrevista
      - ¿Cómo separas la configuración de base de datos entre dev y prod?
      - ¿Qué pasa si no activas ningún perfil?
      - ¿Cómo puedes activar múltiples perfiles a la vez?
      - ¿Cómo cargarías datos de prueba solo en el entorno de desarrollo?
      - ¿Qué diferencia hay entre @Profile y @ConditionalOnProperty?
*/
public class Intro {}
