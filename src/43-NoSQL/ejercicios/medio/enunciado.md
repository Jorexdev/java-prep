# Ejercicios — 43 NoSQL

## Medio

**Ejercicio 1 — Aggregation pipeline**

Simula un aggregation pipeline con 3 stages. Tienes una lista de `Pedido(id, clienteId, total, estado)`. Implementa: `$match` (filtra por estado "completado"), `$group` (agrupa por clienteId, suma total), `$sort` (ordena por total descendente). Usa streams de Java para cada stage. En el `main`, procesa 10 pedidos y muestra los 3 mejores clientes por gasto.

---

**Ejercicio 2 — Cache aside pattern**

Implementa Cache Aside: `CacheAsideService` con `Cache` (HashMap) y `Database` (Map que simula DB). El método `get(id)`: primero busca en cache; si no está (miss), carga de DB, guarda en cache, devuelve. `update(id, value)`: actualiza DB y **evicta** de cache (el siguiente get recargará). En el `main`, demuestra get (miss→load), get (hit), update (evict), get (miss→reload).

---

**Ejercicio 3 — Redis pub/sub**

Crea un sistema pub/sub simplificado: `RedisPubSub` con `Map<String, List<Consumer<String>>>` (channel → subscribers). Implementa `subscribe(channel, handler)`, `publish(channel, message)` (llama a todos los handlers). En el `main`, 3 suscriptores en "noticias", 2 en "alertas"; publica mensajes en ambos canales y muestra quién recibe qué.

---

**Ejercicio 4 — LRU Cache**

Implementa un `LRUCache<K, V>` (Least Recently Used) con tamaño máximo usando `LinkedHashMap` con `accessOrder=true`. Override `removeEldestEntry` para evictar cuando se supera el tamaño. En el `main`, crea cache de tamaño 3, inserta 5 elementos, muestra qué se eviccionó y en qué orden.
