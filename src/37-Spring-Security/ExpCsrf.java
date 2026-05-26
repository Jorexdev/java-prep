import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Simula la protección CSRF de Spring Security.
// CsrfTokenRepository genera y almacena tokens por sesión.
// CsrfFilter valida el token en peticiones de estado (POST, PUT, DELETE).
// Se demuestra por qué las APIs JWT stateless no necesitan CSRF.

// ── Token CSRF ────────────────────────────────────────────────────────────────

class CsrfToken {
    private final String sessionId;
    private final String token;

    CsrfToken(String sessionId, String token) {
        this.sessionId = sessionId;
        this.token     = token;
    }

    public String getSessionId() { return sessionId; }
    public String getToken()     { return token; }

    @Override
    public String toString() { return "CsrfToken{session=" + sessionId + ", token=" + token + "}"; }
}

// ── CsrfTokenRepository ───────────────────────────────────────────────────────

// Equivale a HttpSessionCsrfTokenRepository de Spring Security
class CsrfTokenRepository {

    private final Map<String, CsrfToken> store = new HashMap<>();

    // Genera y almacena un token CSRF para la sesión
    // Equivale a CsrfTokenRepository#generateToken(HttpServletRequest)
    public CsrfToken generate(String sessionId) {
        String tokenValue = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        CsrfToken token = new CsrfToken(sessionId, tokenValue);
        store.put(sessionId, token);
        System.out.println("  [CSRF Repo] Token generado para session=" + sessionId + ": " + tokenValue);
        return token;
    }

    // Recupera el token almacenado para una sesión
    // Equivale a CsrfTokenRepository#loadToken(HttpServletRequest)
    public CsrfToken load(String sessionId) {
        return store.get(sessionId);
    }

    // Limpia el token (equivale a logout o rotación)
    public void clear(String sessionId) {
        store.remove(sessionId);
        System.out.println("  [CSRF Repo] Token eliminado para session=" + sessionId);
    }
}

// ── Petición HTTP simulada ────────────────────────────────────────────────────

class CsrfRequest {
    final String method;
    final String path;
    final String sessionId;
    final String csrfTokenHeader;  // X-CSRF-TOKEN header
    final String csrfTokenForm;    // _csrf param del form
    final boolean hasJwtBearer;   // simula API stateless con JWT

    CsrfRequest(String method, String path, String sessionId,
                String csrfTokenHeader, String csrfTokenForm, boolean hasJwtBearer) {
        this.method          = method;
        this.path            = path;
        this.sessionId       = sessionId;
        this.csrfTokenHeader = csrfTokenHeader;
        this.csrfTokenForm   = csrfTokenForm;
        this.hasJwtBearer    = hasJwtBearer;
    }

    String resolveToken() {
        if (csrfTokenHeader != null) return csrfTokenHeader;
        if (csrfTokenForm   != null) return csrfTokenForm;
        return null;
    }

    @Override
    public String toString() {
        String token = resolveToken();
        return method + " " + path + " session=" + sessionId
            + (token != null ? " _csrf=" + token : " [sin token CSRF]")
            + (hasJwtBearer ? " [JWT Bearer]" : "");
    }
}

// ── CsrfFilter ────────────────────────────────────────────────────────────────

// Equivale a org.springframework.security.web.csrf.CsrfFilter
// Se ubica en la cadena de filtros de Spring Security (FilterChain)
class CsrfFilter {

    private final CsrfTokenRepository repo;

    CsrfFilter(CsrfTokenRepository repo) { this.repo = repo; }

    // Métodos seguros: solo lectura, no modifican estado del servidor
    private static final java.util.Set<String> SAFE_METHODS = java.util.Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    // Retorna null si la petición está permitida; mensaje de error si debe bloquearse
    public String validate(CsrfRequest req) {
        // Las APIs JWT stateless deshabilitan CSRF via SecurityFilterChain:
        // http.csrf(csrf -> csrf.disable())
        if (req.hasJwtBearer) {
            System.out.println("  [CSRF] API stateless con JWT → CSRF deshabilitado (no aplica)");
            return null;
        }

        // Métodos seguros no necesitan validación (no modifican estado)
        if (SAFE_METHODS.contains(req.method.toUpperCase())) {
            System.out.println("  [CSRF] " + req.method + " es safe → sin validación");
            return null;
        }

        // Peticiones de estado (POST/PUT/DELETE) → validar token
        CsrfToken stored = repo.load(req.sessionId);
        if (stored == null) {
            return "403 Forbidden — sesión sin token CSRF (sesión inválida o expirada)";
        }

        String submitted = req.resolveToken();
        if (submitted == null) {
            return "403 Forbidden — falta el token CSRF en la petición";
        }

        if (!stored.getToken().equals(submitted)) {
            return "403 Forbidden — token CSRF inválido (submitted=" + submitted
                + ", expected=" + stored.getToken() + ")";
        }

        System.out.println("  [CSRF] Token válido ✓ → petición procesada");
        return null;  // null = sin error = petición permitida
    }
}

