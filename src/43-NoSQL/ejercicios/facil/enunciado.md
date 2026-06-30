# Ejercicios — 43 NoSQL

## Fácil

**Ejercicio 1 — CRUD en MongoDB**

Crea una clase `Document` que encapsula un `Map<String, Object>`. Crea `Collection` con lista de documentos y métodos `insertOne(Document)`, `findAll()`, `findById(String id)`. En el `main`, inserta 3 usuarios (`id`, `nombre`, `email`), busca por id, imprime todos.

---

**Ejercicio 2 — Redis String con TTL**

Crea una clase `RedisString` con `Map<String, String>` para valores y `Map<String, Long>` para timestamps de expiración. Implementa `set(key, value, ttlSeconds)`, `get(key)` (que devuelve null si expiró), `ttl(key)`. En el `main`, set "session:123" con TTL 2s, lee inmediatamente (éxito), espera 3s, lee de nuevo (null = expirado).

---

**Ejercicio 3 — Cache hit/miss**

Crea una clase `Cache` con `HashMap<String, String>` y un contador `dbCalls`. Implementa `get(String key)`: si está en cache devuelve sin incrementar dbCalls; si no, simula llamada a DB (incrementa dbCalls, guarda en cache, devuelve). En el `main`, haz 5 llamadas con las mismas 2 keys y muestra cuántas fueron a DB.

---

**Ejercicio 4 — Sorted Set (ranking)**

Crea `SortedSet` con `TreeMap<Double, String>` (score → nombre). Implementa `zadd(nombre, score)`, `zrange()` (de menor a mayor score), `zrank(nombre)` (posición 0-indexed). En el `main`, añade 5 jugadores con sus puntos, imprime el ranking y la posición de uno concreto.

---

**Ejercicio 5 — Redis Hash como sesión**

Crea `RedisHash` con `Map<String, Map<String, String>>` (key → fields). Implementa `hset(key, field, value)`, `hget(key, field)`, `hgetall(key)`, `hdel(key, field)`. En el `main`, crea sesión `"session:user:42"` con campos `userId`, `role`, `lastSeen`, léelos todos, elimina `lastSeen`, muestra el estado final.
