# Ejercicios — 34 Spring MVC

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — ResponseEntity simulado**
Implementa una clase `ApiResponse<T>` con `status` (int) y `body` (T). Escribe un método `getProductos()` que devuelva un `ApiResponse` con status 200 y una lista de 3 productos.

**Ejercicio 2 — @RequestParam simulado**
Dada una lista de `Empleado(nombre, depto)`, implementa un método `buscarPorDepto(String depto)` que filtre y devuelva los empleados del departamento indicado.

**Ejercicio 3 — @PathVariable simulado**
Usa un `Map<Integer, String>` como almacén de usuarios (id → nombre). Implementa `getUsuario(int id)` que devuelva el nombre o lance `RuntimeException("404 Not Found: " + id)` si no existe.

**Ejercicio 4 — @RequestBody simulado**
Implementa una clase `CrearPedidoRequest(String producto, int cantidad)` y un método `crearPedido(CrearPedidoRequest req)` que valide los campos y devuelva la confirmación o lance `IllegalArgumentException`.

**Ejercicio 5 — Validación de campos**
Implementa una clase `ValidationResult` con `boolean valid` y `List<String> errores`. Escribe un método `validar(RegistroForm f)` que compruebe que el email contiene "@" y que la password tiene al menos 8 caracteres.

**Ejercicio 6 — @ControllerAdvice global simulado**
Define una interfaz `Handler` con `Object handle(String request)`. Implementa `GlobalExceptionHandler` que envuelva cualquier handler y capture excepciones, devolviendo `"Error 400: " + e.getMessage()` en lugar de propagar el error.
