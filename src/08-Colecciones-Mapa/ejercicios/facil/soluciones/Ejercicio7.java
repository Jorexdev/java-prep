import java.util.HashMap;
import java.util.Map;

public class Ejercicio7 {

    public static void main(String[] args) {
        Map<String, Integer> mapa = new HashMap<>();
        mapa.put("manzana", 8);
        mapa.put("pera", 3);
        mapa.put("naranja", 10);
        mapa.put("uva", 2);
        mapa.put("sandía", 6);
        mapa.put("cereza", 1);

        System.out.println("Antes: " + mapa);

        // Eliminar todas las entradas con valor < 5
        mapa.entrySet().removeIf(entrada -> entrada.getValue() < 5);

        System.out.println("Después (solo valor >= 5): " + mapa);
    }
}
