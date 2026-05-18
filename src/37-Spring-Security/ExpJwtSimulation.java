import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Simulación del flujo JWT completo con Java puro (sin librerías externas).
 *
 * Demuestra:
 * - Estructura JWT: header.payload.signature (Base64 + HMAC simulado)
 * - JwtUtil: generarToken, verificarToken, extraerClaims
 * - Filtro de Spring Security simulado: intercepta el header Authorization: Bearer
 * - SecurityContext simulado: almacena el usuario autenticado
 * - Casos: token válido, token inválido, token expirado, sin token
 *
 * Las anotaciones Spring aparecen como comentarios.
 *
 * Ejecutar: java -cp target/classes ExpJwtSimulation
 */
public class ExpJwtSimulation {

    // ── Constantes ───────────────────────────────────────────────────────────

    static final String CLAVE_SECRETA = "java-prep-secret-key-2024";
    static final long EXPIRACION_MS   = 3_600_000L;   // 1 hora

    // ── Claims del token ─────────────────────────────────────────────────────

    record Claims(String sujeto, String rol, long expiracion) {
        public boolean estaExpirado() {
            return System.currentTimeMillis() > expiracion;
        }
    }

    // ── JwtUtil ──────────────────────────────────────────────────────────────

    // @Component
    static class JwtUtil {

        private final String claveSecreta;

        JwtUtil(String claveSecreta) {
            this.claveSecreta = claveSecreta;
        }

        /**
         * Genera un token JWT simplificado:
         * Base64(header) . Base64(payload) . hashCode(header+payload+secret)
         */
        public String generarToken(String sujeto, String rol) {
            String header  = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            long exp       = System.currentTimeMillis() + EXPIRACION_MS;
            String payload = "{\"sub\":\"" + sujeto + "\","
                           + "\"rol\":\"" + rol + "\","
                           + "\"exp\":" + exp + "}";

            String headerB64  = Base64.getUrlEncoder().withoutPadding()
                                      .encodeToString(header.getBytes());
            String payloadB64 = Base64.getUrlEncoder().withoutPadding()
                                      .encodeToString(payload.getBytes());

            // Firma simulada: hashCode del contenido + clave secreta
            String firma = Integer.toHexString(
                (headerB64 + "." + payloadB64 + claveSecreta).hashCode()
            );

            return headerB64 + "." + payloadB64 + "." + firma;
        }

        /**
         * Verifica la firma del token y devuelve los claims.
         * Lanza IllegalArgumentException si es inválido.
         */
        public Claims verificarToken(String token) {
            String[] partes = token.split("\\.");
            if (partes.length != 3) {
                throw new IllegalArgumentException("Formato JWT inválido");
            }

            String headerB64  = partes[0];
            String payloadB64 = partes[1];
            String firmaToken = partes[2];

            // Verificar firma
            String firmaEsperada = Integer.toHexString(
                (headerB64 + "." + payloadB64 + claveSecreta).hashCode()
            );
            if (!firmaEsperada.equals(firmaToken)) {
                throw new IllegalArgumentException("Firma JWT inválida — token manipulado");
            }

            // Decodificar payload
            String payloadJson = new String(
                Base64.getUrlDecoder().decode(payloadB64)
            );

            Claims claims = parsearClaims(payloadJson);

            if (claims.estaExpirado()) {
                throw new IllegalArgumentException("Token JWT expirado");
            }

            return claims;
        }

        private Claims parsearClaims(String json) {
            // Parser mínimo para el JSON generado por este mismo código
            String sujeto = extraerValor(json, "sub");
            String rol    = extraerValor(json, "rol");
            long exp      = Long.parseLong(extraerValor(json, "exp"));
            return new Claims(sujeto, rol, exp);
        }

        private String extraerValor(String json, String clave) {
            // Busca "clave":"valor" o "clave":numero
            String patron = "\"" + clave + "\":";
            int inicio = json.indexOf(patron);
            if (inicio < 0) throw new IllegalArgumentException("Claim '" + clave + "' no encontrado");
            int desde = inicio + patron.length();
            boolean esString = json.charAt(desde) == '"';
            if (esString) {
                int fin = json.indexOf('"', desde + 1);
                return json.substring(desde + 1, fin);
            } else {
                int fin = json.indexOf(',', desde);
                if (fin < 0) fin = json.indexOf('}', desde);
                return json.substring(desde, fin).trim();
            }
        }
    }

    // ── SecurityContext simulado ─────────────────────────────────────────────

    static class SecurityContext {
        private static final ThreadLocal<String> usuarioAutenticado = new ThreadLocal<>();

        public static void setUsuario(String usuario) {
            usuarioAutenticado.set(usuario);
        }

        public static String getUsuario() {
            return usuarioAutenticado.get();
        }

        public static void limpiar() {
            usuarioAutenticado.remove();
        }
    }