// ── Controlador con flujo GET → POST ─────────────────────────────────────────

class FormController {

    private final CsrfTokenRepository repo;
    private final CsrfFilter          filter;

    FormController(CsrfTokenRepository repo, CsrfFilter filter) {
        this.repo   = repo;
        this.filter = filter;
    }

    // GET /formulario → Spring inyecta el token en el modelo y el form lo incluye
    // <input type="hidden" name="_csrf" th:value="${_csrf.token}"/>
    String mostrarFormulario(String sessionId) {
        CsrfToken token = repo.generate(sessionId);
        System.out.println("  [Controller] GET /formulario → form renderizado con _csrf=" + token.getToken());
        return token.getToken();   // el form lo incluirá en el submit
    }

    String procesarFormulario(CsrfRequest req) {
        System.out.println("  [Controller] " + req);
        String error = filter.validate(req);
        if (error != null) { System.out.println("  → " + error); return error; }
        System.out.println("  → 200 OK — formulario procesado");
        return "200 OK";
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpCsrf {
    public static void main(String[] args) {

        CsrfTokenRepository repo    = new CsrfTokenRepository();
        CsrfFilter          filter  = new CsrfFilter(repo);
        FormController      ctrl    = new FormController(repo, filter);

        System.out.println("=== Simulación CSRF Protection ===\n");

        // ─── Caso 1: flujo legítimo GET → POST con token correcto ─────────────
        System.out.println("[ Caso 1: flujo legítimo — GET obtiene token, POST lo envía ]");
        String validToken = ctrl.mostrarFormulario("session-abc-123");
        ctrl.procesarFormulario(new CsrfRequest(
            "POST", "/formulario", "session-abc-123", validToken, null, false));

        System.out.println();

        // ─── Caso 2: POST sin token (ataque CSRF típico) ──────────────────────
        System.out.println("[ Caso 2: POST sin token CSRF → 403 (ataque CSRF) ]");
        repo.generate("session-victima");   // sesión con token válido almacenado
        ctrl.procesarFormulario(new CsrfRequest(
            "POST", "/formulario", "session-victima", null, null, false));

        System.out.println();

        // ─── Caso 3: POST con token incorrecto ────────────────────────────────
        System.out.println("[ Caso 3: POST con token incorrecto → 403 ]");
        repo.generate("session-xyz");
        ctrl.procesarFormulario(new CsrfRequest(
            "POST", "/perfil", "session-xyz", "TOKEN_FALSO_123", null, false));

        System.out.println();

        // ─── Caso 4: GET no requiere validación ──────────────────────────────
        System.out.println("[ Caso 4: GET no requiere token CSRF ]");
        ctrl.procesarFormulario(new CsrfRequest(
            "GET", "/perfil", "session-abc-123", null, null, false));

        System.out.println();

        // ─── Caso 5: API REST con JWT → CSRF no aplica ───────────────────────
        System.out.println("[ Caso 5: API stateless con JWT Bearer — CSRF deshabilitado ]");
        System.out.println("  SecurityFilterChain: http.csrf(csrf -> csrf.disable())");
        ctrl.procesarFormulario(new CsrfRequest(
            "POST", "/api/usuarios", "NO-SESSION", null, null, true));
        System.out.println("  → 200 OK — API JWT no necesita CSRF");

        System.out.println();
        System.out.println("[ ¿Por qué JWT no necesita CSRF? ]");
        System.out.println("  CSRF funciona porque el navegador adjunta automáticamente las cookies de sesión.");
        System.out.println("  Con JWT, el token se envía en el header Authorization: Bearer <token>.");
        System.out.println("  El navegador NO envía headers personalizados en peticiones cross-origin");
        System.out.println("  sin CORS permisivo, así que un sitio malicioso no puede usurpar el token.");
        System.out.println("  → Si la auth es por header (JWT), CSRF es innecesario.");
        System.out.println("  → Si la auth es por cookie de sesión, CSRF es obligatorio.");
    }
}
