import java.util.HashMap;
import java.util.Map;

// Simula el flujo de autenticación de Spring Security:
// UserDetailsService → PasswordEncoder → DaoAuthenticationProvider → SecurityContext.
// Las excepciones BadCredentialsException y UsernameNotFoundException coinciden
// con las del paquete org.springframework.security.authentication.

// ── UserDetails ───────────────────────────────────────────────────────────────

// Equivale a org.springframework.security.core.userdetails.UserDetails
class UserDetails {
    private final String   username;
    private final String   hashedPassword;
    private final String   role;
    private final boolean  enabled;

    UserDetails(String username, String hashedPassword, String role, boolean enabled) {
        this.username       = username;
        this.hashedPassword = hashedPassword;
        this.role           = role;
        this.enabled        = enabled;
    }

    public String  getUsername()       { return username; }
    public String  getHashedPassword() { return hashedPassword; }
    public String  getRole()           { return role; }
    public boolean isEnabled()         { return enabled; }

    @Override
    public String toString() {
        return "UserDetails{username='" + username + "', role='" + role + "', enabled=" + enabled + "}";
    }
}

// ── Excepciones de autenticación ──────────────────────────────────────────────

// org.springframework.security.authentication.BadCredentialsException
class BadCredentialsException extends RuntimeException {
    BadCredentialsException(String msg) { super(msg); }
}

// org.springframework.security.core.userdetails.UsernameNotFoundException
class UsernameNotFoundException extends RuntimeException {
    UsernameNotFoundException(String msg) { super(msg); }
}

// ── BCrypt simulado ───────────────────────────────────────────────────────────

// Equivale a org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
class BCryptPasswordEncoder {

    // Hash simplificado (no es BCrypt real): prefijo + longitud + suma de chars
    public String encode(String rawPassword) {
        int checksum = rawPassword.chars().sum() % 1000;
        return "$2a$10$SIMULATED_" + rawPassword.length() + "_" + checksum;
    }

    // En BCrypt real: BCrypt.checkpw(rawPassword, encodedPassword)
    public boolean matches(String rawPassword, String encodedPassword) {
        return encodedPassword.equals(encode(rawPassword));
    }
}

// ── UserDetailsService ────────────────────────────────────────────────────────

// Equivale a org.springframework.security.core.userdetails.UserDetailsService
class InMemoryUserDetailsService {

    private final Map<String, UserDetails> users = new HashMap<>();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Registrar usuario — equivale a UserDetailsManager.createUser(...)
    public UserDetails register(String username, String rawPassword, String role) {
        String hashed = encoder.encode(rawPassword);
        UserDetails ud = new UserDetails(username, hashed, role, true);
        users.put(username.toLowerCase(), ud);
        System.out.println("  [UserSvc] Usuario registrado: " + username + " (hash=" + hashed + ")");
        return ud;
    }

    // Equivale a UserDetailsService#loadUserByUsername
    public UserDetails loadUserByUsername(String username) {
        UserDetails ud = users.get(username.toLowerCase());
        if (ud == null) {
            throw new UsernameNotFoundException("Usuario '" + username + "' no encontrado");
        }
        return ud;
    }

    BCryptPasswordEncoder getEncoder() { return encoder; }
}

// ── Authentication (resultado exitoso) ───────────────────────────────────────

// Equivale a org.springframework.security.authentication.UsernamePasswordAuthenticationToken
class Authentication {
    private final UserDetails principal;
    private final boolean     authenticated;

    Authentication(UserDetails principal) {
        this.principal     = principal;
        this.authenticated = true;
    }

    public UserDetails getPrincipal() { return principal; }
    public boolean isAuthenticated()  { return authenticated; }

    @Override
    public String toString() {
        return "Authentication{principal=" + principal.getUsername()
            + ", role=" + principal.getRole() + ", authenticated=true}";
    }
}

// ── SecurityContext ───────────────────────────────────────────────────────────

// Equivale a SecurityContextHolder (ThreadLocal en Spring real)
class SecurityContext {
    private static Authentication current;

    static void setAuthentication(Authentication auth) { current = auth; }
    static Authentication getAuthentication()          { return current; }
    static void clearContext()                         { current = null; }
}

// ── DaoAuthenticationProvider / AuthenticationManager ────────────────────────

