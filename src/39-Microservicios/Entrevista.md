<div align="center">
  <a href="#"><img src="../../assets/modules/banner-39-microservicios-v1.svg" width="100%" alt=""/></a>
</div>
<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Cuándo elegiría una arquitectura de microservicios en lugar de un monolito?**

Los microservicios tienen sentido cuando: el equipo es suficientemente grande para mantener servicios separados (regla de "dos pizzas" de Amazon: si un equipo no puede comer con dos pizzas, es demasiado grande para un microservicio), hay dominios bien delimitados con poca dependencia entre sí, necesitas escalar componentes específicos de forma independiente, o equipos distintos necesitan ciclos de despliegue autónomos. Empezaría con un **monolito modular** (módulos bien separados dentro del mismo proceso) y extraería servicios cuando el dolor del monolito sea real y medible: contención de base de datos, necesidad de escalar un componente específico, o equipos bloqueados entre sí para desplegar. El error más común es microserviciar prematuramente — el overhead operacional (CI/CD por servicio, service discovery, trazado distribuido) es significativo.

---

**¿Qué es el Circuit Breaker pattern y cuáles son sus tres estados?**

El Circuit Breaker envuelve llamadas a servicios remotos para evitar que los fallos en cascada deriven todo el sistema. Funciona como un fusible eléctrico con tres estados:

- **CLOSED**: estado normal. Las llamadas pasan. Se registran éxitos y fallos. Si la tasa de fallos supera el umbral configurado (ej. 50% en las últimas 10 llamadas), transiciona a OPEN.
- **OPEN**: el circuit breaker rechaza llamadas inmediatamente sin llegar al servicio remoto, ejecutando el fallback. Tras un `waitDuration` configurable (ej. 30s), transiciona a HALF_OPEN.
- **HALF_OPEN**: permite pasar un número limitado de llamadas de prueba. Si tienen éxito → CLOSED. Si fallan → vuelve a OPEN y espera de nuevo.

El beneficio clave es que durante OPEN se libera presión del servicio caído, dándole tiempo para recuperarse, y el caller responde rápido con el fallback en lugar de esperar timeouts acumulados.

---

**¿Cómo manejas transacciones distribuidas entre microservicios? ¿Qué es el patrón Saga?**

No existe el ACID distribuido sin 2PC (Two-Phase Commit), y 2PC es demasiado costoso y frágil para producción. La alternativa es la **consistencia eventual** a través del patrón Saga: una transacción de negocio se descompone en una secuencia de transacciones locales, cada una en un servicio. Si un paso falla, se ejecutan **transacciones compensatorias** para deshacer los pasos anteriores.

Dos estilos: **coreografía** (cada servicio escucha eventos y emite el siguiente — desacoplado pero difícil de trazar) y **orquestación** (un proceso central dirige los pasos — más visible pero introduce acoplamiento con el orquestador). La clave es que cada transacción local debe ser idempotente y cada compensación debe ser el inverso lógico del paso original.

---

**¿Cuál es la diferencia entre comunicación síncrona y asíncrona entre servicios?**

**Síncrona (REST/gRPC)**: el caller espera la respuesta del servicio remoto. Simple de implementar y fácil de depurar, pero crea acoplamiento temporal — si el servicio remoto está lento o caído, el caller también se degrada. Adecuado para queries y operaciones que requieren la respuesta para continuar.

**Asíncrona (Kafka/RabbitMQ)**: el caller publica un evento y continúa sin esperar. El receptor procesa cuando puede. Desacopla temporalmente los servicios — si el receptor está caído, el mensaje espera en la cola. Adecuado para eventos de dominio, flujos de trabajo largos, y cuando la consistencia eventual es aceptable. La contrapartida es mayor complejidad: hay que manejar idempotencia, orden de mensajes y visibilidad del estado del procesamiento.

---

**¿Qué problemas resuelve el API Gateway?**

El API Gateway es el punto de entrada único al ecosistema de microservicios y resuelve: (1) **Routing**: mapea rutas externas a servicios internos sin que el cliente necesite conocer la topología. (2) **Autenticación/Autorización centralizada**: valida JWT o sesiones una vez en el gateway, los servicios internos confían en headers propagados. (3) **Rate limiting**: protege los servicios de abuso o picos de tráfico. (4) **SSL termination**: los servicios internos se comunican en HTTP plano, el gateway gestiona HTTPS. (5) **Logging/trazado**: añade correlation IDs y registra todas las requests. (6) **Agregación de respuestas**: en algunos casos combina respuestas de múltiples servicios en una sola respuesta al cliente (Backend for Frontend pattern).

---

**¿Qué es el Outbox Pattern y por qué lo usarías con Kafka?**

El problema: cuando un servicio necesita actualizar su BD y publicar un evento a Kafka en la "misma transacción", no hay forma atómica de hacer ambos sin 2PC. Si escribes en BD y luego falla antes de publicar a Kafka, el evento se pierde. Si publicas a Kafka y luego falla la escritura en BD, tienes inconsistencia.

El Outbox Pattern resuelve esto: en la misma transacción BD que escribe los datos de negocio, también escribe el evento en una tabla `outbox` (misma BD). Un proceso separado — un **poller** o un conector CDC como Debezium — lee la tabla outbox y publica los eventos a Kafka. Garantiza que el evento se publica si y solo si la transacción BD fue exitosa, usando solo la atomicidad local de la BD. Debezium lee el write-ahead log de la BD directamente, con latencia de milisegundos y sin polling costoso.

---

**¿Cómo trazas una request que pasa por 5 microservicios diferentes?**

Con **trazado distribuido**: cuando una request entra al sistema (generalmente en el API Gateway), se genera un `traceId` único. Cada servicio que participa añade su propio `spanId` (segmento del trace). Los headers de trazado (`X-B3-TraceId`, `X-B3-SpanId` en Zipkin, o `traceparent` en W3C Trace Context) se propagan en cada llamada HTTP o en los headers de mensajes Kafka.

Stack típico: **Micrometer Tracing** (antes Spring Cloud Sleuth) instrumenta automáticamente RestTemplate, WebClient y Kafka. Los spans se exportan a **Zipkin** o **Jaeger** donde se visualiza el trace completo con latencia por servicio. En los logs, el `traceId` aparece en cada línea, permitiendo hacer `grep traceId` en todos los servicios para correlacionar qué pasó en cada uno. La regla es simple: **todos los logs deben incluir el traceId**, y toda llamada saliente (HTTP, Kafka, gRPC) debe propagar los headers de tracing.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
