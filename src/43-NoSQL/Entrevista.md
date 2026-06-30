<div align="center">
  <a href="#"><img src="../../assets/modules/banner-43-nosql-v1.svg" width="100%" alt=""/></a>
</div>
<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Cuándo usarías MongoDB en lugar de una base de datos relacional?**

MongoDB encaja cuando el modelo de datos es naturalmente documental: entidades auto-contenidas que raramente necesitan relacionarse con otras mediante JOINs. El caso arquetípico es un catálogo de productos donde cada producto tiene atributos distintos — un televisor tiene resolución y tamaño de pantalla, una camiseta tiene talla y color, un libro tiene ISBN y número de páginas. En SQL necesitarías una tabla EAV o columnas nullable; en MongoDB cada documento tiene exactamente los campos que necesita, sin overhead de schema.

Otros contextos donde MongoDB gana: perfiles de usuario con datos anidados variables (redes sociales, personalización), event logs donde la estructura del evento evoluciona con el tiempo, y content management systems donde cada tipo de contenido tiene su propio schema.

Cuándo **no** usar MongoDB: datos altamente relacionales que requieren JOINs frecuentes entre múltiples entidades (facturación, ERP, sistemas contables), transacciones ACID complejas que tocan múltiples colecciones simultáneamente — MongoDB soporta transacciones multi-documento pero requieren replica set y tienen mayor overhead que en un RDBMS. Si la consistencia transaccional es el núcleo del sistema, un RDBMS maduro es más adecuado.

---

**¿Qué es el aggregation pipeline de MongoDB y cómo funciona?**

El aggregation pipeline es el mecanismo de procesamiento analítico de MongoDB. Los documentos de una colección fluyen en secuencia a través de stages; cada stage recibe el stream de salida del anterior y emite un stream transformado. Es conceptualmente equivalente a una pipeline de Unix: `collection | $match | $group | $sort | $project`.

Los stages más frecuentes en entrevistas: `$match` filtra documentos por condición — si aparece al inicio del pipeline puede usar índices, lo que es crítico para el rendimiento. `$group` agrupa documentos por una expresión y calcula acumuladores (`$sum`, `$avg`, `$min`, `$max`, `$push` para arrays). `$sort` ordena el stream. `$project` selecciona y transforma campos, equivalente al SELECT de SQL. `$lookup` hace un LEFT OUTER JOIN con otra colección: `{ from: "clientes", localField: "clienteId", foreignField: "_id", as: "cliente" }`. `$unwind` expande un array: un documento con un array de N elementos produce N documentos separados, uno por elemento.

La ventaja sobre mapReduce (el mecanismo anterior): el pipeline puede aprovechar índices en `$match`, es más legible y tiene rendimiento significativamente mejor. El aggregation pipeline puede procesarse parcialmente en los shards antes de combinar resultados en el router, lo que permite agregaciones distribuidas eficientes.

---

**¿Qué estructuras de datos ofrece Redis y cuándo usar cada una?**

Redis expone seis estructuras principales, cada una con sus propios comandos y semántica:

**String** — el tipo más básico: bytes arbitrarios asociados a una key. Sirve para caché simple (`SET/GET`), contadores atómicos (`INCR/DECR` garantizan atomicidad sin race conditions), flags de feature (`SET feature:dark-mode true`), y almacenamiento de JSON serializado. `EXPIRE` añade TTL a cualquier key.

**Hash** — mapa de campo-valor dentro de una key. Ideal para representar objetos con múltiples atributos: una sesión de usuario (`HSET session:42 userId 42 role ADMIN`), un perfil (`HGETALL session:42` devuelve todos los campos en un único round-trip). Más eficiente en memoria que un String JSON si el objeto tiene muchos campos accedidos individualmente.

**List** — lista enlazada doblemente. `LPUSH/RPOP` implementa una cola FIFO; `LPUSH/LPOP` implementa un stack. Útil para historial de actividad reciente (`LTRIM` para mantener solo los últimos N), colas de tareas ligeras, y feeds de actividad. `BRPOP` permite espera bloqueante — el consumer espera hasta que haya elementos.

**Set** — colección de strings únicos sin orden. `SADD/SISMEMBER` para tags únicos por artículo, miembros de un grupo, deduplicación de eventos. `SUNION/SINTER/SDIFF` para operaciones entre sets (ej: usuarios que siguen a A y B).

**Sorted Set** — como Set pero cada elemento tiene un score numérico que determina el orden. `ZADD leaderboard 2300 "carlos"`, `ZRANGE leaderboard 0 9 REV` para top 10. `ZRANK` devuelve la posición de un elemento. Perfecto para rankings, leaderboards, y colas de prioridad donde el score es la prioridad.

---