    // ── Filtro JWT simulado ──────────────────────────────────────────────────

    // Simula OncePerRequestFilter
    static class JwtAuthFilter {

        private final JwtUtil jwtUtil;

        JwtAuthFilter(JwtUtil jwtUtil) {
            this.jwtUtil = jwtUtil;
        }

        /**
         * Simula el doFilterInternal de Spring Security.
         * Extrae el token del header, lo valida y establece el SecurityContext.
         *
         * @return true si la autenticación fue exitosa
         */
        public boolean doFilter(String authorizationHeader) {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                System.out.println("  [Filtro] No hay token Bearer en el header");
                return false;
            }

            String token = authorizationHeader.substring(7);   // quita "Bearer "
            try {
                Claims claims = jwtUtil.verificarToken(token);

                // Simula: SecurityContextHolder.getContext().setAuthentication(auth)
                SecurityContext.setUsuario(claims.sujeto() + " [" + claims.rol() + "]");
                System.out.println("  [Filtro] Token válido. Usuario: "
                    + claims.sujeto() + ", Rol: " + claims.rol());
                return true;

            } catch (IllegalArgumentException e) {
                System.out.println("  [Filtro] Token rechazado: " + e.getMessage());
                return false;
            }
        }
    }

    // ── Endpoint simulado ────────────────────────────────────────────────────

    static String endpointProtegido() {
        String usuario = SecurityContext.getUsuario();
        if (usuario == null) {
            return "HTTP 401 Unauthorized";
        }
        return "HTTP 200 OK — Bienvenido, " + usuario;
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  ExpJwtSimulation — Flujo JWT con Java puro");
        System.out.println("═══════════════════════════════════════════════");

        JwtUtil jwtUtil   = new JwtUtil(CLAVE_SECRETA);
        JwtAuthFilter filtro = new JwtAuthFilter(jwtUtil);

        // ── Caso 1: login exitoso — generar token ────────────────────────────
        System.out.println("\n── Caso 1: Generar token en el login");
        String token = jwtUtil.generarToken("ana@email.com", "ADMIN");
        System.out.println("  Token generado:");
        String[] partes = token.split("\\.");
        System.out.println("    Header  (B64): " + partes[0]);
        System.out.println("    Payload (B64): " + partes[1]);
        System.out.println("    Firma:         " + partes[2]);
        System.out.println("    Token completo: " + token.substring(0, 60) + "...");

        // ── Caso 2: request con token válido ────────────────────────────────
        System.out.println("\n── Caso 2: Request con token válido");
        SecurityContext.limpiar();
        filtro.doFilter("Bearer " + token);
        System.out.println("  Endpoint: " + endpointProtegido());
        SecurityContext.limpiar();

        // ── Caso 3: request sin token ────────────────────────────────────────
        System.out.println("\n── Caso 3: Request sin token");
        filtro.doFilter(null);
        System.out.println("  Endpoint: " + endpointProtegido());

        // ── Caso 4: token con firma manipulada ───────────────────────────────
        System.out.println("\n── Caso 4: Token manipulado (firma alterada)");
        String tokenManipulado = partes[0] + "." + partes[1] + ".firmainvalida";
        filtro.doFilter("Bearer " + tokenManipulado);
        System.out.println("  Endpoint: " + endpointProtegido());

        // ── Caso 5: token expirado (forzado con exp en el pasado) ────────────
        System.out.println("\n── Caso 5: Token con payload exp en el pasado");
        // Construimos manualmente un payload expirado
        String headerB64     = partes[0];
        String payloadExpirado = "{\"sub\":\"hacker@email.com\",\"rol\":\"ADMIN\",\"exp\":1}";
        String payloadB64   = Base64.getUrlEncoder().withoutPadding()
                                    .encodeToString(payloadExpirado.getBytes());
        // Firma correcta pero payload expirado
        String firmaForzada = Integer.toHexString(
            (headerB64 + "." + payloadB64 + CLAVE_SECRETA).hashCode()
        );
        String tokenExpirado = headerB64 + "." + payloadB64 + "." + firmaForzada;
        filtro.doFilter("Bearer " + tokenExpirado);
        System.out.println("  Endpoint: " + endpointProtegido());

        // ── Resumen del flujo ────────────────────────────────────────────────
        System.out.println("\n── Estructura JWT decodificada (Caso 2)");
        Claims claimsValidos = jwtUtil.verificarToken(token);
        System.out.println("  sub (sujeto):   " + claimsValidos.sujeto());
        System.out.println("  rol:            " + claimsValidos.rol());
        System.out.println("  exp (ms):       " + claimsValidos.expiracion());
        System.out.println("  expirado:       " + claimsValidos.estaExpirado());

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("  Fin de la simulación JWT");
        System.out.println("═══════════════════════════════════════════════");
    }
}
