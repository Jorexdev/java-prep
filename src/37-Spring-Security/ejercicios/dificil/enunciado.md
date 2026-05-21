# Ejercicios — 37 Spring Security

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — OAuth2 Authorization Code flow

Simula el flujo OAuth2 Authorization Code completo sin librerías externas.

Implementa `AuthServer` con:
1. `authorize(String clientId, String redirectUri, String scope)` → genera un `code` de un solo uso y devuelve la URL de redirección con el code como query param.
2. `token(String code, String clientId, String clientSecret)` → valida el code, lo invalida y devuelve un objeto `TokenResponse` con `access_token`, `token_type` y `expires_in`.
3. `validateToken(String accessToken)` → devuelve el username asociado o lanza excepción si inválido.

El `main` simula los tres pasos imprimiendo cada mensaje de protocolo. Intenta reutilizar el mismo code (debe fallar).

## Ejercicio 2 — Multi-tenant security

Implementa aislamiento de seguridad entre tenants.

Cada tenant tiene su propio `UserDetailsService` y conjunto de roles. Crea `TenantSecurityContext` que extiende el `SecurityContext` añadiendo `tenantId`. Implementa `TenantFilter` que extrae el tenant del header `X-Tenant-ID` y carga el contexto del tenant correcto.

Configura dos tenants (`"acme"` y `"globex"`) con usuarios distintos. El `main` demuestra que un usuario de `"acme"` puede acceder a sus recursos pero que al intentar acceder con contexto de `"globex"` el acceso es denegado.

## Ejercicio 3 — Brute force protection

Implementa protección contra ataques de fuerza bruta.

Crea `LoginAttemptService` que registra intentos fallidos por IP con timestamp usando `ConcurrentHashMap<String, Deque<Long>>`. Bloquea una IP tras 5 fallos en una ventana de 10 minutos. Implementa `AuthenticationService.login(String ip, String username, String password)` que consulta el bloqueo antes de autenticar.

El `main` simula: 6 intentos fallidos desde la misma IP (el 6.º debe indicar bloqueo), un intento exitoso desde otra IP, y luego avanza el reloj simulado más de 10 minutos para demostrar que la IP bloqueada queda libre.

## Ejercicio 4 — Rate limiter Token Bucket

Implementa un rate limiter con el algoritmo Token Bucket.

Crea `TokenBucket(int capacity, int refillPerSecond)` con el método `tryConsume()` que devuelve `boolean`. Implementa `RateLimitFilter` que aplica un bucket distinto por `userId` (usando `Map<String, TokenBucket>`).

El `main` simula: un usuario con límite de 5 req/s hace 8 peticiones rápidas (las primeras 5 son aceptadas, las 3 siguientes rechazadas), luego espera 1 segundo (refill), y hace 5 peticiones más (todas aceptadas).