**¿Cómo funciona `@Cacheable` de Spring con Redis como backend?**

`@Cacheable` es un advice de Spring AOP. Al llamar al método anotado, Spring genera una cache key (por defecto: nombre del método + parámetros serializados) y consulta el `CacheManager`. Si hay hit en Redis, devuelve el valor deserializado sin ejecutar el método — el cuerpo del método nunca se invoca. Si hay miss, ejecuta el método, serializa el resultado y lo almacena en Redis con el TTL configurado, luego devuelve el valor.

```java
// Configuración típica con RedisTemplate
@Bean
public RedisCacheConfiguration cacheConfig() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeValuesWith(RedisSerializationContext.SerializationPair
            .fromSerializer(new GenericJackson2JsonRedisSerializer()));
}
```

`@CacheEvict` invalida una o varias keys al ejecutarse — el siguiente `@Cacheable` sobre esa key hará miss y recargará desde DB. `@CachePut` siempre ejecuta el método Y actualiza Redis, sin consultar el caché previamente; útil en operaciones de actualización donde quieres mantener el caché caliente sin obligar al siguiente caller a hacer miss.

La diferencia clave con Caffeine (caché en memoria): Redis es **distribuido y persistente**. Todas las instancias de tu aplicación comparten el mismo caché. Si una instancia actualiza un producto y hace `@CacheEvict`, todas las demás instancias verán el miss en la siguiente llamada. Con Caffeine, cada instancia tiene su caché local — una actualización en una instancia no invalida el caché de las otras, lo que puede causar lecturas de datos obsoletos en entornos multi-instancia.

---

**¿Qué estrategias de evicción tiene Redis y cuál elegirías para caché de sesiones?**

Redis tiene ocho políticas de evicción configurables via `maxmemory-policy`:

- `noeviction` — rechaza escrituras cuando se alcanza `maxmemory`. Apropiado cuando la pérdida de datos no es aceptable.
- `allkeys-lru` — evicta cualquier key usando LRU (Least Recently Used). Buena opción general para caché.
- `allkeys-lfu` — evicta cualquier key usando LFU (Least Frequently Used). Mejor cuando la popularidad importa más que la recencia.
- `volatile-lru` — evicta solo keys que tienen TTL configurado, usando LRU. Las keys sin TTL nunca se evictan.
- `volatile-lfu` — igual que volatile-lru pero usando LFU.
- `volatile-ttl` — evicta primero las keys con TTL más próximo a expirar.
- `allkeys-random` — evicta keys aleatorias. Raramente útil.
- `volatile-random` — evicta keys con TTL aleatorias.

Para **caché de sesiones**: `volatile-lru`. Las sesiones siempre deberían tener TTL configurado (el usuario cierra sesión por inactividad), así que `volatile-lru` evicta las sesiones menos usadas recientemente cuando la memoria se llena, preservando sesiones activas. Si usaras `allkeys-lru`, podrías evictar datos que no son sesiones y que no deberían perderse. Si usaras `noeviction`, Redis empezaría a rechazar escrituras cuando se llene, lo cual es peor.

---

**¿Cómo evitas el cache stampede (dog piling) con Redis?**

El cache stampede ocurre cuando una key popular expira y múltiples threads (o instancias) detectan el miss simultáneamente y todos van a la base de datos a cargar el mismo dato al mismo tiempo, generando una carga súbita que puede tumbar la DB.

Cuatro estrategias para mitigarlo:

**1. Locking optimista (mutex):** cuando un thread detecta miss, intenta adquirir un lock distribuido (ej: `SET lock:key value NX EX 5`). Solo el thread que adquiere el lock va a DB; el resto espera y reintenta leer del caché. Cuando el primer thread termina y escribe en Redis, los que esperaban encuentran el valor.

**2. Background refresh:** un proceso en background monitoriza el TTL de keys críticas y las renueva antes de que expiren. La key nunca llega a expirar desde el punto de vista de los callers. Implementable con un scheduled job que ejecuta `TTL key` y si queda menos de N segundos recarga el valor.

**3. Probabilistic Early Expiration (PER):** cuando un thread lee una key, calcula con probabilidad creciente si debe renovarla antes de que expire, basándose en el tiempo restante. Cuanto más cerca de expirar, mayor probabilidad de que ese thread la renueve proactivamente. El primer thread que "gana la lotería" recarga el valor.

**4. TTL con jitter:** en lugar de que todas las keys de un mismo tipo tengan el mismo TTL (ej: 300 segundos), se añade un valor aleatorio pequeño: `TTL = 300 + random(0, 30)`. Esto distribuye las expiraciones en el tiempo, evitando que muchas keys expiren simultáneamente y provoquen stampedes coordinados.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
