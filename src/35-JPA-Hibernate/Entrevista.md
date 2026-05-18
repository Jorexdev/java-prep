<div align="center">
  <a href="#"><img src="../../assets/modules/banner-35-jpa-hibernate-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Qué diferencia hay entre JPA y Hibernate?**
JPA es una especificación (un conjunto de interfaces y anotaciones del estándar Jakarta EE) que define cómo hacer ORM en Java. Hibernate es la implementación más popular de esa especificación — proporciona el código real que ejecuta las queries, gestiona el caché y genera el SQL. Usar JPA directamente en el código garantiza portabilidad: podrías cambiar Hibernate por EclipseLink sin tocar el código de negocio. En la práctica, casi todos los proyectos Spring Boot usan Hibernate como provider subyacente.

---

**Explica el ciclo de vida de una entidad JPA (estados)**
Una entidad pasa por cuatro estados: **Transient** — objeto creado con `new`, JPA no lo conoce y no está en BD. **Managed** — vinculado al `PersistenceContext` tras `persist()` o `find()`; cualquier cambio se detecta automáticamente (dirty checking) y se sincroniza con BD al hacer `flush()` o `commit()`. **Detached** — desconectado del contexto (tras `detach()`, `close()` o fin de transacción); los cambios no se propagan hasta hacer `merge()` para reincorporarlo. **Removed** — marcado para eliminar tras `remove()`; se borra en BD en el siguiente flush.

---

**¿Qué es el problema N+1 y cómo lo solucionas?**
Ocurre cuando cargas N entidades y luego accedes a una relación lazy de cada una: Hibernate ejecuta 1 query inicial + N queries adicionales (una por entidad padre). Ejemplo: 100 departamentos → 101 queries en total. Soluciones: (1) **FETCH JOIN** en JPQL: `SELECT DISTINCT d FROM Departamento d JOIN FETCH d.empleados` — una sola query con JOIN. (2) **`@BatchSize(size=25)`** en la colección: Hibernate agrupa los lazy loads en batches de 25 → 5 queries en lugar de 100. (3) **`@EntityGraph`** en Spring Data para especificar qué relaciones cargar en una query concreta. La causa raíz suele ser `FetchType.LAZY` sin join explícito — detectarlo con un log de queries SQL habilitado.

---

**FetchType.LAZY vs EAGER: ¿cuándo usar cada uno?**
`LAZY` carga la relación solo cuando se accede al campo — es el comportamiento por defecto en `@OneToMany` y `@ManyToMany` y el recomendado en general. Evita traer datos innecesarios de BD. Riesgo: `LazyInitializationException` si se accede fuera de la transacción. `EAGER` carga la relación junto con la entidad padre siempre, en cada `find()` o query. Solo tiene sentido si la relación es pequeña y siempre se necesita (ej. `@ManyToOne` de un usuario a su rol). Nunca uses `EAGER` en colecciones grandes — puede traer miles de registros con cada carga.

---

**¿Qué hace @Transactional y qué niveles de propagación existen?**
`@Transactional` es un aspecto que Spring AOP aplica al método: abre una transacción antes de ejecutarlo y hace commit al terminar (o rollback si hay excepción unchecked). Sin `@Transactional`, las operaciones JPA se ejecutan en transacciones autocommit individuales. Las propagaciones más importantes: `REQUIRED` (defecto) — usa la transacción existente o crea una nueva; `REQUIRES_NEW` — siempre crea una nueva, suspendiendo la actual (útil para logs de auditoría que deben persistirse aunque falle la transacción padre); `NOT_SUPPORTED` — ejecuta sin transacción; `MANDATORY` — lanza excepción si no hay transacción activa. También es importante `readOnly = true` para queries de solo lectura: desactiva el dirty checking y permite optimizaciones en el driver JDBC.

---

**¿Qué es la caché de primer nivel (L1) y cuándo se invalida?**
La caché L1 es el `PersistenceContext` — un mapa interno del `EntityManager` que almacena todas las entidades cargadas en la transacción actual. Si pides la misma entidad dos veces con `find()`, la segunda vez Hibernate devuelve el objeto en memoria sin ir a BD. Se invalida completamente al cerrar el `EntityManager` o al terminar la transacción. También se invalida para entidades concretas al llamar `detach()` o `evict()`. Es automática e indesactivable. La caché L2, en cambio, es opcional, vive entre transacciones y requiere configuración explícita.

---

**¿Cuándo usarías JPQL en lugar de Spring Data findBy...?**
`findByNombreAndPrecioGreaterThan(String nombre, BigDecimal precio)` de Spring Data es perfecto para queries simples y estáticas. JPQL (con `@Query`) se justifica cuando: (1) la query involucra JOINs entre entidades o subqueries; (2) necesitas una proyección parcial (`SELECT p.nombre, p.precio`) en lugar de la entidad completa; (3) la query tiene lógica condicional compleja que no se puede expresar como nombre de método; (4) necesitas `JOIN FETCH` para resolver N+1. Si la query es muy dinámica (filtros opcionales variables), Criteria API o QueryDSL son mejores que JPQL concatenado.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
