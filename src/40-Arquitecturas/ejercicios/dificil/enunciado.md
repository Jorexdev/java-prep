# Ejercicios — 40 Arquitecturas

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Event Sourcing completo**
Implementa `EventStore` append-only con `Map<String, List<StoredEvent>>`. `StoredEvent(String aggregateId, int version, String type, Map<String,String> data)`. `CuentaBancaria` se reconstituye desde eventos: `CuentaAbierta`, `Depositado`, `Retirado`, `Bloqueada`. Implementa `CuentaBancaria.reconstituir(List<StoredEvent>)` que aplica cada evento y `reconstituir(List<StoredEvent> events, int upToVersion)` para temporal queries. El `main` abre una cuenta, deposita 3 veces, retira 1 vez y la bloquea; después reconstruye el estado actual y en las versiones 2 y 4.

**Ejercicio 2 — CQRS + Event Sourcing combinados**
Write side: `Inventario` con event sourcing usando `ProductoAñadido(id, nombre, stock)`, `StockAjustado(id, delta)` y `ProductoEliminado(id)`. Read side: proyección `InventarioView(Map<String, Integer> stockPorProducto)`. `CommandHandler` procesa commands, guarda eventos en `EventStore` y publica a `ProjectionUpdater`. Commands: `AñadirProducto`, `AjustarStock`, `EliminarProducto`. El `main` aplica 8 commands, consulta la proyección, la reconstruye desde 0 y verifica que sea consistente.

**Ejercicio 3 — Aggregate con Snapshots**
Implementa `SnapshotStore` con `Map<String, Snapshot(int version, Object state)>`. Añade a `CuentaBancaria` los métodos `toSnapshot()` y `fromSnapshot(Snapshot)`. Estrategia: tomar snapshot cada 5 eventos. Al reconstituir: cargar el snapshot más reciente y hacer replay solo de los eventos posteriores. El `main` genera 13 eventos para una cuenta y verifica que la reconstitución usa el snapshot (solo reprocesa 3 eventos, no 13). Debe imprimir: `"Usando snapshot v10, replaying 3 eventos"`.

**Ejercicio 4 — Anti-Corruption Layer completo**
Sistema legado: `LegacyClienteDTO(String codCliente, String nombreCompleto, String direccionCompleta)`. Dominio nuevo: `Cliente(UUID id, Nombre nombre, Direccion direccion)`, `Nombre(String given, String family)`, `Direccion(String calle, String ciudad)`. Implementa `LegacyClienteAdapter implements ClienteRepository` (del dominio) que traduce usando `LegacyTranslator`. `LegacyTranslator` parsea `nombreCompleto` ("Nombre Apellido") en `Nombre` y `direccionCompleta` ("Calle, Ciudad") en `Direccion`. El `main` usa `ClienteRepository` del dominio sin saber que hay un sistema legado detrás. Cambia el legado (renombra un campo) y demuestra que solo hay que tocar el translator.
