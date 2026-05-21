import java.util.List;

public class Ejercicio3 {

    static class Authentication {
        private final String username;
        private final List<String> roles;

        Authentication(String username, List<String> roles) {
            this.username = username;
            this.roles = List.copyOf(roles);
        }

        public String getUsername() { return username; }
        public List<String> getRoles() { return roles; }
    }

    static class AccessDeniedException extends RuntimeException {
        AccessDeniedException(String msg) { super(msg); }
    }

    static class AccessControl {

        public void checkAccess(Authentication auth, String permission) {
            List<String> roles = auth.getRoles();
            boolean allowed = switch (permission) {
                case "READ"   -> true;
                case "WRITE"  -> roles.contains("EDITOR") || roles.contains("ADMIN");
                case "DELETE" -> roles.contains("ADMIN");
                default -> throw new AccessDeniedException("Permiso desconocido: " + permission);
            };
            if (!allowed) {
                throw new AccessDeniedException(
                        "Usuario '" + auth.getUsername() + "' sin permiso para " + permission);
            }
        }
    }

    public static void main(String[] args) {
        AccessControl ac = new AccessControl();

        Authentication viewer = new Authentication("ana",   List.of("USER"));
        Authentication editor = new Authentication("carlos", List.of("USER", "EDITOR"));
        Authentication admin  = new Authentication("jorge",  List.of("USER", "EDITOR", "ADMIN"));

        String[][] casos = {
            {"ana",    "READ"},
            {"ana",    "WRITE"},
            {"carlos", "WRITE"},
            {"carlos", "DELETE"},
            {"jorge",  "DELETE"},
        };

        java.util.Map<String, Authentication> usuarios = java.util.Map.of(
                "ana", viewer, "carlos", editor, "jorge", admin);

        for (String[] caso : casos) {
            Authentication auth = usuarios.get(caso[0]);
            try {
                ac.checkAccess(auth, caso[1]);
                System.out.println(caso[0] + " -> " + caso[1] + ": CONCEDIDO");
            } catch (AccessDeniedException e) {
                System.out.println(caso[0] + " -> " + caso[1] + ": DENEGADO (" + e.getMessage() + ")");
            }
        }
    }
}
