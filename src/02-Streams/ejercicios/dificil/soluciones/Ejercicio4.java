import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio4 {
    record Transaccion(String tipo, double importe) {}

    public static void main(String[] args) {
        List<Transaccion> txs = List.of(
            new Transaccion("COMPRA",   150.0),
            new Transaccion("VENTA",    320.0),
            new Transaccion("COMPRA",    80.0),
            new Transaccion("DEVOLUCION", 50.0),
            new Transaccion("VENTA",    450.0),
            new Transaccion("COMPRA",   200.0)
        );

        DoubleSummaryStatistics stats = txs.stream()
            .collect(Collectors.summarizingDouble(Transaccion::importe));
        System.out.println("Global — count:" + stats.getCount() + " sum:" + stats.getSum()
            + " min:" + stats.getMin() + " max:" + stats.getMax()
            + String.format(" avg:%.2f", stats.getAverage()));

        Map<String, DoubleSummaryStatistics> porTipo = txs.stream()
            .collect(Collectors.groupingBy(Transaccion::tipo,
                     Collectors.summarizingDouble(Transaccion::importe)));
        porTipo.forEach((tipo, s) ->
            System.out.printf("%-12s count:%d sum:%.0f avg:%.2f%n",
                tipo, s.getCount(), s.getSum(), s.getAverage()));
    }
}
