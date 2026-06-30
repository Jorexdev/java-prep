import java.util.*;

// Simula el flujo OAuth2/OIDC con Keycloak usando Java puro.
//
// Conceptos de Keycloak:
//   Realm  — tenant aislado (usuarios, roles, clients, configuración)
//   Client — aplicación registrada en el realm (web app, backend, CLI)
//   Scope  — qué información incluye el token (openid, email, roles, profile)
//
// Flujos OAuth2:
//   Authorization Code — apps web con usuario: redirect → code → token
//   Client Credentials — machine-to-machine sin usuario (service accounts)
//   Device Flow        — CLIs o smart TVs sin navegador: device code → polling
//
// JWT introspection vs local validation:
//   Introspection: llama a Keycloak en cada request → siempre actualizado,
//                  respeta revocación inmediata, pero añade latencia de red
//   Local (JWKS):  valida la firma localmente → stateless, rápido,
//                  pero un token revocado sigue siendo válido hasta su exp

// ── JwtToken ──────────────────────────────────────────────────────────────────

// Modela los claims estándar de un token JWT emitido por Keycloak.
// El iss apunta al realm: https://keycloak.host/realms/mi-realm
class JwtToken {
    private final String iss;          // issuer → URL del realm en Keycloak
    private final String sub;          // subject → userId en Keycloak
    private final String preferredUsername;
    private final List<String> roles;  // resource_access.<client>.roles
    private final List<String> scopes; // scope claim
    private final long exp;            // expiración (epoch ms)
    private final String rawToken;     // valor Base64 simulado

    JwtToken(String realm, String sub, String username,
             List<String> roles, List<String> scopes, long ttlMs) {
        this.iss               = "https://keycloak.example.com/realms/" + realm;
        this.sub               = sub;
        this.preferredUsername = username;
        this.roles             = List.copyOf(roles);
        this.scopes            = List.copyOf(scopes);
        this.exp               = System.currentTimeMillis() + ttlMs;
        // Token simulado: en producción es Base64(header).Base64(payload).firma
        this.rawToken = "eyJ...(" + username + "@" + realm + ")..." + Long.toHexString(exp);
    }

    boolean isExpired()          { return System.currentTimeMillis() > exp; }
    boolean hasRole(String role) { return roles.contains(role); }

    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    // Spring Security prefija los roles con ROLE_ al convertir desde Keycloak
    // → en el claim puede ser "ADMIN" pero hasAuthority espera "ROLE_ADMIN"
    // → hasRole("ADMIN") añade el prefijo automáticamente en Spring Security
    boolean hasAuthority(String authority) {
        if (authority.startsWith("ROLE_")) {
            return roles.contains(authority.substring(5));
        }
        return scopes.contains(authority);
    }

    String iss()               { return iss; }
    String sub()               { return sub; }
    String preferredUsername() { return preferredUsername; }
    List<String> roles()       { return roles; }
    String rawToken()          { return rawToken; }
}

// ── KeycloakRealm ─────────────────────────────────────────────────────────────

// Simula el Authorization Server de Keycloak para un realm concreto.
// En producción: spring.security.oauth2.resourceserver.jwt.issuer-uri
//   → Spring descarga /.well-known/openid-configuration y desde ahí el JWKS.
//   → Las public keys del JWKS se usan para verificar la firma del JWT localmente.
class KeycloakRealm {
    private final String realmName;
    // user → (password, roles)
    private final Map<String, String[]> users = new LinkedHashMap<>();
    // clientId → clientSecret (para Client Credentials flow)
    private final Map<String, String> clients = new LinkedHashMap<>();

    KeycloakRealm(String realmName) { this.realmName = realmName; }

    KeycloakRealm addUser(String username, String password, String... roles) {
        users.put(username, new String[]{password, String.join(",", roles)});
        return this;
    }

    KeycloakRealm addClient(String clientId, String clientSecret) {
        clients.put(clientId, clientSecret);
        return this;
    }

    // ── Authorization Code Flow ──────────────────────────────────────────────
    // 1. Usuario va a /auth → Keycloak pide credenciales
    // 2. Keycloak redirige a redirect_uri con ?code=...
    // 3. Backend intercambia el code por tokens en /token endpoint
    Optional<JwtToken> authorizationCodeFlow(String username, String password,
                                             List<String> scopes) {
        String[] userData = users.get(username);
        if (userData == null || !userData[0].equals(password)) {
            System.out.println("  [" + realmName + "] AuthCode: credenciales inválidas");
            return Optional.empty();
        }
        List<String> roles = Arrays.asList(userData[1].split(","));
        JwtToken token = new JwtToken(realmName, UUID.randomUUID().toString().substring(0, 8),
                username, roles, scopes, 300_000L); // 5 min
        System.out.println("  [" + realmName + "] AuthCode OK → token para " + username);
        return Optional.of(token);
    }

