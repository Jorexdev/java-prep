import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

// Simula @PreAuthorize y control de acceso basado en roles.
// AuthorizationInterceptor evalúa expresiones SpEL simplificadas
// (hasRole, hasAnyRole, #userId == authentication.name) antes de cada método.

// ── Anotación @PreAuthorize ───────────────────────────────────────────────────

// Equivale a org.springframework.security.access.prepost.PreAuthorize
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface PreAuthorize {
    String value();   // expresión SpEL, ej: "hasRole('ADMIN')"
}

// ── Principal autenticado ─────────────────────────────────────────────────────

// Equivale a org.springframework.security.core.Authentication
class Principal {
    private final String     username;
    private final Set<String> roles;

    Principal(String username, Set<String> roles) {
        this.username = username;
        this.roles    = roles;
    }

    public String      getUsername() { return username; }
    public Set<String> getRoles()    { return roles; }

    @Override
    public String toString() { return username + roles; }
}

// ── SecurityContext simulado (per-invocación) ─────────────────────────────────

class AuthSecurityContext {
    private static final ThreadLocal<Principal> holder = new ThreadLocal<>();

    static void set(Principal p)     { holder.set(p); }
    static Principal get()           { return holder.get(); }
    static void clear()              { holder.remove(); }
}

// ── Evaluador de expresiones SpEL (simplificado) ─────────────────────────────

// Equivale al SpELExpressionHandler de Spring Security
class SpelEvaluator {

    // Evalúa la expresión contra el principal del contexto de seguridad
    static boolean evaluate(String expression, String paramName, Object paramValue) {
        Principal principal = AuthSecurityContext.get();
        if (principal == null) return false;  // sin autenticar

        String spel = expression.trim();

        // hasRole('ROLE_X') o hasRole('X')
        if (spel.startsWith("hasRole(")) {
            String role = extractSingleArg(spel);
            role = normalizeRole(role);
            return principal.getRoles().contains(role);
        }

        // hasAnyRole('X', 'Y', ...)
        if (spel.startsWith("hasAnyRole(")) {
            String inner = spel.substring("hasAnyRole(".length(), spel.length() - 1);
            for (String r : inner.split(",")) {
                if (principal.getRoles().contains(normalizeRole(r.trim()))) return true;
            }
            return false;
        }

        // #userId == authentication.name  (param binding)
        if (spel.contains("== authentication.name")) {
            // El valor del parámetro debe coincidir con el username del principal
            String expectedUsername = String.valueOf(paramValue);
            return principal.getUsername().equals(expectedUsername);
        }

        // isAuthenticated()
        if (spel.equals("isAuthenticated()")) return true;

        return false;
    }

    private static String extractSingleArg(String spel) {
        int start = spel.indexOf('\'');
        int end   = spel.lastIndexOf('\'');
        return (start >= 0 && end > start) ? spel.substring(start + 1, end) : "";
    }

    private static String normalizeRole(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}

// ── Respuesta ─────────────────────────────────────────────────────────────────

class AuthzResponse {
    final int    status;
    final String body;

    AuthzResponse(int status, String body) { this.status = status; this.body = body; }

    @Override
    public String toString() { return status + " " + body; }
}

// ── AuthorizationInterceptor ──────────────────────────────────────────────────

// Equivale al MethodSecurityInterceptor de Spring Security (via @EnableMethodSecurity)
class AuthorizationInterceptor {

    AuthzResponse invoke(String methodName, String preAuthorize,
                         String paramName, Object paramValue,
                         java.util.function.Supplier<String> handler) {
        boolean allowed = SpelEvaluator.evaluate(preAuthorize, paramName, paramValue);

        if (!allowed) {
            Principal p = AuthSecurityContext.get();
            System.out.println("  [Authz ] DENEGADO — " + methodName
                + " @PreAuthorize(\"" + preAuthorize + "\")");
            System.out.println("           Principal: " + (p != null ? p : "anonymous"));
            return new AuthzResponse(403, "{ \"error\": \"Forbidden\" }");
        }

        System.out.println("  [Authz ] PERMITIDO — " + methodName);
        return new AuthzResponse(200, handler.get());
    }
}

// ── Controller con métodos protegidos ─────────────────────────────────────────

// @RestController
class AdminController {

