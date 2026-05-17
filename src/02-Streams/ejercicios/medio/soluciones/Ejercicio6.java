import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio6 {

    public static void main(String[] args) {

        // Ejercicio: resumen estadístico de una lista de decimales (media, suma, min, max, count)
        List<Double> numbers = List.of(12.5, 7.3, 18.9, 4.0, 15.6, 9.1, 21.4);

        // summarizingDouble devuelve DoubleSummaryStatistics con count, sum, min, avg, max
        String summary = numbers
                .stream()
                .collect(Collectors.summarizingDouble(Double::doubleValue))
                .toString();

        System.out.println(summary);
    }
}
