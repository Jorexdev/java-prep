<div align="center">
  <a href="#"><img src="assets/roadmap-v1.svg" width="100%" alt="Roadmap"/></a>
</div>

<div align="center"><img height="12" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='12'/%3E"/></div>

Orden recomendado para sacarle el máximo partido al repo. Cada módulo tiene `README.md` (teoría), `Entrevista.md` (preguntas de entrevista) y archivos Java ejecutables donde aplica.

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/shared/fase-01-fundamentos-v1.svg" width="100%" alt="Fase 01 — Fundamentos del lenguaje"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Empieza aquí si preparas una entrevista técnica Java desde cero o quieres refrescar los conceptos base.

1. [01 · Lambdas](src/01-Lambdas/) — interfaces funcionales, sintaxis, referencias a métodos
2. [02 · Streams](src/02-Streams/) — pipeline lazy, operaciones intermedias/terminales, 30 ejercicios
3. [03 · Optional](src/03-Opcional/) — evitar NPE, map/flatMap, orElseGet
4. [04 · Genéricos](src/04-Genericos/) — wildcards, PECS, type erasure
5. [05 · Excepciones](src/05-Excepciones/) — checked/unchecked, try-with-resources, excepciones custom
6. [06 · Clases Abstractas](src/06-Clases-Abstractas/) — abstract vs interface, cuándo usar cada una

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/shared/fase-02-colecciones-v1.svg" width="100%" alt="Fase 02 — Colecciones"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Bloque muy frecuente en entrevistas. Estudia primero las implementaciones base y luego las variantes.

7. [07 · Lista](src/07-Colecciones-Lista/) — ArrayList vs LinkedList, complejidades
8. [08 · Mapa](src/08-Colecciones-Mapa/) — HashMap, TreeMap, LinkedHashMap, hashCode/equals
9. [09 · Conjunto](src/09-Colecciones-Conjunto/) — HashSet, TreeSet, EnumSet
10. [10 · Cola](src/10-Colecciones-Cola/) — Queue, PriorityQueue, BlockingQueue
11. [11 · Deque](src/11-Colecciones-Deque/) — ArrayDeque como stack y queue
12. [12 · Colecciones Concurrentes](src/12-Colecciones-Concurrentes/) — ConcurrentHashMap, CopyOnWriteArrayList
13. [13 · Utilidades](src/13-Colecciones-Utilidades/) — Collections, Comparable, Comparator, Iterator

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/shared/fase-03-concurrencia-v1.svg" width="100%" alt="Fase 03 — Concurrencia"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Una de las áreas con más profundidad en entrevistas senior. Sigue este orden estrictamente.

14. [14 · Concurrencia](src/14-Concurrencia/) — synchronized, volatile, ReentrantLock, race condition, deadlock
15. [15 · Async](src/15-Concurrencia-Asincrona/) — ExecutorService, CompletableFuture
16. [16 · Virtual Threads](src/16-Hilos-Virtuales/) — Java 21, I/O-bound vs CPU-bound, pinning
17. [17 · Recolector de Basura](src/17-Recolector-de-Basura/) — generaciones, G1GC, ZGC, JVM flags

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/shared/fase-04-principios-v1.svg" width="100%" alt="Fase 04 — Principios y Patrones"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Antes de entrar en Spring, asegúrate de tener estos sólidos. Son la base de las preguntas de diseño.

18. [18 · SOLID](src/18-SOLID/) — los 5 principios con ejemplos de violación y corrección
19. [19 · Patrones de Diseño](src/19-Patrones-de-Diseno/) — Singleton, Factory, Builder, Adapter, Decorator, Strategy, Observer
20. [20 · AOP](src/20-AOP/) — aspectos, pointcut, @Before, @Around, weaving con proxies

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/shared/fase-05-spring-v1.svg" width="100%" alt="Fase 05 — Spring"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Sigue el orden: primero Core (contenedor), luego Boot (configuración y autoconfiguración).

