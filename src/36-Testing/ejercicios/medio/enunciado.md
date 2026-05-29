# Ejercicios — 36 Testing

## Medio

**Ejercicio 1 — Mock con when/thenReturn y verify**
Implementa una clase genérica `MockBuilder<T>` que permita configurar respuestas con `when(String metodo).thenReturn(Object valor)` y verificar invocaciones con `verify(String metodo, int veces)`. Define la interfaz `ProductoRepository` con `findById(int)`, `save(Producto)` y `delete(int)`. Crea un servicio que use el repositorio y escribe tests que configuren mocks, ejecuten el servicio y verifiquen las invocaciones.

**Ejercicio 2 — Spy: objeto real con override selectivo**
Crea una clase `Spy<T>` que envuelva un objeto real y permita sobreescribir métodos individuales registrando sus llamadas. Implementa `ServicioNotificaciones` con métodos reales `enviarSms` y `enviarEmail`. Usa el Spy para interceptar solo `enviarEmail` (sin ejecutarlo realmente) mientras `enviarSms` funciona con la implementación real. Demuestra la diferencia en `main`.

**Ejercicio 3 — ArgumentCaptor**
Crea `ArgumentCaptor<T>` que capture el argumento de la última invocación a un método mockeado. Define `AuditService` con `log(String evento, String detalle, LocalDateTime timestamp)`. Escribe un test que borre un usuario y verifique que el servicio de auditoría recibe el evento correcto, los detalles esperados y un timestamp no nulo. Usa el captor para inspeccionar cada argumento individualmente.

**Ejercicio 4 — Ciclo TDD Red-Green-Refactor**
Implementa `CarritoCompras` siguiendo el ciclo TDD en tres pasos visibles. Paso Red: escribe el test `agregarProducto_aumentaItems` antes de que la clase exista — debe fallar. Paso Green: implementa el mínimo código para que pase. Paso Refactor: añade `calcularTotal()` y el test correspondiente. Repite el ciclo para `aplicarDescuento(double pct)`. El `main` ejecuta los tests en orden mostrando la evolución del ciclo.

**Ejercicio 5 — Tests parametrizados**
Implementa `ParametrizedTestRunner` que reciba una lista de casos `Object[]` (entradas + valor esperado) y ejecute el mismo test para cada uno, mostrando `PASS`/`FAIL` por caso. Implementa la función `esNumeroPrimo(int n)` y pruébala con los casos: 2→true, 3→true, 4→false, 13→true, 15→false, 97→true, 100→false.

**Ejercicio 6 — Custom AssertJ assertion para dominio**
Implementa `PedidoAssert` extendiendo `AbstractAssert<PedidoAssert, Pedido>` de forma simulada (sin la librería real). `Pedido` tiene `id`, `estado` (`PENDIENTE`, `CONFIRMADO`, `CANCELADO`), `total` y `List<String> items`. `PedidoAssert` debe ofrecer los métodos encadenables: `estaConfirmado()`, `tieneTotal(double esperado)`, `contieneItem(String item)` y `tieneNumeroDeItems(int n)`. Cada aserción fallida lanza `AssertionError` con un mensaje descriptivo. El `main` ejecuta una batería de 6 aserciones, mostrando `PASS`/`FAIL` por cada una, incluidas dos que deben fallar intencionadamente.
