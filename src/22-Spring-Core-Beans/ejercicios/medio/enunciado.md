# Ejercicios — 22 Spring Core: Beans

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — BeanDefinition**
Crea la clase `BeanDefinition(Class<?> type, String scope, boolean lazy)`. Implementa `BeanContainer` que almacena definiciones y las usa para crear beans: scope `"singleton"` devuelve siempre la misma instancia, scope `"prototype"` crea una nueva con `type.getDeclaredConstructor().newInstance()`. Demuestra con una clase `Servicio` registrada como singleton y otra como prototype.

**Ejercicio 2 — Request scope con ThreadLocal**
Simula el scope `"request"` de Spring usando `ThreadLocal<Map<String, Object>>`. Crea `RequestScopeContext` con `beginRequest()`, `endRequest()` y `getBean(String name, Supplier<Object> factory)`. Cada thread tiene su propio scope. Lanza 3 threads simultáneos, cada uno con su request, y demuestra que los beans son independientes.

**Ejercicio 3 — BeanPostProcessor**
Define la interfaz `BeanPostProcessor` con `postProcessBefore(Object bean, String name)` y `postProcessAfter(Object bean, String name)`. Implementa `LoggingPostProcessor` que imprime cuándo se crea cada bean. Registra el processor en el contenedor y muestra cómo envuelve la creación de 3 beans distintos.

**Ejercicio 4 — DisposableBean con orden inverso**
Implementa `DisposableBean` con método `destroy()`. El `ApplicationContext` registra los beans en el orden en que se crean. Al llamar `context.close()`, invoca `destroy()` en todos los beans que implementan `DisposableBean` en orden inverso al de creación. Demuestra con 4 beans.

**Ejercicio 5 — @Value con defaults**
Implementa `PropertiesInjector` con un `Map<String, String>` como fuente de propiedades. El método `inject(String key, String defaultValue)` devuelve el valor del mapa si existe, o `defaultValue` si no. Además, resuelve referencias anidadas: `"${app.name}"` se busca en el mapa. Demuestra con 5 propiedades, algunas presentes y otras usando el default.