21. [21 · Spring Core: IoC](src/21-Spring-Core-IoC/) — inversión de control, DI por constructor, @Qualifier
22. [22 · Spring Core: Beans](src/22-Spring-Core-Beans/) — ciclo de vida, @PostConstruct, scopes
23. [23 · Spring Boot: Config](src/23-Spring-Boot-Config/) — application.yml, @ConfigurationProperties, precedencia
24. [24 · Spring Boot: Starters](src/24-Spring-Boot-Starters/) — autoconfiguración, @SpringBootApplication, @Conditional
25. [25 · Spring Boot: Perfiles](src/25-Spring-Boot-Perfiles/) — @Profile, application-{env}.yml
26. [26 · Spring Boot: Logging](src/26-Spring-Boot-Logging/) — SLF4J, Logback, MDC, logging estructurado

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/shared/fase-06-devops-v1.svg" width="100%" alt="Fase 06 — DevOps y Herramientas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Bloques de conocimiento operacional. Puedes estudiarlos en paralelo con las fases anteriores.

27. [27 · Docker](src/27-DevOps-Docker/) — imagen, contenedor, Dockerfile, docker-compose
28. [28 · Kubernetes](src/28-DevOps-Kubernetes/) — Pod, Deployment, Service, HPA, ConfigMap
29. [29 · IaC](src/29-DevOps-IaC/) — Terraform vs Ansible, state file, idempotencia
30. [30 · Pipelines](src/30-DevOps-Pipelines/) — GitHub Actions, Jenkins, GitLab CI

31. [31 · Git](src/31-Herramientas-Git/) — merge vs rebase, cherry-pick, stash
32. [32 · Maven](src/32-Herramientas-Maven/) — ciclo de vida, dependencias transitivas, perfiles
33. [33 · Gradle](src/33-Herramientas-Gradle/) — Wrapper, compilación incremental, Groovy vs Kotlin DSL

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/shared/fase-07-spring-avanzado-v1.svg" width="100%" alt="Fase 07 — Spring Avanzado"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Capa web, persistencia, seguridad y testing. Imprescindibles para cualquier entrevista backend con Spring.

34. [34 · Spring MVC](src/34-Spring-MVC/) — DispatcherServlet, @RestController, @ControllerAdvice, @Valid
35. [35 · JPA / Hibernate](src/35-JPA-Hibernate/) — entidades, relaciones, fetch types, @Transactional, N+1, caché L2
36. [36 · Testing](src/36-Testing/) — JUnit 5, Mockito, @SpringBootTest, TDD, test doubles
37. [37 · Spring Security](src/37-Spring-Security/) — SecurityFilterChain, JWT, BCrypt, @PreAuthorize, OAuth2

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/shared/fase-08-kafka-v1.svg" width="100%" alt="Fase 08 — Kafka y Arquitecturas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Los temas que diferencian un perfil senior. Estudia en este orden.

38. [38 · Kafka](src/38-Kafka/) — topics, particiones, producers, consumers, offsets, DLT, Saga coreografiada
39. [39 · Microservicios](src/39-Microservicios/) — Circuit Breaker, Saga, API Gateway, Outbox Pattern, Two-Phase Commit
40. [40 · Arquitecturas](src/40-Arquitecturas/) — Hexagonal, Clean Architecture, CQRS, Event Sourcing, DDD

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/shared/fase-09-java-moderno-v1.svg" width="100%" alt="Fase 09 — Java Moderno y Reactive"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2020/svg' width='1' height='16'/%3E"/></div>

Módulos de ampliación. Estudia tras completar las fases 01-08.

41. [41 · Java Moderno](src/41-Java-Moderno/) — Records, Sealed Classes, Pattern Matching, Switch Expressions, Text Blocks
42. [42 · Reactive](src/42-Reactive/) — Reactive Streams, Flow API, backpressure, operadores, manejo de errores

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

Módulos de ampliación avanzada. Cubren persistencia NoSQL y caché distribuida, frecuentes en entrevistas senior de backend.

43. [43 · NoSQL](src/43-NoSQL/) — MongoDB, Redis, Spring Data, Spring Cache
