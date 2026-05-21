# Ejercicios — 37 Spring Security

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — JWT completo

Implementa JWT desde cero sin librerías externas.

Crea `JwtUtil` con:
- `generate(String username, List<String> roles, long expiryMs)` → `header.payload.signature` en Base64url  
  - Header: `{"alg":"HS256","typ":"JWT"}`  
  - Payload: `{"sub":"...","roles":[...],"exp":...,"iat":...}`  
  - Signature: `HMAC-SHA256(header.payload, secret)` con `javax.crypto.Mac`
- `verify(String token)` → devuelve un objeto `Claims` con username y roles; lanza excepción si la firma es inválida o el token está expirado.

El `main` genera un token, lo imprime completo, verifica uno válido y verifica uno con la firma alterada (debe lanzar excepción).

## Ejercicio 2 — Refresh token

Implementa un servicio de tokens con soporte para renovación.

Crea `TokenPair` con `accessToken` y `refreshToken`. Implementa `TokenService` con:
- `login(String username)` → genera access token (5 min de vida) + refresh token (7 días)
- `refresh(String refreshToken)` → valida el refresh token, genera un nuevo access token
- `logout(String refreshToken)` → invalida el refresh token (blacklist con `Set`)

El `main` hace login, extrae el username del access token, simula expiración del access, hace refresh, luego hace logout e intenta otro refresh (debe fallar).

## Ejercicio 3 — @PreAuthorize simulado

Implementa autorización declarativa con reflexión.

Crea la anotación `@Secured(String[] roles)`. Crea `SecureProxy<T>` que envuelve un objeto: al invocar un método anotado con `@Secured`, comprueba que el usuario en `SecurityContext` tiene alguno de los roles requeridos; si no, lanza `AccessDeniedException`.

Crea `AdminService` con métodos anotados con distintos roles. El `main` prueba invocaciones con un usuario admin y con un usuario básico, mostrando cuáles se conceden y cuáles se bloquean.

## Ejercicio 4 — CSRF token

Implementa protección CSRF básica.

Crea `CsrfTokenRepository` que genera un token UUID por sesión y lo almacena en un `Map<String, String>`. Implementa `CsrfFilter` que:
- Deja pasar siempre los métodos GET
- Rechaza POST/PUT/DELETE que no lleven el header `X-CSRF-TOKEN` con el valor correcto

El `main` simula: GET (pasa), POST sin token (bloqueado), GET para obtener el token, POST con token correcto (pasa).

## Ejercicio 5 — Filter chain completo

Implementa una cadena de filtros de seguridad encadenados.

La cadena es: `CorsFilter` → `JwtAuthFilter` → `AuthorizationFilter` → `LoggingFilter`. Cada filtro puede llamar al siguiente o cortar la cadena. `SecurityFilterChain` ejecuta la cadena completa.

El `main` prueba cuatro requests:
1. Sin token → corta en `JwtAuthFilter` (401)
2. Token válido pero sin permisos → corta en `AuthorizationFilter` (403)
3. Request válido con permisos → pasa toda la cadena (200)
4. OPTIONS pre-flight → pasa `CorsFilter`, corta antes de JWT (204)
