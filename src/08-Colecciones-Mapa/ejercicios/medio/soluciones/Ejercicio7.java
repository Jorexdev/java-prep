import java.util.HashMap;
import java.util.Map;

public class Ejercicio7 {

    public static void main(String[] args) {
        Map<String, Integer> puntuaciones = new HashMap<>();
        puntuaciones.put("Ana", 85);
        puntuaciones.put("Luis", 92);
        puntuaciones.put("Marta", 78);
        puntuaciones.put("Jorge", 95);
        puntuaciones.put("Eva", 88);

        Map.Entry<String, Integer> ganador = puntuaciones.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElseThrow(() -> new IllegalStateException("Mapa vacío"));

        System.out.println("Puntuaciones: " + puntuaciones);
        System.out.println("Mayor puntuación: " + ganador.getKey() + " con " + ganador.getValue() + " puntos");
    }
}
