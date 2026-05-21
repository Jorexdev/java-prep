# Ejercicios — 40 Arquitecturas

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Value Object**
Implementa `Money(java.math.BigDecimal amount, String currency)` inmutable: el constructor valida que `amount >= 0`, implementa `equals`/`hashCode` por valor. El método `add(Money o)` lanza `IllegalArgumentException` si la moneda es distinta. El `main` demuestra igualdad por valor, suma correcta y suma con monedas distintas.

**Ejercicio 2 — Entity vs Value Object**
Implementa `Persona(int id, String nombre)` como entidad: igualdad por `id`, `nombre` mutable. Implementa `Direccion(String calle, String ciudad, String cp)` como value object: inmutable, igualdad por todos los campos. El `main` crea dos `Persona` con mismo nombre pero distinto id (no son iguales) y dos `Direccion` idénticas (son iguales aunque sean instancias distintas).

**Ejercicio 3 — Repository pattern**
Define la interfaz `ProductoRepository` con los métodos `save`, `findById`, `findAll` y `delete`. Implementa `InMemoryProductoRepository` usando un `HashMap`. `ProductoService` recibe la interfaz por constructor y no conoce la implementación concreta. El `main` usa el servicio sin referenciar `InMemoryProductoRepository` directamente.

**Ejercicio 4 — Domain Event**
Implementa `DomainEvent(String type, String aggregateId, Object payload, java.time.Instant occurredOn)`. Define la interfaz `EventHandler<E>` con `handle(DomainEvent e)`. Implementa `EventPublisher` con `subscribe(String type, EventHandler h)` y `publish(DomainEvent e)`. El `main` publica `PedidoCreado` y `PedidoCancelado`; los handlers imprimen notificaciones.

**Ejercicio 5 — Command/Query segregación**
Define los commands `CrearCuenta(String titular)`, `Depositar(int id, double monto)`, `Retirar(int id, double monto)` y las queries `ObtenerSaldo(int id)`, `ListarMovimientos(int id)`. Implementa `CuentaCommandHandler` y `CuentaQueryHandler` separados; comparten estado solo a través del repositorio. El `main` ejecuta 3 commands y luego 2 queries.

**Ejercicio 6 — Aggregate Root con invariantes**
Implementa `Carrito` (aggregate root) con `List<ItemCarrito(String producto, int cantidad, double precio)>`. Invariantes: máximo 10 items distintos, cantidad entre 1 y 99, total del carrito no puede superar 10 000. Todos los cambios pasan por `agregar(String producto, int cantidad, double precio)`, `eliminar(String producto)` y `vaciar()`. El `main` prueba casos válidos e inválidos.
