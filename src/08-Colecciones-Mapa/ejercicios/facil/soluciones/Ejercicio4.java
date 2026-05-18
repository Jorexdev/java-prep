import java.util.HashMap;
import java.util.Map;

public class Ejercicio4 {

    public static void main(String[] args) {
        Map<String, Integer> mapa = new HashMap<>();

        // Primera inserción: clave no existe -> se inserta con 0
        mapa.putIfAbsent("contador", 0);
        System.out.println("Tras 1er putIfAbsent('contador', 0): " + mapa.get("contador")); // 0

        // Segunda inserción: clave ya existe -> el valor NO se sobreescribe
        mapa.putIfAbsent("contador", 99);
        System.out.println("Tras 2do putIfAbsent('contador', 99): " + mapa.get("contador")); // 0

        // Comparación: put sí sobreescribe
        mapa.put("contador", 99);
        System.out.println("Tras put('contador', 99): " + mapa.get("contador")); // 99
    }
}
