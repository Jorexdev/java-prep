import java.util.HashMap;
import java.util.Map;

public class Ejercicio1 {

    public static void main(String[] args) {
        Map<String, Integer> edades = new HashMap<>();
        edades.put("Ana", 28);
        edades.put("Luis", 35);
        edades.put("Marta", 22);

        // Clave existente
        int edadAna = edades.getOrDefault("Ana", 0);
        System.out.println("Edad de Ana: " + edadAna); // 28

        // Clave inexistente
        int edadElena = edades.getOrDefault("Elena", 0);
        System.out.println("Edad de Elena (no existe): " + edadElena); // 0

        // get clásico con clave inexistente retorna null
        System.out.println("get('Elena'): " + edades.get("Elena")); // null
    }
}
