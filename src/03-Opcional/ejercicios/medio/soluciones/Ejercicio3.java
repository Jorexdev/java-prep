import java.util.Optional;

public class Ejercicio3 {

    public static void main(String[] args) {
        // Caso 1: usuario conectado
        Optional<String> conectado = Optional.of("Jorex");
        conectado.ifPresentOrElse(
                nombre -> System.out.println("Bienvenido, " + nombre),
                ()     -> System.out.println("Sesión no iniciada")
        );

        // Caso 2: nadie conectado
        Optional<String> sinSesion = Optional.empty();
        sinSesion.ifPresentOrElse(
                nombre -> System.out.println("Bienvenido, " + nombre),
                ()     -> System.out.println("Sesión no iniciada")
        );
    }
}