    // ── Client Credentials Flow ──────────────────────────────────────────────
    // Machine-to-machine: el cliente se autentica con su propio clientId+secret.
    // No hay usuario → sub es el serviceAccountId, roles son los del service account.
    Optional<JwtToken> clientCredentialsFlow(String clientId, String clientSecret,
                                             List<String> scopes) {
        String expected = clients.get(clientId);
        if (!clientSecret.equals(expected)) {
            System.out.println("  [" + realmName + "] ClientCreds: client secret inválido");
            return Optional.empty();
        }
        // Service account user → roles mínimos para M2M
        JwtToken token = new JwtToken(realmName, "service-account-" + clientId,
                clientId, List.of("SERVICE"), scopes, 600_000L); // 10 min
        System.out.println("  [" + realmName + "] ClientCreds OK → token para client " + clientId);
        return Optional.of(token);
    }

    // ── Token Introspection ──────────────────────────────────────────────────
    // Llama al /introspect endpoint en cada request → Keycloak confirma si el
    // token sigue activo (respeta logout y revocación).
    // Más lento que validación local pero necesario cuando la revocación inmediata importa.
    Map<String, Object> introspect(JwtToken token) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("active",    !token.isExpired());
        result.put("username",  token.preferredUsername());
        result.put("realm",     realmName);
        result.put("roles",     token.roles());
        System.out.println("  [" + realmName + "/introspect] active=" + !token.isExpired()
                + " user=" + token.preferredUsername());
        return result;
    }

    String realmName() { return realmName; }
}

// ── ResourceServer ────────────────────────────────────────────────────────────

// Simula la configuración del Resource Server en Spring:
//   spring.security.oauth2.resourceserver.jwt.issuer-uri=
//     https://keycloak.example.com/realms/mi-realm
//
// Spring Boot descarga automáticamente:
//   GET /.well-known/openid-configuration  → obtiene jwks_uri
//   GET /protocol/openid-connect/certs     → descarga las claves públicas (JWKS)
// Luego valida cada JWT localmente con esas claves → stateless, sin llamada a Keycloak.
class ResourceServer {
    private final String expectedIssuer;
    private final boolean useIntrospection; // true → llama a Keycloak por request
    private final KeycloakRealm realm;       // solo para introspection

    ResourceServer(String realmName, KeycloakRealm realm, boolean useIntrospection) {
        this.expectedIssuer  = "https://keycloak.example.com/realms/" + realmName;
        this.realm           = realm;
        this.useIntrospection = useIntrospection;
    }

    // Simula el filtro BearerTokenAuthenticationFilter de Spring Security
    boolean validateToken(JwtToken token) {
        // 1. Verificar issuer: el token debe venir del realm correcto
        if (!token.iss().equals(expectedIssuer)) {
            System.out.println("  [ResourceServer] RECHAZADO — issuer inesperado: " + token.iss());
            return false;
        }
        // 2. Verificar expiración
        if (token.isExpired()) {
            System.out.println("  [ResourceServer] RECHAZADO — token expirado");
            return false;
        }
        // 3a. Introspección (llama a Keycloak) o 3b. validación local (JWKS ya descargado)
        if (useIntrospection) {
            Map<String, Object> result = realm.introspect(token);
            return Boolean.TRUE.equals(result.get("active"));
        } else {
            // Validación local: la firma ya fue verificada con la clave pública del JWKS
            System.out.println("  [ResourceServer] JWT local OK (sin llamada a Keycloak)");
            return true;
        }
    }

    // Simula @PreAuthorize("hasRole('ADMIN')") — comprueba rol en el SecurityContext
    String protectedAdminEndpoint(JwtToken token) {
        if (!validateToken(token)) return "HTTP 401 Unauthorized";
        // hasRole('ADMIN') en Spring == hasAuthority('ROLE_ADMIN')
        if (!token.hasAuthority("ROLE_ADMIN")) return "HTTP 403 Forbidden";
        return "HTTP 200 OK — admin area, usuario: " + token.preferredUsername();
    }

