# Ejercicios — 37 Spring Security

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Hash de contraseña

Simula el comportamiento de `BCryptPasswordEncoder` usando `MessageDigest` con SHA-256 y un salt aleatorio.

Crea la clase `PasswordEncoder` con dos métodos:
- `encode(String raw)` — genera un salt de 16 bytes aleatorios, calcula `SHA-256(salt + raw)` y devuelve la cadena `salt:hash` en hexadecimal.
- `matches(String raw, String encoded)` — extrae el salt de la cadena codificada, recomputa el hash y compara.

El `main` debe hashear `"secreto123"`, imprimir el resultado codificado, verificar que `"secreto123"` coincide y que `"otraPassword"` no coincide.

## Ejercicio 2 — SecurityContext en ThreadLocal

Implementa un contenedor de autenticación similar al `SecurityContextHolder` de Spring Security.

Crea la clase `Authentication` con `username` y `List<String> roles`. Crea `SecurityContext` con un `ThreadLocal<Authentication>` y métodos estáticos `setAuthentication`, `getAuthentication` y `clear`.

El `main` debe simular: login (set), acceso al recurso (get + imprimir roles), logout (clear) y verificar que `getAuthentication()` devuelve `null` tras el logout.

## Ejercicio 3 — Roles y permisos

Implementa una clase `AccessControl` con el método `checkAccess(Authentication auth, String permission)` que lanza `AccessDeniedException` si el usuario no tiene el permiso requerido.

Reglas de permisos:
- `READ` → todos los roles
- `WRITE` → roles `EDITOR` o `ADMIN`
- `DELETE` → solo rol `ADMIN`

El `main` prueba tres usuarios con distintos roles en distintas operaciones, imprimiendo el resultado (acceso concedido o motivo de denegación).

## Ejercicio 4 — Filter básico

Implementa una cadena de filtrado de requests HTTP simulada.

Crea la interfaz `SecurityFilter` con `boolean doFilter(Request req, Response res)` (devuelve `false` para bloquear). Crea las clases `Request` (con `Map<String,String> headers` y `String path`) y `Response` (con `int status` y `String body`). Implementa `AuthHeaderFilter` que verifica que el header `Authorization` tenga formato `Bearer [token]`.

El `main` prueba un request con token válido y otro sin token.

## Ejercicio 5 — UserDetails simulado

Implementa la interfaz `UserDetails` con los métodos `getUsername()`, `getPassword()`, `getAuthorities()`, `isEnabled()` e `isAccountNonLocked()`. Crea la clase `User` que la implemente.

Crea `InMemoryUserDetailsService` con un `Map<String, UserDetails>` y el método `loadUserByUsername(String username)` que lanza `UsernameNotFoundException` si no existe.

El `main` crea tres usuarios (uno bloqueado), los carga por nombre de usuario e imprime su estado completo.

## Ejercicio 6 — Basic Auth simulado

Implementa `BasicAuthFilter` que decodifica el header `Authorization: Basic base64(usuario:password)` usando `java.util.Base64`, valida las credenciales contra un `UserDetailsService` y, si son válidas, establece el `SecurityContext`. Si son inválidas o falta el header, la respuesta queda con status 401.

El `main` prueba tres casos: credenciales correctas, credenciales incorrectas y request sin header.
