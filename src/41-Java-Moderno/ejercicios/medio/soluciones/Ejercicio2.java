import java.util.List;

public class Ejercicio2 {

    record Pedido(String id, String estado, int prioridad, double importe) {}

    static String clasificar(Pedido p) {
        return switch (p.estado()) {
            case "PENDIENTE" when p.prioridad() >= 8 && p.importe() > 1000 -> "URGENTE_ALTO_VALOR";
            case "PENDIENTE" when p.prioridad() >= 8                       -> "URGENTE";
            case "PENDIENTE"                                                -> "NORMAL";
            case "PROCESANDO" when p.importe() > 500                       -> "PROCESANDO_ALTO_VALOR";
            case "PROCESANDO"                                               -> "EN_CURSO";
            case "ENVIADO"                                                  -> "COMPLETADO";
            case "CANCELADO"                                                -> "BAJA";
            default                                                         -> "DESCONOCIDO";
        };
    }

    public static void main(String[] args) {
        List<Pedido> pedidos = List.of(
            new Pedido("P001", "PENDIENTE",  9, 1500.0),
            new Pedido("P002", "PENDIENTE",  9,  200.0),
            new Pedido("P003", "PENDIENTE",  3,  300.0),
            new Pedido("P004", "PROCESANDO", 5,  800.0),
            new Pedido("P005", "PROCESANDO", 2,  100.0),
            new Pedido("P006", "ENVIADO",    6,  500.0),
            new Pedido("P007", "CANCELADO",  1,   50.0),
            new Pedido("P008", "DEVUELTO",   4,  250.0)
        );

        System.out.println("=== Clasificacion de Pedidos ===");
        System.out.printf("%-6s %-12s %9s %10s  %s%n",
            "ID", "ESTADO", "PRIORIDAD", "IMPORTE", "CATEGORIA");
        System.out.println("-".repeat(60));

        pedidos.forEach(p -> System.out.printf(
            "%-6s %-12s %9d %9.2f€  %s%n",
            p.id(), p.estado(), p.prioridad(), p.importe(), clasificar(p)
        ));
    }
}
