import java.util.HashMap;
import java.util.Map;

public class Ejercicio4 {

    public static void main(String[] args) {
        String texto = "hola mundo hermoso hoy hace mucho sol azul lago mesa";
        String[] palabras = texto.split(" ");

        // Map<primeraLetra, Map<ultimaLetra, conteo>>
        Map<Character, Map<Character, Integer>> tabla = new HashMap<>();

        for (String palabra : palabras) {
            if (palabra.isEmpty()) continue;
            char primera = palabra.charAt(0);
            char ultima  = palabra.charAt(palabra.length() - 1);

            tabla.computeIfAbsent(primera, k -> new HashMap<>())
                 .merge(ultima, 1, Integer::sum);
        }

        System.out.println("Tabla bidireccional (primera letra -> última letra -> conteo):");
        tabla.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> {
                System.out.println("  '" + e.getKey() + "' ->");
                e.getValue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(inner ->
                        System.out.println("      '" + inner.getKey() + "': " + inner.getValue())
                    );
            });
    }
}
