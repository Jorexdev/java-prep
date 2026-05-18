# Excepciones — Ejercicios Medio

Ejercicios intermedios: jerarquías de excepciones, exception chaining, try-with-resources múltiple, excepciones con campos extra.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Jerarquía de excepciones
Crea: `AppException extends RuntimeException` (base), `ValidationException extends AppException`, `NotFoundException extends AppException`. Crea métodos que lancen cada tipo. En main, captura la base `AppException` para manejar todas, y también captura específica para diferenciar.

## Ejercicio 2 — parseEdad con excepción custom
Implementa `int parseEdad(String s)`: si s es null lanza `IllegalArgumentException`, si no es número lanza `ValidationException` con mensaje "No es un número: [s]", si la edad es negativa lanza `ValidationException` con "Edad negativa". Prueba los 3 casos.

## Ejercicio 3 — Exception chaining
Método `cargarDatos()` lanza `RuntimeException("Error BD")` simulando un error de base de datos. Crea `DatabaseException extends RuntimeException`. En otro método, captura la RuntimeException y relanza como `new DatabaseException("Error cargando datos", e)` (con la causa). En main, imprime e.getCause() para verificar la cadena.

## Ejercicio 4 — try-with-resources con dos recursos
Crea `Recurso` (AutoCloseable) que imprime al abrir y cerrar. Abre dos recursos en un try-with-resources. Verifica que se cierran en orden inverso al de apertura (el segundo abierto se cierra primero).

## Ejercicio 5 — Excepción con campos extra
Crea `ErrorConCodigo extends RuntimeException` con campo `int codigo`. Constructor acepta código y mensaje. Lanza esta excepción con código 404 desde un método. En catch, imprime tanto el getMessage() como el getCodigo().

## Ejercicio 6 — Wrap de checked a unchecked
Crea método `leerArchivo(String ruta)` que lanza `IOException` (checked). Crea `leerArchivoUnchecked(String ruta)` que llama al anterior y convierte la IOException a RuntimeException. El caller no necesita try-catch.

## Ejercicio 7 — Catch genérico vs específico
Muestra con código que `catch(Exception e)` captura cualquier excepción, pero `catch(NullPointerException e)` solo captura NPE. Crea un ejemplo donde importa el orden de los catch (más específico primero).
