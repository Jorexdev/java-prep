<div align="center">
  <a href="#"><img src="../../assets/modules/banner-37-spring-security-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Spring Security** protege aplicaciones mediante una **cadena de filtros Servlet** (`SecurityFilterChain`) que intercepta cada request HTTP antes de que llegue al controller.

**Diferencia fundamental:**

| Concepto | Pregunta | Ejemplo |
|---|---|---|
| **Autenticación** | ¿Quién eres? | Login con usuario/contraseña |
| **Autorización** | ¿Qué puedes hacer? | Solo ADMIN puede acceder a `/admin` |

**Flujo de autenticación (form login / JWT):**

```
HTTP Request
    ↓
DelegatingFilterProxy          ← puente Spring MVC ↔ Servlet
    ↓
FilterChainProxy               ← gestiona la SecurityFilterChain
    ↓
UsernamePasswordAuthenticationFilter  (o JwtAuthFilter custom)
    ↓
AuthenticationManager          ← orquesta la autenticación
    ↓
UserDetailsService.loadUserByUsername(username)
    ↓
UserDetails (username, password, roles)
    ↓
PasswordEncoder.matches()      ← valida el hash
    ↓
SecurityContextHolder          ← almacena Authentication para el request
    ↓
Controller
```

**Configuración básica con `SecurityFilterChain`:**

```java
// @Configuration
// @EnableWebSecurity
public class SecurityConfig {

    // @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())            // desactivar para APIs REST
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**UserDetailsService + UserDetails:**

```java
// @Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // @Autowired
    private UsuarioRepository repo;

    // @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario u = repo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email));

        return User.builder()
            .username(u.getEmail())
            .password(u.getPasswordHash())   // ya hasheado en BD
            .roles(u.getRol())
            .build();
    }
}
```

**PasswordEncoder — BCrypt:**

```java
// @Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);   // cost factor: 2^12 iteraciones
}

// Al registrar usuario:
String hash = encoder.encode("miContraseña");   // genera salt + hash
// Al verificar login:
boolean valido = encoder.matches("miContraseña", hash);  // siempre constante-time
```

BCrypt incluye un **salt aleatorio** en cada hash y el **cost factor** (factor de trabajo) controla las iteraciones: a mayor valor, más lento el brute-force. El valor por defecto es 10; en producción se usa 12.

**JWT — estructura y flujo:**

```
Header.Payload.Signature

eyJhbGciOiJIUzI1NiJ9  .  eyJzdWIiOiJhbmFAZW1haWwuY29tIiwicm9sZSI6IkFETUlOIn0  .  xY7...

Header: { "alg": "HS256" }
Payload: { "sub": "ana@email.com", "role": "ADMIN", "exp": 1716000000 }
Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secretKey)
```

Flujo JWT:
```
POST /auth/login  →  valida credenciales  →  devuelve JWT
GET  /api/datos   →  Authorization: Bearer <token>
                  →  JwtAuthFilter valida firma + expiración
                  →  establece SecurityContext  →  Controller
```

**Autorización por método:**

```java
// @EnableMethodSecurity  ← habilitar en config

// @PreAuthorize("hasRole('ADMIN')")
public void eliminarUsuario(Long id) { ... }

// @Secured("ROLE_ADMIN")
public List<Usuario> listarTodos() { ... }

// @PreAuthorize("hasRole('USER') and #id == authentication.principal.id")
public Usuario verPerfil(Long id) { ... }
```

**Sesiones stateful vs stateless:**

| | Stateful (sesiones) | Stateless (JWT) |
|---|---|---|
| Estado | Servidor almacena sesión | Todo en el token |
| Escalabilidad | Sticky sessions o session store compartido | Horizontal sin coordinación |
| Revocación | Inmediata (borra sesión) | Difícil (hasta que expira) |
| Uso típico | Apps MVC tradicionales | APIs REST, microservicios |

**OAuth2 Resource Server (configuración mínima):**

```java
// application.yml
// spring:
//   security:
//     oauth2:
//       resourceserver:
//         jwt:
//           issuer-uri: https://auth.ejemplo.com

http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

- **Integración transparente con Spring MVC**: Spring Security se engancha al ciclo de vida de Servlet sin modificar los controllers.
- **Extensible**: cada parte del flujo (UserDetailsService, PasswordEncoder, AuthenticationProvider) es un bean reemplazable.
- **Soporte OAuth2/OIDC nativo**: Resource Server y Authorization Server disponibles out-of-the-box.
- **Defensa en profundidad**: CSRF, headers de seguridad (X-Content-Type-Options, X-Frame-Options), clickjacking protection — todo activado por defecto.

Ver [ExpJwtSimulation.java](ExpJwtSimulation.java) para el flujo JWT completo con Java puro y [ExpSecurityChain.java](ExpSecurityChain.java) para la simulación de la cadena de filtros.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