// Equivale a DaoAuthenticationProvider + AuthenticationManager
class AuthenticationManager {

    private final InMemoryUserDetailsService userService;

    AuthenticationManager(InMemoryUserDetailsService userService) {
        this.userService = userService;
    }

    // Equivale a AuthenticationManager#authenticate(UsernamePasswordAuthenticationToken)
    public Authentication authenticate(String username, String rawPassword) {
        // 1. Cargar UserDetails — lanza UsernameNotFoundException si no existe
        UserDetails ud = userService.loadUserByUsername(username);

        // 2. Verificar cuenta habilitada
        if (!ud.isEnabled()) {
            throw new BadCredentialsException("Cuenta deshabilitada: " + username);
        }

        // 3. Verificar contraseña
        if (!userService.getEncoder().matches(rawPassword, ud.getHashedPassword())) {
            throw new BadCredentialsException("Contraseña incorrecta para: " + username);
        }

        // 4. Crear token autenticado y almacenar en SecurityContext
        Authentication auth = new Authentication(ud);
        SecurityContext.setAuthentication(auth);
        return auth;
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpAuthentication {
    public static void main(String[] args) {

        System.out.println("=== Simulación Spring Security Authentication ===\n");

        // ─── Setup ────────────────────────────────────────────────────────────
        InMemoryUserDetailsService userSvc = new InMemoryUserDetailsService();
        AuthenticationManager      authMgr = new AuthenticationManager(userSvc);

        System.out.println("[ Registro de usuarios ]");
        userSvc.register("jorge",  "secret123", "ROLE_ADMIN");
        userSvc.register("ana",    "password!", "ROLE_USER");
        System.out.println();

        // ─── Caso 1: autenticación correcta ──────────────────────────────────
        System.out.println("[ Caso 1: credenciales correctas ]");
        try {
            SecurityContext.clearContext();
            Authentication auth = authMgr.authenticate("jorge", "secret123");
            System.out.println("  → Autenticado: " + auth);
            System.out.println("  → SecurityContext: " + SecurityContext.getAuthentication());
        } catch (Exception ex) {
            System.out.println("  → ERROR (inesperado): " + ex.getMessage());
        }

        System.out.println();

        // ─── Caso 2: contraseña incorrecta ───────────────────────────────────
        System.out.println("[ Caso 2: contraseña incorrecta → BadCredentialsException ]");
        try {
            SecurityContext.clearContext();
            authMgr.authenticate("jorge", "wrongpass");
            System.out.println("  → ERROR: debería haber lanzado excepción");
        } catch (BadCredentialsException ex) {
            System.out.println("  → BadCredentialsException: " + ex.getMessage());
            System.out.println("  → SecurityContext: " + SecurityContext.getAuthentication() + " (vacío)");
        }

        System.out.println();

        // ─── Caso 3: usuario inexistente ─────────────────────────────────────
        System.out.println("[ Caso 3: usuario no existe → UsernameNotFoundException ]");
        try {
            authMgr.authenticate("noexiste", "cualquier");
        } catch (UsernameNotFoundException ex) {
            System.out.println("  → UsernameNotFoundException: " + ex.getMessage());
        }

        System.out.println();

        // ─── Caso 4: usuario autenticado accede a recurso protegido ──────────
        System.out.println("[ Caso 4: recurso protegido — verificar SecurityContext ]");
        SecurityContext.clearContext();
        authMgr.authenticate("ana", "password!");

        Authentication ctx = SecurityContext.getAuthentication();
        if (ctx != null && ctx.isAuthenticated()) {
            System.out.println("  → Acceso permitido para: " + ctx.getPrincipal().getUsername()
                + " (rol=" + ctx.getPrincipal().getRole() + ")");
        }

        System.out.println();
        System.out.println("[ Notas ]");
        System.out.println("  Spring Security NUNCA lanza UsernameNotFoundException al cliente");
        System.out.println("  para no revelar si el usuario existe — la convierte en BadCredentialsException.");
        System.out.println("  BCryptPasswordEncoder usa un salt aleatorio por hash → nunca hay dos hashes iguales.");
        System.out.println("  SecurityContextHolder usa ThreadLocal → seguro en entornos multi-hilo.");
    }
}
