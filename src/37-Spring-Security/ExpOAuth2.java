import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Simula el flujo OAuth2 Authorization Code.
// Estados: redirect → login → authorization code → token exchange → API call con Bearer.
// AuthorizationServer emite códigos y tokens.
// ResourceServer valida el token en cada petición a la API.

// ── Modelos de tokens ─────────────────────────────────────────────────────────

class AuthCode {
    final String code;
    final String clientId;
    final String username;
    final long   expiresAt;

    AuthCode(String code, String clientId, String username, long expiresAt) {
        this.code      = code;
        this.clientId  = clientId;
        this.username  = username;
        this.expiresAt = expiresAt;
    }

    boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
}

class AccessToken {
    final String tokenValue;
    final String username;
    final String scope;
    final long   expiresAt;

    AccessToken(String tokenValue, String username, String scope, long expiresAt) {
        this.tokenValue = tokenValue;
        this.username   = username;
        this.scope      = scope;
        this.expiresAt  = expiresAt;
    }

    boolean isExpired() { return System.currentTimeMillis() > expiresAt; }

    @Override
    public String toString() {
        return "AccessToken{username='" + username + "', scope='" + scope
            + "', expiry=+" + (expiresAt - System.currentTimeMillis()) / 1000 + "s}";
    }
}

// ── Authorization Server ──────────────────────────────────────────────────────

// Equivale a Spring Authorization Server / Keycloak / Auth0
class AuthorizationServer {

    // Clientes registrados: clientId → clientSecret
    private final Map<String, String> clients = Map.of(
        "mi-app-client", "client-secret-xyz"
    );

    // Usuarios registrados (simplificado)
    private final Map<String, String> users = Map.of(
        "jorge", "pass123",
        "ana",   "segura!"
    );

    private final Map<String, AuthCode>   pendingCodes  = new HashMap<>();
    private final Map<String, AccessToken> activeTokens = new HashMap<>();

    // ── Paso 1: cliente redirige al AS para login ─────────────────────────────
    // GET /oauth2/authorize?response_type=code&client_id=X&redirect_uri=Y&scope=Z
    public String buildAuthorizationUrl(String clientId, String redirectUri, String scope) {
        if (!clients.containsKey(clientId)) throw new IllegalArgumentException("clientId desconocido");
        System.out.println("  [AS] URL de autorización generada:");
        String url = "https://auth.example.com/oauth2/authorize"
            + "?response_type=code"
            + "&client_id=" + clientId
            + "&redirect_uri=" + redirectUri
            + "&scope=" + scope;
        System.out.println("       " + url);
        return url;
    }

    // ── Paso 2: usuario se autentica y concede permiso → AS emite código ─────
    // POST /oauth2/authorize (form: username + password + consent)
    public String authorize(String clientId, String username, String password, String redirectUri) {
        System.out.println("  [AS] Usuario '" + username + "' se autentica y da consentimiento...");

        String storedPass = users.get(username);
        if (storedPass == null || !storedPass.equals(password)) {
            throw new SecurityException("Credenciales inválidas para usuario: " + username);
        }

        String code = "code_" + UUID.randomUUID().toString().substring(0, 8);
        // Código válido 60 segundos (en producción: 10 minutos)
        pendingCodes.put(code, new AuthCode(code, clientId, username, System.currentTimeMillis() + 60_000));

        System.out.println("  [AS] Código emitido: " + code);
        System.out.println("  [AS] Redirigiendo a: " + redirectUri + "?code=" + code);
        return code;
    }

    // ── Paso 3: cliente intercambia código por access token ───────────────────
    // POST /oauth2/token  (grant_type=authorization_code, code=X, client credentials)
    public AccessToken exchangeCodeForToken(String code, String clientId, String clientSecret) {
        System.out.println("  [AS] Intercambiando código '" + code + "' por token...");

        // Validar cliente
        String expectedSecret = clients.get(clientId);
        if (expectedSecret == null || !expectedSecret.equals(clientSecret)) {
            throw new SecurityException("Cliente no autenticado: " + clientId);
        }

        // Validar código
        AuthCode authCode = pendingCodes.remove(code);  // un código solo se usa una vez
        if (authCode == null)      throw new SecurityException("Código desconocido: " + code);
        if (authCode.isExpired())  throw new SecurityException("Código expirado: " + code);
        if (!authCode.clientId.equals(clientId)) throw new SecurityException("Código no pertenece al cliente");

        // Emitir token (TTL 1 hora)
        String tokenValue = "Bearer_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        AccessToken token = new AccessToken(tokenValue, authCode.username, "read write",
            System.currentTimeMillis() + 3_600_000);

