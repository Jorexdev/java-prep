import java.util.Optional;

public class Ejercicio1 {

    static class Usuario {
        private final String nombre;
        private final String email; // puede ser null

        Usuario(String nombre, String email) {
            this.nombre = nombre;
            this.email = email;
        }

        String getNombre() { return nombre; }
        String getEmail()  { return email; }
    }

    static String procesarUsuario(Optional<Usuario> optUsuario) {
        return optUsuario
                .map(Usuario::getEmail)              // Optional<String> con el email (o vacío si email es null)
                .filter(e -> e.contains("@"))        // descartar emails sin @
                .orElse("sin-email-valido");         // valor por defecto
    }

    public static void main(String[] args) {
        // Caso 1: usuario sin email (null)
        Optional<Usuario> sinEmail = Optional.of(new Usuario("Ana", null));
        System.out.println("Sin email:      " + procesarUsuario(sinEmail));

        // Caso 2: usuario con email válido
        Optional<Usuario> emailValido = Optional.of(new Usuario("Jorge", "jorge@ejemplo.com"));
        System.out.println("Email válido:   " + procesarUsuario(emailValido));

        // Caso 3: usuario con email malformado (sin @)
        Optional<Usuario> emailMalo = Optional.of(new Usuario("Luis", "luisSINarroba"));
        System.out.println("Email malo:     " + procesarUsuario(emailMalo));

        // Caso 4: Optional vacío
        System.out.println("Optional vacío: " + procesarUsuario(Optional.empty()));
    }
}
