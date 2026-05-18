# Optional — Ejercicios Fácil

Ejercicios básicos para practicar `Optional.of`, `ofNullable`, `empty`, `isPresent`, `get`, `orElse`, `orElseGet`, `orElseThrow`, `map`, `filter`, `ifPresent`.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Crear y verificar presencia
Crea un `Optional<String>` a partir de un valor no nulo, otro a partir de null con `ofNullable`, y uno vacío con `empty`. Imprime si cada uno está presente usando `isPresent()` e `isEmpty()`.

## Ejercicio 2 — orElse con valor por defecto
Dado un método `buscarNombre(int id)` que retorna `Optional<String>` (vacío para ids desconocidos), usa `orElse("Desconocido")` para obtener siempre un String. Prueba con id conocido e id desconocido.

## Ejercicio 3 — orElseGet con Supplier
Repite el ejercicio anterior pero usando `orElseGet(() -> generarNombreDefault())` en lugar de `orElse`. El Supplier debe imprimir un mensaje cuando se invoca para demostrar que es lazy (solo se llama si el Optional está vacío).

## Ejercicio 4 — map para transformar
Dado un `Optional<String>` con un nombre, usa `map(String::length)` para obtener un `Optional<Integer>` con la longitud. Si el Optional original está vacío, el resultado también debe estar vacío.

## Ejercicio 5 — filter para descartar
Dado un `Optional<Integer>` con una edad, usa `filter(e -> e >= 18)` para obtener el Optional solo si la persona es mayor de edad. Prueba con una edad válida y una no válida.

## Ejercicio 6 — ifPresent para ejecutar acción
Dado un `Optional<String>` con un email, usa `ifPresent(email -> enviarBienvenida(email))` para enviar un email solo si existe. Si el Optional está vacío, no debe ocurrir nada.

## Ejercicio 7 — orElse vs orElseGet (diferencia de evaluación)
Demuestra que `orElse` siempre evalúa su argumento aunque el Optional tenga valor, mientras que `orElseGet` es lazy. Crea un método `valorCostoso()` que imprima un mensaje al evaluarse y úsalo con ambos métodos sobre un Optional con valor.

## Ejercicio 8 — orElseThrow
Dado un `Optional<String>` que puede estar vacío, usa `orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"))` para obtener el valor o lanzar excepción. Captura la excepción en el main y muestra el mensaje.
