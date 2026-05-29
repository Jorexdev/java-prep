# Ejercicios — 24 Spring Boot: Starters
## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Spring factories simulation**

Simula `META-INF/spring.factories` usando un `Map<String, List<String>>`.
Clave: `"EnableAutoConfiguration"`, valor: lista de nombres de auto-config classes.
Carga la lista, instancia cada clase usando reflection y ejecuta su método `configure()`.
Demo con 3 auto-configs: `WebAutoConfig`, `DataSourceAutoConfig`, `SecurityAutoConfig`.

---

**Ejercicio 2 — Condition evaluation**

Implementa `ConditionContext(Map<String,String> props)`.
Define 3 `Condition` distintas:
- `PropertyPresentCondition(key)` → true si la clave existe en props
- `ClassPresentCondition(className)` → true si la clase está en el classpath
- `BeanAbsentCondition(beanType, container)` → true si NO hay bean de ese tipo

Crea `CompositeCondition(List<Condition>)` que evalúa todas con AND.
Demo combinando las 3 condiciones, mostrando el resultado de cada una y el AND final.

---

**Ejercicio 3 — @ConditionalOnWebApplication**

Según el valor de `"app.type"` en el config Map:
- `"web"` → registra beans web (simula `DispatcherServlet`, `HandlerMapping`)
- `"servlet"` → registra beans servlet (simula `ServletContext`, `FilterChain`)
- cualquier otro → registra beans reactivos (simula `WebFlux`, `ReactorNetty`)

Demo los 3 casos mostrando qué beans se registran en cada uno.

---

**Ejercicio 4 — Auto-config report**

Al terminar la auto-config, genera un reporte tipo Spring Boot Conditions Report.
Cada auto-config tiene un nombre y puede reportar: MATCH (condición cumplida)
o NO_MATCH con la razón (condición que falló).
Imprime una tabla con `[✓]` para las que se ejecutaron y `[✗]` para las omitidas
con la razón. Demo con 5 auto-configs donde 2 se omiten.

---

**Ejercicio 5 — Custom starter**

Implementa el starter `"observability"` que activa `MetricsCollector`,
`HealthIndicator` y `TracingFilter` si `"observability.enabled=true"`.
Cada bean se configura con las propiedades del starter (endpoint, samplingRate).
Demo: flujo completo con la propiedad en true y en false.

---

**Ejercicio 6 — Custom starter con auto-configuración condicional**
Implementa un starter propio con 3 módulos: `cache` (activa `CacheManager` si `cache.enabled=true`; activa `CacheStatistics` si además `cache.stats.enabled=true`; activa `SimpleCacheManager` como fallback si `cache.enabled` no está), `security` (activa `SecurityFilter` si `security.enabled=true`) y `metrics` (activa `MetricsRegistry` si `metrics.enabled=true`). Define condiciones reutilizables: `PropertyEqualsCondition`, `PropertyMissingCondition`. `CustomStarterAutoConfig` evalúa cada bean con su condición y genera un reporte tabular `[✓ MATCH] / [✗ NO_MATCH]`. Demo con 2 escenarios: todas las features ON (5 beans activos) y config mínima (solo metrics, fallback SimpleCacheManager activo).

---
