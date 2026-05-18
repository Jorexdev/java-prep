# Optional — Ejercicios Medio

Ejercicios intermedios para practicar `flatMap`, encadenamiento de operaciones, `ifPresentOrElse`, `or()`, `stream()` y patrones comunes de null-safety.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Pipeline map → filter → orElse
Crea un método `procesarUsuario(Optional<Usuario>)` que encadene: `map(Usuario::getEmail)` → `filter(e -> e.contains("@"))` → `orElse("sin-email-valido")`. La clase Usuario tiene nombre y email (puede ser null).

## Ejercicio 2 — flatMap para evitar Optional anidado
Tienes una clase `Pedido` con método `Optional<Direccion> getDireccion()` y `Direccion` con método `Optional<String> getCalle()`. Sin flatMap obtendrías `Optional<Optional<String>>`. Usa `flatMap` para obtener directamente `Optional<String>`.

## Ejercicio 3 — ifPresentOrElse
Dado un `Optional<String>` con el nombre de un usuario conectado, usa `ifPresentOrElse(nombre -> System.out.println("Bienvenido, " + nombre), () -> System.out.println("Sesión no iniciada"))`.

## Ejercicio 4 — findById con orElseThrow
Implementa un repositorio mock de usuarios (Map<Integer, String>) con método `findById(int id)` que retorne `Optional<String>`. Desde el main, usa `orElseThrow` para obtener el usuario o lanzar `NoSuchElementException` con mensaje descriptivo.

## Ejercicio 5 — Reescribir null-checks anidados
Reescribe este código con Optional:
```java
String ciudad = null;
if (usuario != null) {
    Direccion dir = usuario.getDireccion();
    if (dir != null) {
        ciudad = dir.getCiudad();
    }
}
String resultado = ciudad != null ? ciudad : "Ciudad desconocida";
```

## Ejercicio 6 — or() para fallback entre Optionals
Tienes dos fuentes de configuración: `configuracionLocal()` y `configuracionRemota()`, ambas retornan `Optional<String>`. Usa `opt1.or(() -> configuracionRemota())` para intentar primero la local y caer en la remota si está vacía.

## Ejercicio 7 — Optional.stream() integrado con Stream
Dada una lista de `Optional<String>`, usa `lista.stream().flatMap(Optional::stream)` para obtener solo los valores presentes en una sola lista. Evita el uso de `filter(Optional::isPresent).map(Optional::get)`.

## Ejercicio 8 — Repositorio con Optional
Implementa una interfaz `Repositorio<T>` con método `Optional<T> findByNombre(String nombre)`. Crea `RepositorioProductos` con lista interna y usa el método encadenando `.map(Producto::getPrecio).orElse(0.0)`.
