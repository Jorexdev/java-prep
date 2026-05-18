import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio4 {
    public static void main(String[] args) {
        List<Integer> numeros = List.of(1, 2, 3, 2, 4, 1, 5, 3, 6);
        System.out.println("Lista original: " + numeros);

        // Contamos frecuencias con un Map
        Map<Integer, Integer> frecuencias = new HashMap<>();
        for (int n : numeros) {
            frecuencias.merge(n, 1, Integer::sum);
        }

        // Filtramos los que aparecen exactamente una vez
        List<Integer> unicosList = new ArrayList<>();
        frecuencias.forEach((num, freq) -> {
            if (freq == 1) unicosList.add(num);
        });

        System.out.println("Frecuencias: " + frecuencias);
        System.out.println("Aparecen exactamente una vez: " + unicosList);
    }
}
