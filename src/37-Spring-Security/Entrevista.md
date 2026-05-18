<div align="center">
  <a href="#"><img src="../../assets/modules/banner-37-spring-security-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Qué es `SecurityFilterChain` y cómo funciona el flujo de autenticación?**

`SecurityFilterChain` es el bean central de Spring Security: define una cadena ordenada de filtros Servlet que interceptan cada request HTTP. El flujo para autenticación con JWT: (1) el request entra al `FilterChainProxy`; (2) pasa por el filtro custom `JwtAuthFilter` que extrae el token del header `Authorization: Bearer`; (3) el filtro valida la firma con la clave secreta y extrae los claims; (4) si es válido, construye un objeto `Authentication` y lo coloca en `SecurityContextHolder`; (5) el filtro de autorización comprueba si el usuario tiene los roles requeridos para el endpoint; (6) el request llega al controller. Si algún filtro rechaza, devuelve 401/403 directamente.

---

**¿Cómo implementas autenticación con JWT en Spring Security?**

Los pasos son: (1) crear un `JwtAuthFilter extends OncePerRequestFilter` que extrae el token del header, lo valida y llama a `SecurityContextHolder.getContext().setAuthentication(auth)`; (2) registrar el filtro antes del `UsernamePasswordAuthenticationFilter` con `http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`; (3) en `SecurityFilterChain`, configurar `sessionManagement` como `STATELESS` y deshabilitar CSRF; (4) crear el endpoint `POST /auth/login` que valida credenciales con `AuthenticationManager.authenticate()` y devuelve el token generado. La clave secreta para firmar suele guardarse en propiedades como `jwt.secret`.

---

**¿Cuál es la diferencia entre autenticación y autorización?**

**Autenticación** responde a "¿quién eres?" — verifica la identidad del usuario comprobando credenciales (contraseña, token, certificado). En Spring Security lo gestiona el `AuthenticationManager` y el resultado es un objeto `Authentication` en el `SecurityContext`. **Autorización** responde a "¿qué puedes hacer?" — una vez autenticado, determina si el usuario tiene permisos para acceder a un recurso o ejecutar una acción. En Spring Security se configura con `authorizeHttpRequests()` para rutas y con `@PreAuthorize` para métodos. La autenticación siempre precede a la autorización.

---

**¿Por qué se usa BCrypt para almacenar contraseñas? ¿Qué es el factor de trabajo (cost factor)?**

BCrypt es un algoritmo de hashing diseñado específicamente para contraseñas con tres propiedades clave: (1) **lento por diseño** — al contrario que SHA-256 que es rápido, BCrypt hace que el brute-force sea costoso; (2) **incluye salt automático** — cada hash es diferente aunque la contraseña sea la misma, impidiendo ataques de rainbow table; (3) **adaptable** — el cost factor (factor de trabajo) controla el número de iteraciones como `2^n`. Con cost=10 son 1024 iteraciones; con cost=12 son 4096. A medida que el hardware mejora, puedes aumentar el factor. El valor recomendado en producción es 12. `BCryptPasswordEncoder` en Spring almacena el salt dentro del propio hash, por lo que no es necesario guardarlo por separado.

---

**¿Qué hace la protección CSRF y cuándo puedes desactivarla?**

CSRF (Cross-Site Request Forgery) es un ataque donde una web maliciosa hace que el navegador del usuario autenticado envíe requests no deseados a tu app. Spring Security lo previene exigiendo un token CSRF en cada petición que modifica estado (POST, PUT, DELETE). El token se incluye como campo oculto en formularios o header `X-CSRF-TOKEN`. Puedes desactivarlo con `csrf.disable()` cuando: (1) tu API es stateless con JWT — no hay cookies de sesión que un atacante pueda explotar; (2) la app no usa autenticación basada en cookies. En APIs REST puras consumidas por SPAs con JWT, desactivar CSRF es la práctica estándar.

---

**¿Cuál es la diferencia entre sesiones stateful y stateless en Spring Security?**

Con **sesiones stateful**, Spring Security crea un `HttpSession` en el servidor al autenticar y guarda el `SecurityContext` en ella. El cliente recibe una cookie `JSESSIONID`. En cada request, el servidor busca la sesión y recupera el contexto. Escalar horizontalmente requiere sticky sessions o un session store compartido (Redis). Con autenticación **stateless** (JWT), no hay sesión en el servidor: el contexto de seguridad se reconstruye en cada request a partir del token. Escala trivialmente sin coordinación entre instancias. Contrapartida: revocar un token JWT antes de su expiración requiere una lista negra o tokens de corta duración.

---

**¿Cómo proteges un endpoint para que solo accedan usuarios con rol ADMIN?**

Hay dos enfoques complementarios. A nivel de ruta en `SecurityFilterChain`:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

A nivel de método con `@PreAuthorize` (requiere `@EnableMethodSecurity` en la config):

```java
// @PreAuthorize("hasRole('ADMIN')")
public void eliminarUsuario(Long id) { ... }
```

`@PreAuthorize` es más flexible porque permite expresiones SpEL: `hasRole('ADMIN') or #userId == authentication.principal.id`. Si el usuario no tiene el rol, Spring Security lanza `AccessDeniedException` que se traduce en HTTP 403 Forbidden.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
