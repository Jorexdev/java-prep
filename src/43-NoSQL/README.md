<div align="center">
  <a href="#"><img src="../../assets/modules/banner-43-nosql-v1.svg" width="100%" alt=""/></a>
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

NoSQL ("Not Only SQL") agrupa bases de datos que no siguen el modelo relacional. La diferencia central no es la ausencia de SQL como lenguaje — es el modelo de datos y los compromisos de consistencia que asume cada familia. Donde las bases relacionales ofrecen un esquema fijo, JOINs, y transacciones ACID multi-tabla, las NoSQL priorizan escalabilidad horizontal, flexibilidad de esquema y alto rendimiento para patrones de acceso específicos.

Hay cuatro familias principales. **Document stores** (MongoDB, CouchDB) almacenan documentos JSON o BSON auto-contenidos; un documento puede anidar objetos y arrays, eliminando la necesidad de JOINs para datos relacionados. **Key-value stores** (Redis, DynamoDB) son la abstracción más simple: un mapa distribuido con lecturas y escrituras en O(1). **Column-family** (Cassandra, HBase) organizan los datos en familias de columnas, optimizadas para escrituras masivas y lecturas por rango de filas. **Graph databases** (Neo4j) modelan entidades como nodos y relaciones como aristas; ideales cuando la relación entre datos es el dato relevante.

```
Cuándo SQL vs NoSQL:

  SQL (relacional):
    ✓ Datos altamente normalizados con muchas relaciones
    ✓ Transacciones ACID complejas entre múltiples entidades
    ✓ Queries ad-hoc con JOINs variables
    ✓ Reporting y análisis con agregaciones complejas
    ✗ Schema rígido: cada cambio requiere migración

  NoSQL (document / key-value):
    ✓ Schema flexible o evolutivo sin migraciones
    ✓ Documentos auto-contenidos (leer un doc = un round-trip)
    ✓ Escalabilidad horizontal con sharding automático
    ✓ Patrones de acceso conocidos y predecibles
    ✗ Sin JOINs nativos: la desnormalización es responsabilidad tuya
```

**MongoDB** es un document store: los datos viven en colecciones de documentos BSON (Binary JSON). Un documento es la unidad atómica de lectura y escritura. **Redis** es un key-value store en memoria: las claves mapean a estructuras de datos nativas (Strings, Hashes, Lists, Sets, Sorted Sets). Redis es la elección canónica para caché, sesiones y rankings en tiempo real.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**MongoDB — BSON documents y colecciones**

Un documento MongoDB es un objeto JSON enriquecido con tipos BSON (ObjectId, Date, Binary). Las colecciones no imponen schema: dos documentos en la misma colección pueden tener campos completamente distintos. Esto elimina las migraciones DDL — añadir un campo es simplemente escribir documentos con ese campo nuevo.

```java
// Documento de usuario con array de direcciones anidado
{
  "_id": ObjectId("64a1b2c3d4e5f6a7b8c9d0e1"),
  "nombre": "Ana García",
  "email": "ana@ejemplo.com",
  "edad": 28,
  "direcciones": [
    { "tipo": "casa", "ciudad": "Madrid", "cp": "28001" },
    { "tipo": "trabajo", "ciudad": "Madrid", "cp": "28045" }
  ],
  "fechaRegistro": ISODate("2024-01-15T10:30:00Z")
}
// No hay tabla "direcciones" separada. Sin JOINs. Un documento = un read.
```

**Aggregation pipeline de MongoDB**

El aggregation pipeline es la alternativa a SQL GROUP BY + JOIN + HAVING. Los documentos fluyen en secuencia a través de stages, cada uno transforma el stream:

```
$match    → filtra documentos (como WHERE). Usa índices si está al inicio.
$group    → agrupa y calcula ($sum, $avg, $min, $max, $count)
$sort     → ordena el stream resultante
$project  → selecciona/transforma campos (como SELECT)
$lookup   → LEFT JOIN con otra colección
$unwind   → expande arrays: un doc con array de N → N documentos
$limit    → toma los N primeros
$skip     → salta los N primeros
```

```java
// Ventas totales por categoría, top 5:
db.pedidos.aggregate([
  { $match: { estado: "completado" } },
  { $group: { _id: "$categoria", totalVentas: { $sum: "$importe" } } },
  { $sort: { totalVentas: -1 } },
  { $limit: 5 }
])
```

**Índices en MongoDB**

Sin índice, MongoDB hace collection scan (O(n)). Con índice, la búsqueda es O(log n). Los índices más importantes:

```java
// Índice simple: acelera findByEmail
db.usuarios.createIndex({ email: 1 })  // 1 = ascendente

// Índice compuesto: cubre queries que filtran por estado y ordenan por fecha
db.pedidos.createIndex({ estado: 1, fechaCreacion: -1 })

// Índice único: garantiza unicidad a nivel de base de datos
db.usuarios.createIndex({ email: 1 }, { unique: true })

// Índice TTL: borra documentos automáticamente tras expirar
db.sesiones.createIndex({ creadoEn: 1 }, { expireAfterSeconds: 3600 })
```

**Redis — estructuras de datos nativas**

Redis no es solo un Map. Cada tipo tiene comandos especializados con complejidad conocida:

