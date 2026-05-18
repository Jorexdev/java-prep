<div align="center">
  <a href="#"><img src="../../assets/modules/banner-40-arquitecturas-v1.svg" width="100%" alt=""/></a>
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

La arquitectura de software define **cómo se organizan las capas, dependencias y responsabilidades** de un sistema. Es la decisión que más difícil resulta cambiar después — y por eso es la más costosa cuando se hace mal.

La distinción fundamental es entre **arquitectura técnica** (cómo se organizan las capas desde el punto de vista de la tecnología: controladores, servicios, repositorios) y **arquitectura de dominio** (cómo se estructuran los conceptos del negocio: aggregates, bounded contexts, domain events). Las arquitecturas modernas priorizan que el **dominio no dependa de nada externo** — ni de frameworks, ni de bases de datos, ni de HTTP.

El principio que une todas estas arquitecturas es el mismo: **las dependencias deben apuntar hacia el dominio**, nunca hacia afuera. El dominio es el núcleo estable; las infraestructuras (HTTP, BD, cache, colas) son detalles que pueden cambiar.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Arquitectura Hexagonal (Ports & Adapters)**

Propuesta por Alistair Cockburn. El dominio vive en el centro completamente aislado de cualquier framework o detalle de infraestructura. Se comunica con el exterior a través de **puertos** (interfaces) y **adaptadores** (implementaciones concretas).

```
           ┌─────────────────────────────────┐
  HTTP ────► [Adaptador HTTP]                │
           │      ↓                          │
           │  [Puerto Entrada: UseCase]       │
           │      ↓                          │
           │  [Dominio — Entidades, Reglas]   │
           │      ↓                          │
           │  [Puerto Salida: Repository]     │
           │      ↓                          │
           │  [Adaptador JPA / MongoDB]       │
           └─────────────────────────────────┘
```

El dominio (entidades, use cases) no importa nada de Spring, JPA, ni HTTP. Solo depende de interfaces. Los adaptadores implementan esas interfaces. Esto permite testear el dominio con JUnit puro, sin contexto Spring ni base de datos.

**Clean Architecture (Robert C. Martin)**

Cuatro círculos concéntricos con una regla estricta: **las dependencias solo pueden apuntar hacia adentro**.

```
[Frameworks & Drivers] → [Interface Adapters] → [Use Cases] → [Entities]
       ↑ web, DB, UI         ↑ controllers,          ↑ reglas       ↑ reglas
                               presenters,          de aplicación    de negocio
                               gateways                              puras
```

- `Entities`: objetos de negocio con sus reglas (sin frameworks)
- `Use Cases`: orquestan entidades para cumplir casos de uso (sin frameworks)
- `Interface Adapters`: convierten datos entre use cases y frameworks (controllers, presenters, repositories)
- `Frameworks & Drivers`: Spring, JPA, React, etc. — detalles que pueden cambiar

**CQRS (Command Query Responsibility Segregation)**

Separar el modelo de **escritura** (commands) del modelo de **lectura** (queries). No tienen por qué ser la misma representación de los datos.

```java
// Command: modifica estado, no devuelve datos de dominio
// @CommandHandler
// void handle(CrearPedidoCommand cmd) { ... guarda en BD relacional }

// Query: solo lee, optimizado para la presentación
// @QueryHandler
// PedidoResumen handle(ObtenerPedidoQuery query) {
//     return vistasDesnormalizadas.findById(query.pedidoId());
// }
```

Tiene sentido cuando las necesidades de lectura y escritura son muy distintas (ej. escritura normalizada en relacional, lectura desnormalizada en Elasticsearch), o cuando la escala de lecturas es muy superior a la de escrituras.

**Event Sourcing**

En lugar de guardar el **estado actual**, se guarda la **secuencia de eventos** que llevaron a ese estado. El estado se reconstruye reproduciendo los eventos desde el inicio.

```java
// Estado actual (forma tradicional):
// Cuenta { saldo=150 }

// Event Sourcing:
// [CuentaCreada{titular="Ana"}]
// [DepositoRealizado{cantidad=200}]
// [RetiroRealizado{cantidad=50}]
// → saldo=150 (reconstruido reproduciendo los eventos)
```

Ventajas: audit trail completo gratuito, replay para debugging, posibilidad de reconstruir cualquier estado pasado, projections para diferentes vistas del mismo estado. Complejidades: eventual consistency de las projections, necesidad de versionar eventos, mayor complejidad inicial.

**DDD (Domain-Driven Design)**

- **Bounded Context**: límite explícito dentro del cual un modelo de dominio tiene significado consistente. "Producto" en el contexto de Catálogo tiene atributos distintos a "Producto" en el contexto de Inventario.
- **Aggregate**: grupo de entidades y value objects con una raíz (Aggregate Root) que garantiza la consistencia del grupo. Solo se accede al aggregate a través de su raíz.
- **Value Object**: objeto definido por sus atributos, sin identidad propia. `Dinero(100, "EUR")` es igual a otro `Dinero(100, "EUR")`.
- **Domain Event**: algo que ocurrió en el dominio y es relevante para el negocio. `PedidoConfirmado`, `StockAgotado`. Inmutables, en pasado.
- **Repository**: abstracción de persistencia a nivel de aggregate. Trabaja con aggregates completos, no con filas.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Testabilidad** — El dominio sin frameworks se testea con JUnit puro: instancias de clases, sin contexto Spring, sin base de datos, sin mocks de frameworks. Un test unitario de un use case puede ejecutarse en microsegundos. Al aislar el dominio, los tests son deterministas y rápidos.

**Flexibilidad para cambiar infraestructura** — Si el dominio depende solo de interfaces (`PedidoRepository`), cambiar de JPA a MongoDB es cambiar el adaptador, no tocar el dominio. Migrar de REST a GraphQL es añadir un adaptador nuevo. El dominio no se mueve.

**Separación de conceptos** — Cada capa tiene una responsabilidad clara. Los cambios en la capa HTTP no afectan al dominio. Los cambios en la BD no afectan a los use cases. El código de negocio no está mezclado con código de infraestructura — es legible por sí solo.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
