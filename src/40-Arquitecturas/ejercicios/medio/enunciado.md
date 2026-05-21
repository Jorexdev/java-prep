# Ejercicios — 40 Arquitecturas

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Arquitectura Hexagonal completa**
Dominio: `Cuenta(int id, String titular, double saldo)`, interfaz `CuentaRepository` (puerto de salida) y `TransferirUseCase` (puerto de entrada). Adaptadores de salida: `MemoriaCuentaRepository` y `LoggingCuentaRepository` (decorator que loguea y delega). Adaptadores de entrada: `ApiController` (simula HTTP con `transfer(int from, int to, double amount)`) y `BatchProcessor` (procesa `List<String>` con instrucciones en formato `"from:to:amount"`). El `main` crea 3 cuentas y ejecuta transferencias via ambos adaptadores con ambos repositorios.

**Ejercicio 2 — CQRS con modelos separados**
Write model: `CuentaBancaria(id, titular, saldo)` con operaciones `depositar` y `retirar`. Read model: `CuentaResumen(id, titular, saldo, numOperaciones)` actualizado por eventos. `CommandBus.dispatch(Command)` invoca el handler correspondiente y publica un `DomainEvent`. Un `EventHandler` actualiza el read model. `QueryBus.query(Query)` lee del read model. El `main` ejecuta 4 commands, consulta el read model y verifica que `numOperaciones` sea correcto.

**Ejercicio 3 — Bounded Context con ACL**
Contexto Ventas: `PedidoVentas(id, clienteId, total)`. Contexto Facturación: `Factura(id, nif, importe)`. `AntiCorruptionLayer` traduce `clienteId → nif` usando `ClienteInfoProvider`. `FacturacionService.facturar(PedidoVentas pedido)` solo conoce su propio modelo. El `main` crea un pedido en Ventas, el ACL traduce y se crea la factura en Facturación. Demuestra que cambiar el modelo de Ventas no afecta a Facturación.

**Ejercicio 4 — Domain Event Bus síncrono y asíncrono**
Implementa `SyncEventBus` (entrega en el mismo thread, en orden) y `AsyncEventBus` (`ExecutorService.submit`, handlers en threads separados). `PedidoService` usa `EventBus` por interfaz. Incluye un handler lento que tarda 200 ms (`Thread.sleep`). El `main` demuestra que con `SyncEventBus` la llamada a `crearPedido()` tarda ≥200 ms y con `AsyncEventBus` retorna inmediatamente.

**Ejercicio 5 — Specification pattern componible**
Define la interfaz `Specification<T>` con `isSatisfiedBy(T)` y los métodos `default and`, `default or`, `default not`. Implementa `Producto(String id, String categoria, double precio, int stock, boolean activo)` y las specs `PrecioEntre(min, max)`, `CategoriaDe(String cat)`, `EnStock()` y `Activo()`. Implementa `ProductoCatalogo.buscar(Specification<Producto>)`. El `main` usa la composición `PrecioEntre(10,100).and(EnStock()).and(CategoriaDe("electrónica").not())`.