    private final AuthorizationInterceptor interceptor = new AuthorizationInterceptor();

    // @PreAuthorize("hasRole('ADMIN')")
    AuthzResponse listarUsuarios() {
        return interceptor.invoke("listarUsuarios", "hasRole('ADMIN')",
            null, null, () -> "{ \"usuarios\": [\"jorge\",\"ana\"] }");
    }

    // @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    AuthzResponse verPerfil(String userId) {
        return interceptor.invoke("verPerfil", "hasAnyRole('ADMIN', 'USER')",
            null, null, () -> "{ \"perfil\": \"" + userId + "\" }");
    }

    // @PreAuthorize("#userId == authentication.name")
    // Solo el propio usuario puede editar su perfil (o ADMIN via otra regla)
    AuthzResponse editarPerfil(String userId) {
        return interceptor.invoke("editarPerfil", "#userId == authentication.name",
            "userId", userId, () -> "{ \"updated\": \"" + userId + "\" }");
    }

    // @PreAuthorize("hasRole('ADMIN')")
    AuthzResponse eliminarUsuario(String target) {
        return interceptor.invoke("eliminarUsuario", "hasRole('ADMIN')",
            null, null, () -> "{ \"deleted\": \"" + target + "\" }");
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpAuthorization {
    public static void main(String[] args) {

        AdminController ctrl = new AdminController();

        System.out.println("=== Simulación @PreAuthorize + RBAC ===\n");

        // ─── Como ADMIN ───────────────────────────────────────────────────────
        System.out.println("[ ADMIN: todas las operaciones permitidas ]");
        AuthSecurityContext.set(new Principal("jorge", Set.of("ROLE_ADMIN", "ROLE_USER")));

        System.out.println("  GET /admin/usuarios   → " + ctrl.listarUsuarios());
        System.out.println("  GET /perfil/jorge     → " + ctrl.verPerfil("jorge"));
        System.out.println("  PUT /perfil/jorge     → " + ctrl.editarPerfil("jorge"));
        System.out.println("  DELETE /usuarios/ana  → " + ctrl.eliminarUsuario("ana"));
        AuthSecurityContext.clear();

        System.out.println();

        // ─── Como USER normal ─────────────────────────────────────────────────
        System.out.println("[ USER: acceso limitado ]");
        AuthSecurityContext.set(new Principal("ana", Set.of("ROLE_USER")));

        System.out.println("  GET /admin/usuarios   → " + ctrl.listarUsuarios());   // 403
        System.out.println("  GET /perfil/ana       → " + ctrl.verPerfil("ana"));   // OK
        System.out.println("  PUT /perfil/ana       → " + ctrl.editarPerfil("ana")); // OK (propio)
        System.out.println("  PUT /perfil/jorge     → " + ctrl.editarPerfil("jorge")); // 403 (ajeno)
        System.out.println("  DELETE /usuarios/x    → " + ctrl.eliminarUsuario("x")); // 403
        AuthSecurityContext.clear();

        System.out.println();

        // ─── Sin autenticar (anónimo) ────────────────────────────────────────
        System.out.println("[ ANONYMOUS: todo denegado ]");
        // No se llama a AuthSecurityContext.set → principal = null

        System.out.println("  GET /admin/usuarios   → " + ctrl.listarUsuarios());
        System.out.println("  GET /perfil/ana       → " + ctrl.verPerfil("ana"));

        System.out.println();
        System.out.println("[ Notas SpEL de Spring Security ]");
        System.out.println("  hasRole('X')                   → true si tiene ROLE_X");
        System.out.println("  hasAnyRole('X','Y')            → true si tiene ROLE_X o ROLE_Y");
        System.out.println("  isAuthenticated()              → true si no es anónimo");
        System.out.println("  #param == authentication.name  → compara param con username actual");
        System.out.println("  @bean.method(#param)           → delega en un bean Spring");
    }
}
