import java.util.HashMap;
import java.util.Map;

public class Ejercicio2 {

    public static void main(String[] args) {
        Map<String, Integer> mapa1 = new HashMap<>();
        mapa1.put("java", 3);
        mapa1.put("python", 1);
        mapa1.put("rust", 2);

        Map<String, Integer> mapa2 = new HashMap<>();
        mapa2.put("java", 5);
        mapa2.put("go", 4);
        mapa2.put("python", 3);

        System.out.println("Mapa 1: " + mapa1);
        System.out.println("Mapa 2: " + mapa2);

        // Combinar mapa2 en mapa1 sumando valores de claves comunes
        Map<String, Integer> combinado = new HashMap<>(mapa1);
        mapa2.forEach((clave, valor) ->
            combinado.merge(clave, valor, Integer::sum)
        );

        System.out.println("Combinado (suma de frecuencias): " + combinado);
    }
}
