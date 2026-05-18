import java.util.Optional;

public class Ejercicio8 {

    public static void main(String[] args) {
        // Caso 1: Optional vacío → lanza excepción
        Optional<String> vacio = Optional.empty();
        try {
            String valor = vacio.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            System.out.println("Valor: " + valor); // no se alcanza
        } catch (IllegalArgumentException e) {
            System.out.println("Excepción capturada: " + e.getMessage());
        }

        // Caso 2: Optional con valor → funciona normalmente, no lanza excepción
        Optional<String> conValor = Optional.of("Jorge");
        try {
            String valor = conValor.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            System.out.println("Valor obtenido: " + valor);
        } catch (IllegalArgumentException e) {
            System.out.println("Excepción capturada: " + e.getMessage()); // no se alcanza
        }
    }
}
