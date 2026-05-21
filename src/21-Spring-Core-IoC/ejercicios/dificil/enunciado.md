# Ejercicios — 21 Spring Core: IoC

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — IoC Container con reflection**
Implementa `IoCContainer` que registra clases (no instancias). Al llamar `getBean(Class<?>)`, el contenedor instancia la clase por reflection usando el constructor con más parámetros. Resuelve recursivamente todas las dependencias del constructor. Demuestra con tres clases encadenadas: `Config` (sin deps), `Cache` (necesita `Config`), `AppService` (necesita `Cache` y `Config`). Verifica que las instancias de `Config` son el mismo objeto (singleton).

**Ejercicio 2 — @PostConstruct / @PreDestroy simulado**
Define la interfaz `BeanLifecycle` con `init()` y `destroy()`. Implementa `LifecycleContainer` que mantiene una lista de beans creados en orden. Al registrar un bean que implementa `BeanLifecycle`, llama automáticamente a `init()`. Al llamar `close()` en el contenedor, invoca `destroy()` en orden inverso al de creación. Demuestra con tres beans que muestran mensajes detallados en cada fase.

**Ejercicio 3 — @Autowired con ambigüedad**
Define `NotificadorService` con dos implementaciones: `EmailNotificador` y `SmsNotificador`. Crea `AmbiguityContainer` que puede tener múltiples beans del mismo tipo. Sin qualifier, `getBean(Class<?>)` lanza `AmbiguousBeansException` con los nombres de ambas implementaciones. Con `getBean(Class<?>, String qualifier)` resuelve correctamente. En `main`, demuestra ambos casos y luego usa ambos notificadores correctamente.

**Ejercicio 4 — Event system**
Implementa: clase base `ApplicationEvent` con timestamp y source, interfaz `ApplicationListener<T extends ApplicationEvent>` con `onEvent(T event)`, clase `ApplicationEventPublisher` con `register(ApplicationListener<T>)` y `publish(T event)`. Crea `ContextRefreshedEvent` y un `LogEvent` custom. Implementa dos listeners. En `main`, registra los listeners, publica ambos eventos y muestra que cada listener recibe solo los eventos del tipo correcto usando `instanceof`.
