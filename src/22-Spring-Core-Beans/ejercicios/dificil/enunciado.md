# Ejercicios — 22 Spring Core: Beans

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — @Bean con reflection**
Crea una anotación `@Bean` (custom). Implementa una clase `ConfigClass` con 3 métodos anotados con `@Bean` que devuelven distintos objetos. Escribe `ConfigurationProcessor` que, dado un objeto de la clase de configuración, usa reflection para encontrar todos los métodos con `@Bean`, los invoca y registra el resultado en un contenedor por nombre de método.

**Ejercicio 2 — Scoped proxy**
Implementa `ScopedProxy<T>` que envuelve un `Supplier<T>`. Cada acceso al proxy llama al supplier. Compara: (a) bean singleton directo — siempre el mismo objeto, (b) bean prototype a través de ScopedProxy — objeto nuevo en cada acceso. Demuestra el caso de uso real: un bean singleton que necesita un bean prototype en cada operación.

**Ejercicio 3 — Ciclo de vida completo con timestamps**
Implementa el ciclo de vida completo de un bean en este orden: constructor → `setBeanName(String)` → `setApplicationContext(ctx)` → `@PostConstruct` (init) → uso → `@PreDestroy` (destroy). Usa `System.nanoTime()` para medir el tiempo entre cada fase. Muestra una tabla con las fases y sus timestamps relativos en microsegundos.

**Ejercicio 4 — AwareInterfaces**
Define las interfaces `BeanNameAware` con `setBeanName(String name)` y `ApplicationContextAware` con `setApplicationContext(Object ctx)`. El contenedor, tras crear un bean, detecta si implementa estas interfaces y llama a los setters correspondientes. Implementa un bean que usa su nombre para loguear y el contexto para buscar otro bean. Demuestra el flujo completo.

---

**Ejercicio 5 — Event system con @EventListener simulado**
Implementa `ApplicationEvent` (base con source y timestamp), `TypedListener<T extends ApplicationEvent>` que solo procesa eventos del tipo `T` (verifica con `Class.isInstance`), y `ApplicationEventPublisher` con `register` y `publish`. Crea 4 eventos: `ContextRefreshedEvent`, `UserCreatedEvent`, `OrderPlacedEvent`, `SystemShutdownEvent`. Registra 6 listeners con servicios distintos: `EmailNotificationService` (escucha UserCreated y Shutdown), `AuditService` (escucha ApplicationEvent base = todos), `BillingService` (escucha OrderPlaced), `MetricsService` (escucha UserCreated y OrderPlaced). Publica 6 eventos y verifica que cada listener recibe solo los de su tipo. Imprime estadísticas de cuántos eventos procesó cada listener.

---