        activeTokens.put(tokenValue, token);
        System.out.println("  [AS] Token emitido: " + token);
        return token;
    }

    // Usado por el ResourceServer para validar tokens entrantes
    AccessToken introspect(String tokenValue) {
        return activeTokens.get(tokenValue);
    }

    // Registra un token directamente (solo para demos y tests)
    void registerToken(AccessToken token) {
        activeTokens.put(token.tokenValue, token);
    }
}

// ── Resource Server ───────────────────────────────────────────────────────────

// Equivale a un microservicio protegido con Spring Security OAuth2 Resource Server
class ResourceServer {

    private final AuthorizationServer as;

    ResourceServer(AuthorizationServer as) { this.as = as; }

    // Cada request lleva: Authorization: Bearer <token>
    String handleRequest(String httpMethod, String path, String bearerHeader) {
        System.out.println("  [RS] " + httpMethod + " " + path
            + "  Authorization: " + (bearerHeader != null ? bearerHeader.substring(0, 20) + "..." : "MISSING"));

        // ── Validar Bearer token ──────────────────────────────────────────────
        if (bearerHeader == null || !bearerHeader.startsWith("Bearer_")) {
            return "401 Unauthorized — Token ausente o mal formado";
        }

        AccessToken token = as.introspect(bearerHeader);
        if (token == null)     return "401 Unauthorized — Token desconocido";
        if (token.isExpired()) return "401 Unauthorized — Token expirado";

        // Token válido → procesar petición
        return "200 OK — " + httpMethod + " " + path + " ejecutado para user='" + token.username + "'";
    }
}

// ── Cliente OAuth2 ────────────────────────────────────────────────────────────

class OAuth2Client {

    private final String             clientId;
    private final String             clientSecret;
    private final String             redirectUri;
    private final AuthorizationServer as;
    private AccessToken              storedToken;

    OAuth2Client(String clientId, String clientSecret, String redirectUri, AuthorizationServer as) {
        this.clientId     = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri  = redirectUri;
        this.as           = as;
    }

    // Orquesta los 3 pasos del flujo Authorization Code
    void login(String username, String password) {
        System.out.println("  [Client] Iniciando flujo Authorization Code...");
        as.buildAuthorizationUrl(clientId, redirectUri, "read write");
        String code = as.authorize(clientId, username, password, redirectUri);
        storedToken = as.exchangeCodeForToken(code, clientId, clientSecret);
    }

    String callApi(ResourceServer rs, String method, String path) {
        if (storedToken == null) return "Error: no autenticado";
        return rs.handleRequest(method, path, storedToken.tokenValue);
    }

    AccessToken getToken() { return storedToken; }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpOAuth2 {
    public static void main(String[] args) {

        AuthorizationServer as = new AuthorizationServer();
        ResourceServer      rs = new ResourceServer(as);
        OAuth2Client        client = new OAuth2Client(
            "mi-app-client", "client-secret-xyz",
            "https://mi-app.example.com/callback", as);

        System.out.println("=== Simulación OAuth2 Authorization Code Flow ===\n");

        // ─── Flujo completo: login + llamada a API ────────────────────────────
        System.out.println("[ PASO 1-3: Authorization Code Flow ]");
        client.login("jorge", "pass123");

        System.out.println();
        System.out.println("[ PASO 4: llamadas a la API protegida con Bearer token ]");
        System.out.println("  → " + client.callApi(rs, "GET",  "/api/perfil"));
        System.out.println("  → " + client.callApi(rs, "POST", "/api/pedidos"));

        System.out.println();

        // ─── Llamada sin token → 401 ──────────────────────────────────────────
        System.out.println("[ Acceso sin token ]");
        System.out.println("  → " + rs.handleRequest("GET", "/api/perfil", null));

        System.out.println();

        // ─── Token expirado simulado ──────────────────────────────────────────
        System.out.println("[ Token expirado (simulado con TTL=0) ]");
        AccessToken expired = new AccessToken("Bearer_EXPIRED_TOKEN", "jorge", "read",
            System.currentTimeMillis() - 1000);   // ya expiró
        as.registerToken(expired);   // lo registramos para que introspect lo encuentre
        System.out.println("  → " + rs.handleRequest("GET", "/api/datos", "Bearer_EXPIRED_TOKEN"));

        System.out.println();
        System.out.println("[ Resumen del flujo Authorization Code ]");
        System.out.println("  1. Cliente redirige al Authorization Server (AS)");
        System.out.println("  2. Usuario se autentica en el AS y da consentimiento");
        System.out.println("  3. AS redirige al cliente con ?code=XXXXXX (válido ~10 min)");
        System.out.println("  4. Cliente intercambia code + client_secret → access_token");
        System.out.println("  5. Cliente usa access_token en cada API call: Authorization: Bearer <token>");
        System.out.println("  6. Resource Server valida el token via introspection o JWT local");
        System.out.println();
        System.out.println("  Ventaja sobre password flow: las credenciales del usuario NUNCA");
        System.out.println("  llegan al cliente — solo pasan por el AS.");
    }
}
