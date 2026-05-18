import java.util.Optional;

public class Ejercicio6 {

    static void enviarBienvenida(String email) {
        System.out.println("Email enviado a: " + email);
    }

    public static void main(String[] args) {
        // Optional con valor — ifPresent ejecuta la acción
        Optional<String> conEmail = Optional.of("jorge@ejemplo.com");
        System.out.println("Con email:");
        conEmail.ifPresent(email -> enviarBienvenida(email));

        // Optional vacío — ifPresent no hace nada
        Optional<String> sinEmail = Optional.empty();
        System.out.println("\nSin email (no debe imprimirse nada a continuación):");
        sinEmail.ifPresent(email -> enviarBienvenida(email));
        System.out.println("(fin del bloque sin email)");
    }
}
