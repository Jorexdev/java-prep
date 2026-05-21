import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio6 {

    interface UserDetails {
        String getUsername();
        String getPassword();
        List<String> getAuthorities();
        boolean isEnabled();
        boolean isAccountNonLocked();
    }

    static class User implements UserDetails {
        private final String username;
        private final String password;
        private final List<String> authorities;

        User(String username, String password, List<String> authorities) {
            this.username = username;
            this.password = password;
            this.authorities = List.copyOf(authorities);
        }

        @Override public String getUsername()          { return username; }
        @Override public String getPassword()          { return password; }
        @Override public List<String> getAuthorities() { return authorities; }
        @Override public boolean isEnabled()           { return true; }
        @Override public boolean isAccountNonLocked()  { return true; }
    }

    static class InMemoryUserDetailsService {
        private final Map<String, UserDetails> store = new HashMap<>();

        void register(UserDetails user) { store.put(user.getUsername(), user); }

        UserDetails find(String username) { return store.get(username); }
    }

    static class Authentication {
        final String username;
        final List<String> roles;

        Authentication(String username, List<String> roles) {
            this.username = username;
            this.roles = roles;
        }
    }

    static class SecurityContext {
        private static final ThreadLocal<Authentication> CTX = new ThreadLocal<>();
        static void set(Authentication a) { CTX.set(a); }
        static Authentication get()       { return CTX.get(); }
        static void clear()               { CTX.remove(); }
    }

    static class Response {
        int status;
        String body;
        Response(int status, String body) { this.status = status; this.body = body; }
    }

    static class BasicAuthFilter {
        private final InMemoryUserDetailsService userService;

        BasicAuthFilter(InMemoryUserDetailsService userService) {
            this.userService = userService;
        }

        Response filter(Map<String, String> headers) {
            String authHeader = headers.get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                return new Response(401, "Unauthorized: Basic Auth requerido");
            }

            String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
            int colon = decoded.indexOf(':');
            if (colon < 0) return new Response(401, "Unauthorized: formato inválido");

            String username = decoded.substring(0, colon);
            String password = decoded.substring(colon + 1);

            UserDetails user = userService.find(username);
            if (user == null || !user.getPassword().equals(password)) {
                return new Response(401, "Unauthorized: credenciales incorrectas");
            }

            SecurityContext.set(new Authentication(user.getUsername(), user.getAuthorities()));
            return new Response(200, "Autenticado como " + username);
        }
    }

    public static void main(String[] args) {
        InMemoryUserDetailsService service = new InMemoryUserDetailsService();
        service.register(new User("jorge", "supersecret", List.of("ADMIN")));

        BasicAuthFilter filter = new BasicAuthFilter(service);

        System.out.println("--- Credenciales correctas ---");
        String correctToken = Base64.getEncoder().encodeToString("jorge:supersecret".getBytes());
        Response r1 = filter.filter(Map.of("Authorization", "Basic " + correctToken));
        System.out.println("Status: " + r1.status + " | " + r1.body);
        System.out.println("SecurityContext: " + (SecurityContext.get() != null
                ? SecurityContext.get().username : "null"));
        SecurityContext.clear();

        System.out.println();
        System.out.println("--- Credenciales incorrectas ---");
        String wrongToken = Base64.getEncoder().encodeToString("jorge:wrongpass".getBytes());
        Response r2 = filter.filter(Map.of("Authorization", "Basic " + wrongToken));
        System.out.println("Status: " + r2.status + " | " + r2.body);

        System.out.println();
        System.out.println("--- Sin header ---");
        Response r3 = filter.filter(Map.of("Content-Type", "application/json"));
        System.out.println("Status: " + r3.status + " | " + r3.body);
    }
}