    // Simula @PreAuthorize("hasAuthority('read:productos')")
    String protectedReadEndpoint(JwtToken token) {
        if (!validateToken(token)) return "HTTP 401 Unauthorized";
        if (!token.hasAuthority("read:productos")) return "HTTP 403 Forbidden";
        return "HTTP 200 OK — productos: [...]";
    }
}

// ── Main ──────────────────────────────────────────────────────────────────────

public class ExpKeycloak {

    public static void main(String[] args) {

        System.out.println("═".repeat(62));
        System.out.println("  ExpKeycloak — OAuth2/OIDC con Keycloak + Spring Security");
        System.out.println("═".repeat(62));

        // ── Configurar el realm ──────────────────────────────────────────────
        KeycloakRealm realm = new KeycloakRealm("java-prep")
            .addUser("ana", "pass123", "ADMIN", "USER")
            .addUser("bob", "secret",  "USER")
            .addClient("order-service", "client-secret-xyz");

        ResourceServer rsLocal       = new ResourceServer("java-prep", realm, false); // JWKS local
        ResourceServer rsIntrospect  = new ResourceServer("java-prep", realm, true);  // introspection

        // ── Caso 1: Authorization Code Flow (usuario con roles) ──────────────
        System.out.println("\n── Caso 1: Authorization Code Flow (usuario ana/ADMIN) ──────");
        Optional<JwtToken> anaToken = realm.authorizationCodeFlow(
            "ana", "pass123", List.of("openid", "email", "roles"));

        anaToken.ifPresent(token -> {
            System.out.println("  Issuer: " + token.iss());
            System.out.println("  Roles:  " + token.roles());
            System.out.println("  Token (simulado): " + token.rawToken());
            System.out.println("  [/admin] " + rsLocal.protectedAdminEndpoint(token));
            System.out.println("  [/read]  " + rsLocal.protectedReadEndpoint(token));
        });

        // ── Caso 2: Usuario sin rol ADMIN intenta acceder a /admin ───────────
        System.out.println("\n── Caso 2: Authorization Code Flow (usuario bob/USER) ───────");
        Optional<JwtToken> bobToken = realm.authorizationCodeFlow(
            "bob", "secret", List.of("openid", "roles"));

        bobToken.ifPresent(token -> {
            System.out.println("  Roles: " + token.roles());
            System.out.println("  [/admin] " + rsLocal.protectedAdminEndpoint(token));
        });

        // ── Caso 3: Client Credentials Flow (M2M) ───────────────────────────
        System.out.println("\n── Caso 3: Client Credentials Flow (machine-to-machine) ────");
        Optional<JwtToken> serviceToken = realm.clientCredentialsFlow(
            "order-service", "client-secret-xyz", List.of("openid"));

        serviceToken.ifPresent(token -> {
            System.out.println("  Subject: " + token.sub() + " (service account)");
            System.out.println("  Roles:   " + token.roles());
        });

        // ── Caso 4: Token introspection (vs validación local) ────────────────
        System.out.println("\n── Caso 4: Introspection vs validación local con JWKS ───────");
        anaToken.ifPresent(token -> {
            System.out.println("  Con introspection (llama a Keycloak cada request):");
            System.out.println("    " + rsIntrospect.protectedAdminEndpoint(token));
            System.out.println("  Con validación local (JWKS descargado al arrancar):");
            System.out.println("    " + rsLocal.protectedAdminEndpoint(token));
        });

        // ── Caso 5: Credenciales inválidas ───────────────────────────────────
        System.out.println("\n── Caso 5: Credenciales incorrectas ─────────────────────────");
        realm.authorizationCodeFlow("ana", "wrong-pass", List.of("openid"));
        realm.clientCredentialsFlow("order-service", "bad-secret", List.of("openid"));

        // ── Resumen ──────────────────────────────────────────────────────────
        System.out.println("\n── Resumen ───────────────────────────────────────────────────");
        System.out.println("  hasRole('ADMIN')         → comprueba ROLE_ADMIN (prefijo automático)");
        System.out.println("  hasAuthority('ROLE_ADMIN')→ igual, sin prefijo automático");
        System.out.println("  Realm    → tenant aislado de Keycloak (usuarios + clientes + roles)");
        System.out.println("  Client   → app registrada: public (SPA), confidential (backend)");
        System.out.println("  Scope    → qué claims incluye el token (openid obligatorio para OIDC)");
        System.out.println("  JWKS     → claves públicas del realm → validación local sin latencia");
        System.out.println("  Introspect → activo=false para tokens revocados, pero HTTP extra");
        System.out.println("═".repeat(62));
    }
}
