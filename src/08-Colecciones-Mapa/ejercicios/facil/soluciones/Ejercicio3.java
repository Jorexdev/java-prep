import java.util.HashMap;
import java.util.Map;

public class Ejercicio3 {

    public static void main(String[] args) {
        String[] palabras = {"hola", "mundo", "hola", "java", "mundo", "hola"};

        Map<String, Integer> frecuencia = new HashMap<>();
        for (String palabra : palabras) {
            frecuencia.put(palabra, frecuencia.getOrDefault(palabra, 0) + 1);
        }

        System.out.println("Frecuencia de palabras:");
        frecuencia.forEach((palabra, count) ->
            System.out.println("  '" + palabra + "' aparece " + count + " vez/veces")
        );
    }
}
