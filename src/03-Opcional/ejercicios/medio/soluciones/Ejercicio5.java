import java.util.Optional;

public class Ejercicio5 {

    static class Direccion {
        private final String ciudad;

        Direccion(String ciudad) { this.ciudad = ciudad; }

        String getCiudad() { return ciudad; }
    }

    static class Usuario {
        private final Direccion direccion;

        Usuario(Direccion direccion) { this.direccion = direccion; }

        Direccion getDireccion() { return direccion; }
    }

    public static void main(String[] args) {
        // --- Versión original con null-checks imperativos ---
        Usuario usuarioConCiudad = new Usuario(new Direccion("Madrid"));
        Usuario usuarioSinDir    = new Usuario(null);

        System.out.println("=== Versión con null-checks ===");
        System.out.println("Con ciudad: " + ciudadNullChecks(usuarioConCiudad));
        System.out.println("Sin dir:    " + ciudadNullChecks(usuarioSinDir));
        System.out.println("null:       " + ciudadNullChecks(null));

        System.out.println("\n=== Versión con Optional ===");
        System.out.println("Con ciudad: " + ciudadOptional(usuarioConCiudad));
        System.out.println("Sin dir:    " + ciudadOptional(usuarioSinDir));
        System.out.println("null:       " + ciudadOptional(null));
    }

    // Versión original — null-checks anidados
    static String ciudadNullChecks(Usuario usuario) {
        String ciudad = null;
        if (usuario != null) {
            Direccion dir = usuario.getDireccion();
            if (dir != null) {
                ciudad = dir.getCiudad();
            }
        }
        return ciudad != null ? ciudad : "Ciudad desconocida";
    }

    // Versión con Optional — misma lógica, sin null-checks explícitos
    static String ciudadOptional(Usuario usuario) {
        return Optional.ofNullable(usuario)
                .map(Usuario::getDireccion)
                .map(Direccion::getCiudad)
                .orElse("Ciudad desconocida");
    }
}
