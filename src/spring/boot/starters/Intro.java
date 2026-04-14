package spring.boot.starters;

/*
    SPRING BOOT — Starters y autoconfiguración

    ► ¿Qué es Spring Boot?
      Framework que simplifica la creación de aplicaciones Spring eliminando
      la configuración manual (XML o Java Config extenso).

      Se basa en tres pilares:
        1. Starters → dependencias predefinidas agrupadas por funcionalidad.
        2. Autoconfiguración → detecta librerías en el classpath y configura beans automáticamente.
        3. Spring Initializr → generador de proyectos (start.spring.io).

    ── STARTERS ───────────────────────────────────────────────────────────────

    ► ¿Qué es un Starter?
      Un starter es una dependencia de Maven/Gradle que agrupa todas las
      librerías necesarias para una funcionalidad concreta.

      En lugar de añadir 10 dependencias por separado, añades un starter
      y Spring Boot gestiona las versiones compatibles.

    ► Starters más comunes

      spring-boot-starter              → núcleo (logging, autoconfig, Spring context)
      spring-boot-starter-web          → Spring MVC + Tomcat embebido + Jackson
      spring-boot-starter-data-jpa     → JPA + Hibernate + Spring Data
      spring-boot-starter-security     → Spring Security
      spring-boot-starter-test         → JUnit 5 + Mockito + AssertJ + Spring Test
      spring-boot-starter-actuator     → endpoints de salud, métricas, info
      spring-boot-starter-data-redis   → Redis + Lettuce/Jedis
      spring-boot-starter-amqp         → RabbitMQ
      spring-boot-starter-mail         → JavaMail
      spring-boot-starter-validation   → Bean Validation (Hibernate Validator)
      spring-boot-starter-aop          → Spring AOP + AspectJ

    ► Ejemplo en pom.xml

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- No hace falta especificar versión; se hereda del parent -->
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>3.3.0</version>
        </parent>

    ── AUTOCONFIGURACIÓN ──────────────────────────────────────────────────────

    ► ¿Cómo funciona?
      Spring Boot analiza el classpath al arrancar y aplica configuraciones
      condicionalmente. Cada clase de autoconfiguración usa anotaciones @Conditional.

      Ejemplo interno: si detecta spring-boot-starter-data-jpa en el classpath
      y hay un DataSource configurado, crea automáticamente:
        - EntityManagerFactory
        - TransactionManager
        - JpaRepositories

    ► Anotaciones @Conditional (cómo funciona por dentro)

      @ConditionalOnClass(DataSource.class)
        → solo se activa si la clase está en el classpath.

      @ConditionalOnMissingBean(DataSource.class)
        → solo si el usuario no definió ya un bean de ese tipo.

      @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
        → solo si la propiedad está activa.

    ► Ejemplo de autoconfiguración personalizada

        @Configuration
        @ConditionalOnClass(ObjectMapper.class)
        @ConditionalOnMissingBean(ObjectMapper.class)
        public class JacksonAutoConfiguration {

            @Bean
            public ObjectMapper objectMapper() {
                return new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }
        }

    ► Ver qué autoconfiguración está activa
      Al arrancar con debug=true en application.properties:

        debug=true

      O con el endpoint de Actuator:
        GET /actuator/conditions

    ► Deshabilitar una autoconfiguración específica

        @SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
        public class Application { ... }

      O en application.properties:
        spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

    ── @SpringBootApplication ─────────────────────────────────────────────────

      Esta anotación es un atajo de tres anotaciones:

        @SpringBootConfiguration   → equivale a @Configuration
        @EnableAutoConfiguration   → activa la autoconfiguración
        @ComponentScan             → escanea beans desde el paquete raíz

        @SpringBootApplication
        public class Application {
            public static void main(String[] args) {
                SpringApplication.run(Application.class, args);
            }
        }

    ── SERVIDOR EMBEBIDO ──────────────────────────────────────────────────────

      spring-boot-starter-web incluye Tomcat embebido por defecto.
      Se puede cambiar a Jetty o Undertow:

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>

    ► Preguntas típicas de entrevista
      - ¿Qué hace @EnableAutoConfiguration?
      - ¿Cómo sobreescribes un bean que Spring Boot autoconfigura?
      - ¿Qué es spring-boot-starter-parent y para qué sirve?
      - ¿Cómo ves qué beans se han autoconfigurando al arrancar?
      - ¿Puedes cambiar el servidor embebido? ¿Cómo?
*/
public class Intro {}
