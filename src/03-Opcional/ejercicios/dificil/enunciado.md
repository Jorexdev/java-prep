# Optional — Ejercicios Difícil

Ejercicios avanzados con Optional: reemplazar null-checks complejos, combinar Optional con Stream, patrones de validación acumulativa y caché.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Cuatro niveles de null-check
Reescribe este encadenamiento usando Optional:
```java
String iso = null;
if (pedido != null && pedido.getCliente() != null 
    && pedido.getCliente().getDireccion() != null 
    && pedido.getCliente().getDireccion().getPais() != null) {
    iso = pedido.getCliente().getDireccion().getPais().getCodigoISO();
}
```
Modela las clases Pedido, Cliente, Direccion, Pais con Optional en cada getter.

## Ejercicio 2 — Parseo seguro de JSON simulado
Dado un `Map<String, Object>` que representa un JSON parseado, extrae campos anidados de forma segura: `data.usuario.nombre` y `data.usuario.edad`. Usa Optional en cada paso para manejar claves ausentes sin NullPointerException.

## Ejercicio 3 — Validación con early-return usando Optional
Implementa `validarPedido(Pedido p)` que retorne `Optional<String>` con el primer error encontrado (usuario nulo, producto sin stock, precio negativo) o `Optional.empty()` si todo es correcto. Usa Optional en lugar de if-return encadenados.

## Ejercicio 4 — Optional con Stream.findFirst
Dada una lista de candidatos (nombre, puntuación), usa `stream().filter(...).findFirst()` para obtener el primer candidato con puntuación >= 80. Luego encadena `map(Candidato::getNombre).orElse("Sin candidato apto")`.

## Ejercicio 5 — firstNonEmpty genérico
Implementa el método `Optional<T> firstNonEmpty(List<Optional<T>> opciones)` que devuelva el primer Optional con valor. Pruébalo con una lista de 4 opciones donde solo la tercera tiene valor.

## Ejercicio 6 — Caché con Optional
Implementa un caché simple: `Map<String, String>` para almacenar resultados. El método `obtener(String clave)` debe: buscar en caché (retorna `Optional`), si vacío calcular el valor (operación costosa), guardarlo en caché y devolverlo. Usa `computeIfAbsent` y envuélvelo en Optional.
