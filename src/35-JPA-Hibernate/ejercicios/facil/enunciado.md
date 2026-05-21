# Ejercicios — 35 JPA / Hibernate

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Estados de entidad**

Crea una clase `Usuario` con campos `id` y `nombre`. Implementa un `EntityManager` simulado con un `HashMap` interno. Demuestra los cuatro estados del ciclo de vida JPA:

- **TRANSIENT**: objeto recién creado, sin id asignado, no gestionado.
- **MANAGED**: después de llamar a `persist`, el objeto entra en el contexto de persistencia.
- **DETACHED**: después de llamar a `detach`, el objeto existe pero ya no está gestionado.
- **REMOVED**: después de llamar a `remove`, está marcado para eliminación.

El `main` debe imprimir el estado en cada paso.

---

**Ejercicio 2 — CRUD básico**

Implementa un `EntityManager` genérico con: `persist(T)` que auto-genera el id, `find(int id)` que devuelve la entidad, `merge(T)` que actualiza, y `remove(int id)` que elimina. Usa `Producto(int id, String nombre, double precio)` como entidad. El `main` ejecuta las cuatro operaciones imprimiendo cada resultado.

---

**Ejercicio 3 — @OneToMany simulado**

Crea una clase `Pedido` con `id`, `cliente` y una lista de `LineaPedido`. Cada `LineaPedido` tiene `id`, `producto` y `cantidad`. Implementa un `PedidoRepository` con `save(Pedido)` y `findById(int)`. El `main` crea un pedido con 3 líneas, lo guarda y lo recupera, imprimiendo todas sus líneas.

Las anotaciones JPA van como comentarios: `// @Entity`, `// @OneToMany`, `// @JoinColumn`.

---

**Ejercicio 4 — JPQL básico simulado**

Implementa un `UsuarioRepository` que gestione una lista interna de usuarios con campos `nombre`, `activo` y `depto`. Proporciona cuatro métodos de consulta: `findAll()`, `findByActivo(boolean)`, `findByNombreContaining(String)`, y `countByDepto(String)`. El `main` carga varios usuarios de prueba y muestra el resultado de cada consulta.

---

**Ejercicio 5 — @GeneratedValue IDENTITY**

Crea un `AutoIncrementIdGenerator` usando `AtomicInteger`. Intégralo en un `EntityManager` que asigne id automáticamente en cada `persist`. Demuestra que dos entidades distintas reciben ids consecutivos, incluso si se crearon sin id.

---

**Ejercicio 6 — Dirty checking**

Implementa un `EntityManager` que, al hacer `persist`, guarde un snapshot (copia) del estado inicial de cada entidad. El método `flush()` compara el estado actual de cada entidad gestionada con su snapshot y, si hay diferencias, imprime la sentencia SQL equivalente: `UPDATE Usuario SET nombre=X WHERE id=Y`. El `main` modifica algunas entidades gestionadas y llama a `flush()`.
