import java.util.List;

public class Ejercicio4 {

    static abstract class ExportadorDatos {
        final void exportar(List<String> datos) {
            abrirConexion();
            validarDatos(datos);
            datos.forEach(d -> System.out.println(transformar(d)));
            cerrarConexion();
        }

        void abrirConexion()  { System.out.println("Abriendo conexión..."); }
        void validarDatos(List<String> d) {
            if (d == null || d.isEmpty()) throw new IllegalArgumentException("Sin datos");
            System.out.println("Validando " + d.size() + " registros...");
        }
        abstract String transformar(String dato);
        void cerrarConexion() { System.out.println("Cerrando conexión."); }
    }

    static class ExportadorCSV extends ExportadorDatos {
        @Override public String transformar(String dato) { return dato.replace("|", ","); }
    }

    static class ExportadorXML extends ExportadorDatos {
        @Override public String transformar(String dato) {
            String[] p = dato.split("\\|");
            return "<item><nombre>" + p[0] + "</nombre><valor>" + (p.length > 1 ? p[1] : "") + "</valor></item>";
        }
    }

    public static void main(String[] args) {
        List<String> datos = List.of("Laptop|999", "Raton|24", "Teclado|49");

        System.out.println("=== CSV ===");
        new ExportadorCSV().exportar(datos);
        System.out.println("\n=== XML ===");
        new ExportadorXML().exportar(datos);
    }
}
