import java.util.*;
import java.util.stream.*;

/**
 * Ejercicio 1 (Medio) — Aggregation pipeline
 * $match → $group (con $sum) → $sort usando Java streams.
 */
public class Ejercicio1 {

    record Pedido(String id, String clienteId, double total, String estado) {}

    public static void main(String[] args) {
        List<Pedido> pedidos = List.of(
            new Pedido("P01", "C01", 250.0, "completado"),
            new Pedido("P02", "C02", 120.0, "pendiente"),
            new Pedido("P03", "C01", 380.0, "completado"),
            new Pedido("P04", "C03", 90.0,  "completado"),
            new Pedido("P05", "C02", 450.0, "completado"),
            new Pedido("P06", "C03", 310.0, "completado"),
            new Pedido("P07", "C01", 175.0, "cancelado"),
            new Pedido("P08", "C02", 200.0, "completado"),
            new Pedido("P09", "C03", 520.0, "completado"),
            new Pedido("P10", "C01", 140.0, "completado")
        );

        // Stage 1: $match → solo completados
        List<Pedido> matched = pedidos.stream()
                .filter(p -> "completado".equals(p.estado()))
                .collect(Collectors.toList());
        System.out.println("$match (completados): " + matched.size() + " pedidos");

        // Stage 2: $group → suma total por clienteId
        Map<String, Double> grouped = matched.stream()
                .collect(Collectors.groupingBy(Pedido::clienteId,
                         Collectors.summingDouble(Pedido::total)));
        System.out.println("$group: " + grouped);

        // Stage 3: $sort descendente + $limit 3
        List<Map.Entry<String, Double>> sorted = grouped.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        System.out.println("\nTop 3 clientes por gasto:");
        for (int i = 0; i < sorted.size(); i++) {
            System.out.printf("  %d. %s → %.2f€%n",
                    i + 1, sorted.get(i).getKey(), sorted.get(i).getValue());
        }
    }
}
