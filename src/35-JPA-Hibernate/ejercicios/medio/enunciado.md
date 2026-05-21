# Ejercicios — 35 JPA / Hibernate

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Problema N+1**

Crea las clases `Depto` con id y nombre, y `Empleado` con id, nombre y referencia a su depto. Implementa un `EmpleadoRepository` con un contador de queries. Simula dos modos de carga:

- **LAZY**: al acceder a los empleados de cada depto se lanza una query adicional → 1 query para cargar los 4 deptos + 4 queries individuales = 5 total.
- **EAGER / FETCH JOIN**: todos los datos se cargan en 1 sola query.

El `main` ejecuta ambos modos e imprime cuántas queries se ejecutaron en cada caso.

---

**Ejercicio 2 — Optimistic Locking (@Version)**

Crea `Cuenta(int id, double saldo, int version)`. El `EntityManager` lanza `OptimisticLockException` si la versión del objeto no coincide con la versión almacenada en el mapa interno. En cada merge exitoso, incrementa la versión. El `main` simula dos "transacciones" concurrentes que leen la misma cuenta: la primera actualiza correctamente y la segunda falla porque trabaja con la versión obsoleta.

---

**Ejercicio 3 — Caché L2 simulada**

Implementa una `CacheL2` con un `Map` interno y estadísticas de `hits` y `misses`. Integra la caché en un `EntityManager`: `findById` consulta primero la `CacheL2`; si hay miss, va al almacén principal y guarda el resultado en caché. El `main` realiza varias búsquedas del mismo id y distintos ids, luego imprime las estadísticas finales de la caché.

---

**Ejercicio 4 — @Transactional con rollback**

Implementa una clase `Transaction` con `begin()`, `commit()` y `rollback()`. Crea un `ServicioBancario` que transfiera dinero entre dos cuentas dentro de una transacción: si la cuenta origen no tiene saldo suficiente, lanza excepción y hace rollback restaurando los saldos originales. El `main` prueba una transferencia válida y una inválida, verificando la integridad de los saldos en ambos casos.

---

**Ejercicio 5 — Bulk operations**

Crea un `UsuarioRepository` con 100 usuarios distribuidos entre varios departamentos. Implementa dos variantes de la misma operación:

- `updateActivoByDepto` (modo ineficiente): itera con un bucle y actualiza uno a uno.
- `bulkUpdateActivo` (modo eficiente): reemplaza el contenido del repositorio filtrando y reconstruyendo la lista en una sola pasada.

El `main` mide el tiempo de cada variante con `System.nanoTime()` y compara los resultados.
