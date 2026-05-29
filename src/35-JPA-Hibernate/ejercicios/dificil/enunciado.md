# Ejercicios — 35 JPA / Hibernate

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Unit of Work completo**

Implementa un `UnitOfWork` que gestione:

- **Identity map**: si se pide la misma entidad dos veces por id, la segunda llamada devuelve la instancia en memoria sin ir a "BD".
- **Dirty tracking**: snapshot del estado en el momento del registro.
- **Cola de nuevos objetos** (`registerNew`).
- **Cola de objetos eliminados** (`registerDeleted`).

El método `commit()` ejecuta los INSERTs para los nuevos, los UPDATEs para los modificados (dirty) y los DELETEs para los eliminados, en ese orden. El `main` registra varios objetos, modifica algunos y llama a `commit()`, demostrando el identity map y el dirty tracking.

---

**Ejercicio 2 — Herencia SINGLE_TABLE**

Crea una jerarquía de herencia:

- `Vehiculo` (base) con `id`, `marca` y `tipo` (discriminador).
- `Coche` extiende `Vehiculo` con campo `puertas`.
- `Camion` extiende `Vehiculo` con campo `cargaMaxTon`.

Implementa `VehiculoRepository` que almacena todos en un mismo `Map<Integer, Vehiculo>` usando el campo `tipo` como discriminador. Proporciona `findCochePorMarca(String)` y `findCamiones()`. El `main` persiste una mezcla de vehículos y los recupera filtrando por tipo.

Las anotaciones van como comentarios: `// @Inheritance(strategy = InheritanceType.SINGLE_TABLE)`, `// @DiscriminatorColumn`, `// @DiscriminatorValue`.

---

**Ejercicio 3 — Specification pattern para queries**

Define la interfaz `Specification<T>` con un método `boolean isSatisfiedBy(T entity)`. Implementa las composiciones `AndSpec`, `OrSpec` y `NotSpec`. Crea tres specs concretas para `Empleado(int id, String nombre, String depto, double salario, boolean activo)`:

- `PorDepto(String depto)`
- `SalarioMayorQue(double minimo)`
- `Activo()`

Añade `findAll(Specification<Empleado>)` al `EmpleadoRepository`. El `main` construye queries complejas combinando specs con AND/OR/NOT y filtra la lista de empleados.

---

**Ejercicio 4 — Connection pool simulado**

Implementa `ConnectionPool(int size)` con un `Semaphore` que limite el número de conexiones simultáneas. Cada `Connection` tiene un `id`, un método `execute(String sql)` que imprime el id de conexión y la query, y un método `close()` que libera el permiso del semáforo. El `main` lanza 10 threads que piden conexión, ejecutan una "query" con un sleep breve, y la devuelven. Con un pool de tamaño 3, los demás threads esperan. Imprime cuándo cada thread adquiere y libera su conexión.

---

**Ejercicio 5 — Segunda caché con expiración, eviction y estadísticas**

Implementa `CacheL2<K,V>` con:
- **TTL por entrada**: cada valor almacenado expira tras un tiempo configurable.
- **Política LRU**: cuando la caché supera `maxSize`, desaloja la entrada menos recientemente usada.
- **Estadísticas**: contador de `hits`, `misses` y `evictions`.

Integra la caché en un `EntityManager` que simula acceso a BD con un delay de 10 ms por consulta. El `main` realiza 20 lecturas (mezcla de ids conocidos y desconocidos), deja expirar algunas entradas avanzando un reloj simulado y muestra las estadísticas finales (`hits`, `misses`, `evictions`, `hit rate %`).

---
