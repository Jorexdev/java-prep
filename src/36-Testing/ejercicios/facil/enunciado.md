# Ejercicios — 36 Testing

## Fácil

**Ejercicio 1 — Assertions básicas**
Construye un mini framework de assertions con una clase `Assert` que tenga métodos estáticos: `assertEquals`, `assertNotNull`, `assertTrue`, `assertFalse` y `assertThrows`. Crea una clase `Calculadora` con `sumar` y `dividir` (lanza `ArithmeticException` si el divisor es 0). Escribe 6 tests que ejerciten la calculadora usando tus assertions. Cada assertion imprime `PASS: nombre` o lanza `AssertionError` con `FAIL: ...`.

**Ejercicio 2 — Ciclo de vida setUp/tearDown**
Implementa una clase `TestRunner` que simule el ciclo de vida `@BeforeEach`/`@AfterEach`. El método `run(String nombre, Runnable test)` debe imprimir `→ setUp`, ejecutar el test, imprimir `→ tearDown`, y capturar cualquier excepción mostrando `FAIL`. Escribe 4 tests: 3 que pasen y 1 que falle. Verifica que `tearDown` siempre se ejecuta aunque el test falle.

**Ejercicio 3 — assertThrows y mensajes de error**
Crea `ServicioUsuarios` con tres métodos que lanzan excepciones específicas: `crear(String nombre)` lanza `IllegalArgumentException` si el nombre está en blanco, `buscar(int id)` lanza `NoSuchElementException` si el id no existe, y `eliminar(int id)` lanza `IllegalStateException` si el usuario tiene pedidos activos. Verifica cada excepción comprobando tanto el tipo como el mensaje exacto.

**Ejercicio 4 — assertAll: múltiples assertions sin cortocircuito**
Implementa `assertAll(Runnable... assertions)` que ejecuta todas las assertions aunque alguna falle, acumulando todos los mensajes y lanzando al final un único error con todos los fallos. Crea un `record Persona(String nombre, int edad, String email)` y valida dos personas: una válida y otra con 3 campos inválidos. Comprueba que `assertAll` muestra los 3 fallos a la vez.

**Ejercicio 5 — Valores límite (boundary testing)**
Crea `Nota(int valor)` que lanza `IllegalArgumentException` para valores fuera de `[0, 10]`. El método `getCalificacion()` devuelve `"Suspenso"` (<5), `"Aprobado"` (5-6), `"Notable"` (7-8) y `"Sobresaliente"` (9-10). Prueba exactamente los valores límite: -1, 0, 4, 5, 6, 7, 8, 9, 10 y 11.

**Ejercicio 6 — Mock manual de interfaz**
Define la interfaz `EmailService` con `send(String to, String subject)`. Implementa `MockEmailService` que registra cada llamada en una lista. Crea `ServicioRegistro` que usa `EmailService` por constructor. Verifica que al registrar un usuario se llama a `send` exactamente una vez con el email correcto.
