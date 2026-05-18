import java.util.HashMap;
import java.util.Map;

public class Ejercicio4 {

    public static void main(String[] args) {
        String[] visitas = {
            "/home", "/about", "/home", "/blog", "/home",
            "/about", "/blog", "/home", "/contact"
        };

        Map<String, Integer> contadorVisitas = new HashMap<>();

        for (String url : visitas) {
            // Si la clave no existe (v==null), inicializa en 1; si existe, incrementa
            contadorVisitas.compute(url, (k, v) -> v == null ? 1 : v + 1);
        }

        System.out.println("Contador de visitas por URL:");
        contadorVisitas.forEach((url, count) ->
            System.out.println("  " + url + " -> " + count + " visita(s)")
        );
    }
}
