import java.util.List;

public class Ejercicio2 {

    static class Authentication {
        private final String username;
        private final List<String> roles;

        Authentication(String username, List<String> roles) {
            this.username = username;
            this.roles = List.copyOf(roles);
        }

        public String getUsername() { return username; }
        public List<String> getRoles() { return roles; }

        @Override
        public String toString() {
            return "Authentication{username='" + username + "', roles=" + roles + "}";
        }
    }

    static class SecurityContext {
        private static final ThreadLocal<Authentication> CONTEXT = new ThreadLocal<>();

        public static void setAuthentication(Authentication auth) {
            CONTEXT.set(auth);
        }

        public static Authentication getAuthentication() {
            return CONTEXT.get();
        }

        public static void clear() {
            CONTEXT.remove();
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Login ---");
        SecurityContext.setAuthentication(new Authentication("jorge", List.of("USER", "EDITOR")));

        System.out.println("--- Acceso al recurso ---");
        Authentication auth = SecurityContext.getAuthentication();
        System.out.println("Usuario autenticado: " + auth.getUsername());
        System.out.println("Roles: " + auth.getRoles());

        System.out.println("--- Logout ---");
        SecurityContext.clear();

        System.out.println("Autenticación tras logout: " + SecurityContext.getAuthentication());
    }
}