```
String  → GET/SET/INCR/EXPIRE/TTL — contador, caché simple, flag de feature
Hash    → HSET/HGET/HGETALL/HDEL  — objeto con campos (sesión de usuario, perfil)
List    → LPUSH/RPOP/LRANGE        — cola FIFO (tareas), historial (últimas N acciones)
Set     → SADD/SMEMBERS/SISMEMBER/SUNION — tags únicos, miembros de grupo, deduplicación
Sorted Set → ZADD/ZRANK/ZRANGE    — rankings con score, leaderboards, cola de prioridad
```

```java
// Contador de visitas con expiración a medianoche
SET visitas:home 0
INCR visitas:home          // O(1) — atómico, thread-safe
EXPIRE visitas:home 86400  // expira en 24h

// Sesión de usuario completa en un Hash (un round-trip para leer todo)
HSET session:user:42 userId 42 role ADMIN lastSeen "2024-01-15T10:30:00Z"
HGETALL session:user:42    // devuelve todos los campos

// Ranking de jugadores (Sorted Set)
ZADD leaderboard 1500 "ana"
ZADD leaderboard 2300 "carlos"
ZADD leaderboard 1800 "bea"
ZRANGE leaderboard 0 -1 WITHSCORES REV  // ana:1500, bea:1800, carlos:2300 (descendente)
ZRANK leaderboard "bea"                  // posición 1 (0-indexed, orden ascendente)
```

**Spring Data MongoDB**

Spring Data genera implementaciones de repositorio en tiempo de compilación basadas en el nombre del método:

```java
@Document(collection = "usuarios")
public class Usuario {
    @Id private String id;
    @Field("nombre") private String nombre;
    @Indexed(unique = true) private String email;
    private int edad;
    private String estado;
}

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByEdadGreaterThan(int edad);
    @Query("{'estado': ?0, 'edad': {$gte: ?1}}")
    List<Usuario> findByEstadoAndEdadMinima(String estado, int edadMinima);
}
```

**Spring Cache con Redis**

`@Cacheable` convierte cualquier método en una operación cache-aside transparente vía AOP:

```java
@Service
public class ProductoService {

    @Cacheable(value = "productos", key = "#id")
    public Producto findById(String id) {
        // Solo se ejecuta en cache miss. En cache hit, Spring devuelve el valor
        // almacenado en Redis sin entrar en el método.
        return productoRepository.findById(id).orElseThrow();
    }

    @CacheEvict(value = "productos", key = "#producto.id")
    public Producto actualizar(Producto producto) {
        // Invalida la entry en Redis al actualizar el dato en DB.
        return productoRepository.save(producto);
    }

    @CachePut(value = "productos", key = "#result.id")
    public Producto crear(Producto producto) {
        // Ejecuta el método Y actualiza Redis. Útil para pre-poblar el caché.
        return productoRepository.save(producto);
    }
}
```

La diferencia entre Redis y Caffeine (caché en memoria): Redis es **distribuido** — todos los nodos de tu aplicación comparten el mismo caché. Caffeine es local — cada instancia tiene su propio caché, lo que puede causar inconsistencias en entornos multi-instancia.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Escalabilidad horizontal nativa** — MongoDB escala con sharding automático: los documentos se distribuyen entre shards basándose en la shard key. Redis escala con Redis Cluster: el espacio de keys se divide en 16384 slots distribuidos entre nodos. Ambos permiten escalar añadiendo nodos, sin reescribir la aplicación.

**Schema flexible en MongoDB** — Los catálogos de e-commerce son el caso de uso canónico: un televisor tiene resolución, un libro tiene ISBN, una camiseta tiene talla y color. En SQL necesitas tabla base + tablas de atributos o columnas nullable. En MongoDB cada documento tiene exactamente los campos que necesita. Las migraciones son opcionales y progresivas.

**Redis para patrones de alto rendimiento** — Redis opera en microsegundos porque todo vive en memoria. Los casos de uso donde brilla: caché de resultados de API (evitar round-trips a DB), sesiones de usuario distribuidas (stateless entre instancias), rankings en tiempo real (Sorted Set con ZADD/ZRANGE en O(log n)), colas de tareas ligeras (List con LPUSH/BRPOP), pub/sub para notificaciones en tiempo real, y rate limiting (INCR + EXPIRE sobre una key por IP/usuario).

**Casos de uso por tecnología:**

```
MongoDB:
  ✓ Catálogos de productos con atributos heterogéneos
  ✓ Perfiles de usuario con datos anidados variables
  ✓ Event logs e historial de actividad
  ✓ Gestión de contenidos (CMS, artículos, comentarios)
  ✗ Evitar: datos con muchas relaciones cruzadas, transacciones entre múltiples colecciones

Redis:
  ✓ Caché de segundo nivel (resultado de queries costosas)
  ✓ Sesiones de usuario distribuidas entre instancias
  ✓ Leaderboards y rankings en tiempo real
  ✓ Colas de tareas ligeras y pub/sub
  ✓ Rate limiting y throttling de APIs
  ✗ Evitar: datos que no caben en RAM, queries complejas con filtros variables
```

Ver [ExpMongoDB.java](ExpMongoDB.java), [ExpSpringDataMongo.java](ExpSpringDataMongo.java), [ExpRedis.java](ExpRedis.java) y [ExpSpringCacheRedis.java](ExpSpringCacheRedis.java) para ejemplos ejecutables de MongoDB simulado, Spring Data, estructuras de datos Redis y Spring Cache con patrones de invalidación.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
