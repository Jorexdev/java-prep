# Ejercicios — 39 Microservicios

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Outbox Pattern completo

Implementa el patrón Outbox para garantizar consistencia entre la escritura en base de datos y la publicación de eventos.

`Database` contiene `Map<Integer, Pedido> pedidos` y `List<OutboxEvent> outbox`. `PedidoService.crear(String producto, double precio)` escribe el pedido y un evento outbox en un bloque `synchronized` (transacción simulada). `OutboxRelay` (Runnable en hilo background) lee los eventos `PENDING`, los publica en `List<String> publishedEvents` y los marca como `PUBLISHED`. El `main` crea 5 pedidos, arranca el relay, espera con `CountDownLatch` y verifica que los 5 eventos fueron publicados.

---

## Ejercicio 2 — Choreography Saga completa

Implementa una saga coreografiada donde 5 servicios se comunican exclusivamente a través de un `EventBus` pub/sub en memoria.

`PedidoService` crea pedidos y escucha cancelaciones. `InventarioService` reserva stock o publica `StockAgotado`. `PagoService` cobra o publica `PagoRechazado`. `EnvioService` crea el envío al recibir pago aprobado. `NotificacionService` notifica el resultado final. El `main` simula 3 flujos: compra exitosa, fallo por stock agotado, fallo por pago rechazado.

---

## Ejercicio 3 — Sidecar Proxy

Implementa un `Sidecar` que envuelve cualquier servicio e intercepta sus llamadas aplicando de forma transparente: circuit breaker (threshold=2), retry (max=2), timeout simulado con variable de control, y métricas por operación (`Map<String, int[]>` con total y errores). `CatalogoService` lanza excepción cuando el parámetro contiene "error". El `main` simula 10 llamadas con 3 errores intercalados e imprime las métricas al final.

---

## Ejercicio 4 — Two-Phase Commit simulado

Implementa el protocolo 2PC con `ResourceManager` (prepare/commit/rollback) y `TransactionCoordinator`. En la Fase 1 el coordinador llama `prepare` a todos los participantes; si alguno responde NO, llama `rollback` a todos los que respondieron YES. En la Fase 2, si todos responden YES, llama `commit` a todos. El `main` prueba tres escenarios: todos confirman (éxito), uno falla en prepare (abort con rollback parcial), todos confirman pero uno falla en commit (necesita recovery).

---

## Ejercicio 5 — Saga coreografiada con compensación automática

Implementa una saga coreografiada mediante un `EventBus` en memoria donde cada servicio reacciona a eventos y publica nuevos eventos o eventos de compensación. Define los servicios: `PedidoService`, `InventarioService`, `PagoService` y `EnvioService`. Cada servicio escucha solo sus eventos de interés. Si un servicio falla, publica un evento de compensación (`PedidoCancelado`, `StockLiberado`, `ReembolsoEmitido`) que los servicios anteriores reciben y usan para deshacer su trabajo. La compensación es automática y reactiva, sin orquestador central. El `main` simula tres flujos: flujo exitoso completo, fallo en `PagoService` (desencadena `StockLiberado` y `PedidoCancelado`) y fallo en `InventarioService` (desencadena `PedidoCancelado`).

---
