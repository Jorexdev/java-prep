import java.util.TreeMap;
import java.util.Map;

public class Ejercicio5 {

    public static void main(String[] args) {
        // TreeMap ordena automáticamente las claves por orden natural (alfabético)
        Map<String, Integer> ciudades = new TreeMap<>();
        ciudades.put("Zaragoza", 700000);
        ciudades.put("Madrid", 3300000);
        ciudades.put("Barcelona", 1600000);
        ciudades.put("Bilbao", 350000);
        ciudades.put("Valencia", 800000);

        System.out.println("Ciudades ordenadas alfabéticamente:");
        ciudades.forEach((ciudad, poblacion) ->
            System.out.println("  " + ciudad + ": " + poblacion + " hab.")
        );
    }
}
