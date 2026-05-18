<div align="center">
  <a href="#"><img src="../../assets/modules/banner-35-jpa-hibernate-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**JPA** (Jakarta Persistence API) es una **especificación** — define las interfaces y anotaciones para mapear objetos Java a bases de datos relacionales. **Hibernate** es la **implementación** más usada de esa especificación. Relación análoga a JDBC (spec) vs driver MySQL (implementación).

El `EntityManager` es el objeto central de JPA: gestiona el ciclo de vida de las entidades dentro de un `PersistenceContext` (unidad de trabajo transaccional).

**Ciclo de vida de una entidad:**

```
new Producto()          → TRANSIENT   — objeto Java normal, JPA no lo conoce
em.persist(p)           → MANAGED     — JPA lo rastrea: cambios se sincronizan con BD al hacer flush
em.detach(p)            → DETACHED    — desconectado del contexto, cambios no se sincronizan
em.merge(p)             → MANAGED     — reincorpora un objeto detached al contexto activo
em.remove(p)            → REMOVED     — marcado para borrar en el siguiente flush/commit
commit / flush          → SQL ejecutado en BD
```

El `PersistenceContext` actúa como caché L1 de primer nivel: si pides la misma entidad dos veces dentro de la misma transacción, Hibernate devuelve la instancia en memoria sin ir a la BD.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Anotaciones de entidad:**

```java
@Entity                          // marca la clase como entidad JPA
@Table(name = "productos")       // nombre de tabla (opcional — por defecto el de la clase)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // autoincrement en BD
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(precision = 10, scale = 2)
    private BigDecimal precio;
}
```

**Relaciones:**

```java
// Un departamento tiene muchos empleados
@Entity
public class Departamento {

    @OneToMany(mappedBy = "departamento", fetch = FetchType.LAZY)
    //          ↑ campo en Empleado que apunta de vuelta   ↑ carga bajo demanda (recomendado)
    private List<Empleado> empleados;
}

@Entity
public class Empleado {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")   // FK en la tabla empleados
    private Departamento departamento;
}
```

**FetchType.LAZY vs EAGER:**

| Tipo | Comportamiento | Cuándo usar |
|---|---|---|
| `LAZY` | Carga la relación solo cuando se accede | Por defecto en `@OneToMany`/`@ManyToMany` — evita carga innecesaria |
| `EAGER` | Carga la relación junto con la entidad padre | Solo si siempre necesitas los datos — peligroso en colecciones grandes |

**Problema N+1 y cómo evitarlo:**

```java
// MAL — N+1: 1 query para departamentos + N queries (una por departamento) para empleados
List<Departamento> deps = em.createQuery("SELECT d FROM Departamento d", Departamento.class)
    .getResultList();
for (Departamento d : deps) {
    d.getEmpleados().size();  // ← lazy load → 1 query extra por cada departamento
}

// BIEN — FETCH JOIN: 1 sola query con JOIN
List<Departamento> deps = em.createQuery(
    "SELECT DISTINCT d FROM Departamento d JOIN FETCH d.empleados", Departamento.class)
    .getResultList();

// Alternativa en Spring Data: @BatchSize(size = 25) en la colección → agrupa los lazy loads
```

**JPQL vs Criteria API:**

```java
// JPQL — orientado a entidades, legible, tipado en tiempo de ejecución
String jpql = "SELECT p FROM Producto p WHERE p.precio > :minPrecio ORDER BY p.nombre";
List<Producto> productos = em.createQuery(jpql, Producto.class)
    .setParameter("minPrecio", BigDecimal.valueOf(50))
    .getResultList();

// Criteria API — tipado en compilación, útil para queries dinámicas
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Producto> cq = cb.createQuery(Producto.class);
Root<Producto> root = cq.from(Producto.class);
cq.select(root).where(cb.greaterThan(root.get("precio"), BigDecimal.valueOf(50)));
```

**Cachés:**

| Nivel | Alcance | Siempre activa |
|---|---|---|
| L1 (PersistenceContext) | Una transacción / EntityManager | Sí — no configurable |
| L2 (shared cache) | Toda la aplicación, entre transacciones | No — requiere configuración explícita (Ehcache, Infinispan…) |

**@Transactional y propagación:**

```java
@Transactional                                    // abre transacción al entrar, commit al salir
public void transferir(Long origen, Long destino, BigDecimal importe) { ... }

// Propagaciones más usadas:
@Transactional(propagation = Propagation.REQUIRED)     // usa transacción existente o crea una (default)
@Transactional(propagation = Propagation.REQUIRES_NEW) // siempre nueva transacción — suspende la actual
@Transactional(propagation = Propagation.NOT_SUPPORTED)// ejecuta sin transacción
@Transactional(readOnly = true)                        // optimización para queries de solo lectura
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

- **Abstracción de SQL**: las entidades se trabajan como objetos Java — Hibernate genera el SQL adecuado para cada base de datos.
- **Portabilidad entre BD**: cambiar de PostgreSQL a MySQL solo requiere cambiar el dialecto — el código Java no cambia.
- **Gestión automática del ciclo de vida**: Hibernate detecta cambios en entidades managed (`dirty checking`) y ejecuta los UPDATE necesarios sin código manual.
- **Spring Data JPA** elimina el boilerplate de repositorios: `findByNombreAndPrecioGreaterThan(String nombre, BigDecimal precio)` genera la query automáticamente.
- **Caché L1 incluida**: dentro de una transacción, Hibernate no repite queries para la misma entidad — reduce roundtrips a BD.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
