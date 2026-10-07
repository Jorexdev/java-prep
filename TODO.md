# 📋 TODO - java-prep

## 1. Fundamentos de Java
- [x] Colecciones (List, Set, Map, Queue, Deque, Concurrent)
- [x] Añadir ejemplos y diferencias entre Comparable y Comparator
- [x] Añadir ejemplos y diferencias entre Iterable e Iterator
- [x] Genéricos
- [X] Clases abstractas e interfaces
- [x] Lambdas y Streams
- [x] Manejo de excepciones (checked/unchecked, buenas prácticas)
- [x] Concurrencia (`synchronized`, `volatile`, `locks`)
- [x] ConcurrentHashMap + CopyOnWriteArrayList
- [x] CompletableFuture, ExecutorService
- [x] Virtual Threads
- [x] Garbage Collector (visión general, tuning básico)
- [x] Revisar base completa

## 2. Principios y Patrones de Software
- [x] Principios SOLID
- [x] Patrones: Singleton, Factory, Builder, Strategy, Observer, Adapter, Decorator

## 3. Herramientas de build y control de versiones
- [x] Maven (dependencias, profiles, plugins) → 32-Herramientas-Maven
- [x] Gradle → 33-Herramientas-Gradle
- [x] Git (branching, conflictos, GitFlow, PRs) → 31-Herramientas-Git

## 4. CI/CD y DevOps
- [x] Pipelines: Jenkins, GitHub Actions, GitLab CI → 30-DevOps-Pipelines
- [x] Docker (imágenes, redes, volúmenes) → 27-DevOps-Docker
- [x] Kubernetes (pods, deployments, services, config maps, secrets) → 28-DevOps-Kubernetes
- [x] Terraform / Ansible (infra as code – básico) → 29-DevOps-IaC

## 5. Spring Core & Boot
- [x] IoC y DI → 21-Spring-Core-IoC
- [x] Beans (ciclo de vida, scopes) → 22-Spring-Core-Beans
- [x] Starters y autoconfiguración → 24-Spring-Boot-Starters
- [x] Configuración externalizada (properties / YAML) → 23-Spring-Boot-Config
- [x] Perfiles de entorno → 25-Spring-Boot-Perfiles
- [x] Logging con SLF4J / Logback → 26-Spring-Boot-Logging

## 6. Desarrollo Web
- [x] Spring MVC (controllers, mappings, ResponseEntity)
- [x] Validación con Bean Validation
- [x] Manejo de errores global (@ControllerAdvice, @ExceptionHandler)

## 7. Persistencia
- [x] JPA / Hibernate (entidades, relaciones, fetch types)
- [x] CrudRepository, JpaRepository, queries con @Query
- [x] Transacciones con @Transactional
- [x] Migraciones (Flyway / Liquibase) → 35-JPA-Hibernate
- [x] N+1 problem, batch inserts, caching
- [x] MongoDB / Redis → 43-NoSQL
- [x] Spring Cache con Redis → 43-NoSQL

## 8. Comunicación entre Servicios
- [x] RestTemplate / WebClient → 39-Microservicios
- [x] Resilience4j (retry, circuit breaker, bulkhead)
- [x] Kafka (producers, consumers, topics)
- [x] Arquitectura basada en eventos
- [x] Spring Cloud Gateway
- [x] Eureka / Consul (service registry)
- [x] Spring Cloud Config (config server) → 39-Microservicios

## 9. Seguridad
- [x] Spring Security (SecurityFilterChain, filtros)
- [x] Autenticación y autorización
- [x] JWT
- [x] @PreAuthorize / Method Security
- [x] OAuth2 / OIDC
- [x] Keycloak / Auth0 → 37-Spring-Security

## 10. Observabilidad
- [x] Micrometer (métricas) → 26-Spring-Boot-Logging
- [x] Logging estructurado
- [x] Prometheus + Grafana → 26-Spring-Boot-Logging / 39-Microservicios
- [x] Tracing distribuido (OpenTelemetry / Zipkin / Jaeger) → 26-Spring-Boot-Logging, 39-Microservicios

## 11. Asincronía y Scheduling
- [x] @Async → 22-Spring-Core-Beans
- [x] @Scheduled → 22-Spring-Core-Beans
- [x] RabbitMQ / Kafka → 39-Microservicios

## 12. Testing
- [x] Unit testing (JUnit5 + Mockito)
- [x] Integration testing (Spring Boot Test)
- [x] Testcontainers (DBs, Kafka, RabbitMQ)
- [x] Contract Testing (Pact) → 36-Testing
- [x] API Testing (Postman, REST Assured) → 36-Testing
- [x] Swagger / OpenAPI → 34-Spring-MVC

## 13. Arquitecturas de Software
- [x] Arquitectura en capas
- [x] Clean Architecture
- [x] DDD (conceptos básicos)
- [x] Arquitectura hexagonal

## 14. Buenas prácticas
- [x] Organización de paquetes y nomenclatura → 40-Arquitecturas
- [x] Manejo de logs y trazabilidad → 26-Spring-Boot-Logging
- [x] Clean Code → 40-Arquitecturas
- [x] 12-factor app → 40-Arquitecturas
- [x] Performance tuning → 40-Arquitecturas
