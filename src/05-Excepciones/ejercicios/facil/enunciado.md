# Excepciones — Ejercicios Fácil

Ejercicios básicos: captura de excepciones comunes, finally, custom exceptions, multi-catch, try-with-resources.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Capturar NumberFormatException
Escribe un método `parsearEntero(String s)` que intente convertir el String a int. Si falla, captura la `NumberFormatException`, imprime "Valor inválido: [s]" y retorna -1. Prueba con "42", "abc" y null.

## Ejercicio 2 — Finally garantizado
Crea una clase `Conexion` con métodos `abrir()`, `operar()` y `cerrar()`. En el main, usa try-finally para garantizar que `cerrar()` siempre se llame aunque `operar()` lance una excepción.

## Ejercicio 3 — Excepción custom checked
Crea `EdadInvalidaException extends Exception` con un constructor que acepte el valor de edad. Crea `validarEdad(int edad)` que lanza esta excepción si edad < 0 o > 150. Llama al método en el main y maneja la excepción.

## Ejercicio 4 — Multi-catch
Escribe un método que pueda lanzar `NumberFormatException` o `ArrayIndexOutOfBoundsException`. En el main, usa un solo bloque `catch (NumberFormatException | ArrayIndexOutOfBoundsException e)` para manejar ambas.

## Ejercicio 5 — try-with-resources
Crea clase `Recurso implements AutoCloseable` con método `usar()` que imprime algo y `close()` que imprime "Recurso cerrado". Usa try-with-resources y verifica que close() se llama automáticamente.

## Ejercicio 6 — Re-lanzar excepción
Crea un método que capture una `Exception`, la loggee (System.err) y la relance con `throw e`. En el main, captura la excepción relanzada y muestra su mensaje.

## Ejercicio 7 — Excepción unchecked custom
Crea `ProductoNoEncontradoException extends RuntimeException` con constructor que acepta el id del producto. Crea `buscarProducto(int id)` que la lanza para ids <= 0. Maneja la excepción en main.
