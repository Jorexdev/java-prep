import java.util.List;

public class Ejercicio1 {

    record Venta(String producto, int cantidad, double precio) {
        double total() { return cantidad * precio; }
    }

    static class ReporteVentas {
        private final List<Venta> ventas;
        ReporteVentas(List<Venta> ventas) { this.ventas = ventas; }
        List<Venta> getVentas() { return ventas; }
        double totalGeneral() { return ventas.stream().mapToDouble(Venta::total).sum(); }
    }

    interface Generador {
        String generar(ReporteVentas reporte);
    }

    static class GeneradorHTML implements Generador {
        @Override public String generar(ReporteVentas r) {
            var sb = new StringBuilder("<table>\n");
            for (var v : r.getVentas())
                sb.append("  <tr><td>").append(v.producto()).append("</td><td>").append(v.total()).append("</td></tr>\n");
            sb.append("  <tr><td>TOTAL</td><td>").append(r.totalGeneral()).append("</td></tr>\n</table>");
            return sb.toString();
        }
    }

    static class GeneradorCSV implements Generador {
        @Override public String generar(ReporteVentas r) {
            var sb = new StringBuilder("producto,cantidad,precio,total\n");
            for (var v : r.getVentas())
                sb.append(v.producto()).append(",").append(v.cantidad()).append(",")
                  .append(v.precio()).append(",").append(v.total()).append("\n");
            return sb.toString();
        }
    }

    static class GeneradorJSON implements Generador {
        @Override public String generar(ReporteVentas r) {
            var sb = new StringBuilder("[\n");
            for (var v : r.getVentas())
                sb.append("  {\"producto\":\"").append(v.producto()).append("\",\"total\":").append(v.total()).append("},\n");
            if (!r.getVentas().isEmpty()) sb.setLength(sb.length() - 2);
            return sb.append("\n]").toString();
        }
    }

    public static void main(String[] args) {
        var reporte = new ReporteVentas(List.of(
            new Venta("Teclado", 3, 49.99),
            new Venta("Ratón",   5, 24.99)
        ));

        for (Generador g : List.of(new GeneradorHTML(), new GeneradorCSV(), new GeneradorJSON())) {
            System.out.println(g.generar(reporte));
            System.out.println("---");
        }
    }
}
