<div align="center">
  <a href="#"><img src="../../assets/modules/banner-39-microservicios-v1.svg" width="100%" alt=""/></a>
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

Una arquitectura de microservicios divide un sistema en **servicios pequeños, autónomos y deployables independientemente**. Cada servicio tiene un dominio delimitado (Bounded Context), su propia base de datos y su propio ciclo de despliegue. Los equipos pueden desarrollar, testear y desplegar sus servicios sin coordinar con otros.

Frente al monolito: el monolito escala como una unidad (toda la app o ninguna parte) y un fallo en una sección puede derribar todo el sistema. Los microservicios permiten **escalar solo el servicio que necesita más recursos** y limitan el radio de explosión de un fallo a un servicio.

**Cuándo NO usar microservicios:** Si el equipo es pequeño (< 5-10 personas), el overhead operacional (CI/CD por servicio, service discovery, observabilidad distribuida) supera el beneficio. El riesgo mayor es crear un **distributed monolith**: servicios acoplados por llamadas síncronas que se despliegan juntos de facto — lo peor de ambos mundos.

```
Monolito:
  [UI → Service A → Service B → Service C → DB única]
  Problema: fallo en C deriba todo; escalar = escalar todo

Microservicios:
  [API Gateway]
       ↓
  [Servicio Pedidos]──DB Pedidos
  [Servicio Inventario]──DB Inventario   ← cada uno escala independiente
  [Servicio Pagos]──DB Pagos
  [Servicio Envío]──DB Envío
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Service Discovery (Eureka/Consul)**

Los servicios no conocen las IPs de sus dependencias — se registran en un registry al arrancar con su nombre lógico. El cliente consulta el registry para obtener instancias disponibles y el load balancer (Ribbon/Spring Cloud LoadBalancer) distribuye las llamadas.

```java
// Spring Cloud Eureka (server):
@EnableEurekaServer
@SpringBootApplication
public class EurekaServer { ... }

// Servicio cliente:
@EnableDiscoveryClient
@SpringBootApplication
public class ServicioPedidos { ... }

// Llamada con nombre lógico (no IP):
@Autowired RestTemplate restTemplate; // con @LoadBalanced
String respuesta = restTemplate.getForObject(
    "http://servicio-inventario/stock/{productoId}", String.class, productoId);
```

**API Gateway (Spring Cloud Gateway)**

Punto de entrada único para todos los servicios. Responsable de: routing, autenticación/autorización centralizada, rate limiting, logging de requests, CORS. Evita que los clientes necesiten conocer la topología interna.

```java
// application.yml:
// spring.cloud.gateway.routes:
//   - id: pedidos-route
//     uri: lb://servicio-pedidos
//     predicates:
//       - Path=/api/pedidos/**
//     filters:
//       - StripPrefix=1
//       - name: RequestRateLimiter
//         args:
//           redis-rate-limiter.replenishRate: 10
//           redis-rate-limiter.burstCapacity: 20
```

**Circuit Breaker (Resilience4j)**

Envuelve llamadas remotas para proteger el sistema de fallos en cascada.

```
Estado CLOSED (normal):
  Todas las llamadas pasan. Se registran éxitos/fallos.
  Si failureRate > threshold → transición a OPEN

Estado OPEN (protegido):
  Las llamadas se rechazan inmediatamente (sin llamar al servicio).
  Se llama al fallback. Tras waitDuration → transición a HALF_OPEN

Estado HALF_OPEN (prueba):
  Permite N llamadas de prueba.
  Si pasan: → CLOSED
  Si fallan: → OPEN (esperar de nuevo)
```

```java
@CircuitBreaker(name = "inventario", fallbackMethod = "stockFallback")
public int consultarStock(String productoId) {
    return inventarioClient.getStock(productoId);
}

public int stockFallback(String productoId, Exception e) {
    log.warn("Circuit breaker abierto para inventario, usando caché");
    return cacheLast.getOrDefault(productoId, 0);
}
```

**Comunicación síncrona vs asíncrona**

| | Síncrona (REST/gRPC) | Asíncrona (Kafka/RabbitMQ) |
|---|---|---|
| Acoplamiento temporal | Alto (el caller espera) | Bajo (fire-and-forget) |
| Fallo del receptor | Propagado al caller | El mensaje queda en cola |
| Casos de uso | Queries, CRUD, necesito respuesta | Eventos de dominio, workflows largos |
| Consistencia | Fuerte (en el momento) | Eventual |

**Saga Pattern (transacciones distribuidas)**

Sin transacciones ACID distribuidas, la consistencia entre servicios se gestiona con Sagas.

- **Coreografía**: cada servicio escucha eventos y emite el siguiente. Desacoplado pero difícil de trazar.
- **Orquestación**: un orquestador central dirige los pasos. Más visible pero introduce un punto central.

```
Saga "Crear Pedido" (coreografía):
  PedidoService → emite PedidoCreado
  InventarioService → escucha PedidoCreado → reserva stock → emite StockReservado
  PagoService → escucha StockReservado → procesa pago → emite PagoCompletado
  EnvioService → escucha PagoCompletado → crea envío

  Si PagoService falla:
  PagoService → emite PagoFallido
  InventarioService → escucha PagoFallido → libera stock → emite StockLiberado
  PedidoService → escucha StockLiberado → cancela pedido
```

**Outbox Pattern**

Problema: publicar a Kafka y escribir en BD en la misma "transacción" sin Two-Phase Commit. Solución: escribir el evento en una tabla `outbox` en la misma transacción BD, y un proceso separado (polling o CDC con Debezium) lee la tabla y publica a Kafka. Garantiza que el evento se publica si y solo si la escritura en BD fue exitosa.

**Observabilidad**

- **Micrometer**: métricas (contador de requests, latencia, errores) exportadas a Prometheus/Grafana
- **Zipkin/Jaeger**: trazado distribuido. Cada request recibe un `traceId` propagado por headers HTTP (`X-B3-TraceId`). Permite ver el flujo completo a través de todos los servicios con tiempos por hop.
- **Correlation IDs**: el gateway añade un header `X-Correlation-Id` a cada request entrante. Todos los logs incluyen ese ID para poder correlacionar logs de distintos servicios.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Escalado independiente** — Si el Servicio de Búsqueda recibe 10× más tráfico que el de Pedidos, se escala solo ese servicio. En un monolito habría que escalar todo el sistema multiplicando el coste de infra.

**Despliegue autónomo por equipo** — Cada equipo despliega su servicio a producción sin coordinación con otros. Permite ciclos de release cortos y reducción del riesgo por despliegue (cambios pequeños y frecuentes son más seguros que cambios grandes e infrecuentes).

**Resiliencia** — Un fallo en el Servicio de Recomendaciones no impide hacer pedidos. Con Circuit Breaker y fallbacks, los servicios degradan graciosamente en lugar de propagarse el fallo en cascada. En un monolito, un hilo bloqueado puede agotar el pool de threads y derribar todo el proceso.

Ver [ExpCircuitBreaker.java](ExpCircuitBreaker.java), [ExpAPIGateway.java](ExpAPIGateway.java), [ExpServiceMesh.java](ExpServiceMesh.java), [ExpSagaPattern.java](ExpSagaPattern.java), [ExpCQRS.java](ExpCQRS.java) y [ExpDistributedTracing.java](ExpDistributedTracing.java) para ejemplos ejecutables con circuit breaker, API gateway, saga, CQRS y trazado distribuido.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
