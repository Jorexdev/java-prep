import java.util.*;

public class Ejercicio1 {

    record TokenResponse(String accessToken, String tokenType, int expiresIn) {}

    static class AuthorizationServer {
        private final Map<String, String> validClients = Map.of("webapp", "secret123");
        private final Map<String, String> pendingCodes = new HashMap<>();  // code → username
        private final Map<String, String> issuedTokens = new HashMap<>();  // token → username

        String authorize(String clientId, String redirectUri) {
            if (!validClients.containsKey(clientId))
                throw new IllegalArgumentException("Cliente desconocido: " + clientId);
            String code = "CODE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            pendingCodes.put(code, "jorge");
            System.out.println("[AuthServer] Usuario autenticado. Código: " + code);
            System.out.println("[AuthServer] Redirigiendo → " + redirectUri + "?code=" + code + "&state=xyz");
            return code;
        }

        TokenResponse exchangeCode(String code, String clientId, String clientSecret) {
            if (!clientSecret.equals(validClients.get(clientId)))
                throw new SecurityException("Credenciales de cliente inválidas");
            String username = pendingCodes.remove(code);
            if (username == null) throw new IllegalArgumentException("Código inválido o ya usado");
            String token = "AT_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
            issuedTokens.put(token, username);
            System.out.println("[AuthServer] Token emitido para '" + username + "'");
            return new TokenResponse(token, "Bearer", 3600);
        }

        String introspect(String token) {
            String user = issuedTokens.get(token);
            if (user == null) throw new SecurityException("Token inválido o expirado");
            return user;
        }
    }

    static class ResourceServer {
        private final AuthorizationServer auth;
        ResourceServer(AuthorizationServer a) { this.auth = a; }

        String getResource(String bearerToken, String path) {
            String user = auth.introspect(bearerToken);
            System.out.println("[Resource] Acceso concedido: " + user + " → " + path);
            return "{\"user\":\"" + user + "\",\"resource\":\"" + path + "\"}";
        }
    }

    public static void main(String[] args) {
        AuthorizationServer authServer = new AuthorizationServer();
        ResourceServer resourceServer  = new ResourceServer(authServer);

        System.out.println("=== PASO 1: Cliente solicita autorización ===");
        String code = authServer.authorize("webapp", "https://app.example.com/callback");

        System.out.println("\n=== PASO 2: Cliente intercambia code por access_token ===");
        TokenResponse tr = authServer.exchangeCode(code, "webapp", "secret123");
        System.out.printf("access_token=%s  type=%s  expires_in=%ds%n",
            tr.accessToken(), tr.tokenType(), tr.expiresIn());

        System.out.println("\n=== PASO 3: Cliente accede al recurso protegido ===");
        System.out.println(resourceServer.getResource(tr.accessToken(), "/api/perfil"));

        System.out.println("\n=== PASO 4: Reutilizar el mismo code (debe fallar) ===");
        try {
            authServer.exchangeCode(code, "webapp", "secret123");
        } catch (IllegalArgumentException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }

        System.out.println("\n=== PASO 5: Token alterado (debe fallar) ===");
        try {
            resourceServer.getResource(tr.accessToken() + "TAMPERED", "/api/datos");
        } catch (SecurityException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }
    }
}
