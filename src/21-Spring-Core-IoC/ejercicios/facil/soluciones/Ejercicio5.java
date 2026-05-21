import java.util.LinkedHashMap;
import java.util.Map;

public class Ejercicio5 {

    interface Formatter {
        String formatear(String texto);
    }

    // En Spring: // @Component("mayusculas")
    static class FormatterMayusculas implements Formatter {
        @Override
        public String formatear(String texto) {
            return texto.toUpperCase();
        }
    }

    // En Spring: // @Component("minusculas")
    static class FormatterMinusculas implements Formatter {
        @Override
        public String formatear(String texto) {
            return texto.toLowerCase();
        }
    }

    static class Procesador {
        // En Spring: Map<String, Formatter> se inyecta con todos los beans del tipo,
        // con la clave siendo el nombre del bean — simulamos eso aquí
        private final Map<String, Formatter> formatters;

        Procesador(Map<String, Formatter> formatters) {
            this.formatters = formatters;
        }

        // @Qualifier equivale a buscar por nombre en el mapa
        String procesar(String qualifier, String texto) {
            Formatter formatter = formatters.get(qualifier);
            if (formatter == null) {
                throw new IllegalArgumentException("No existe formatter con qualifier: " + qualifier);
            }
            return formatter.formatear(texto);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @Qualifier simulado ===\n");

        // El "contenedor" registra beans con nombre — Spring hace esto automáticamente
        Map<String, Formatter> mapaFormatters = new LinkedHashMap<>();
        mapaFormatters.put("mayusculas", new FormatterMayusculas());
        mapaFormatters.put("minusculas", new FormatterMinusculas());

        Procesador procesador = new Procesador(mapaFormatters);

        String texto = "Hola desde Spring IoC";
        System.out.println("Texto original: " + texto);
        System.out.println();

        // Usando qualifier "mayusculas"
        String resultado1 = procesador.procesar("mayusculas", texto);
        System.out.println("@Qualifier(\"mayusculas\"): " + resultado1);

        // Usando qualifier "minusculas"
        String resultado2 = procesador.procesar("minusculas", texto);
        System.out.println("@Qualifier(\"minusculas\"): " + resultado2);

        System.out.println();

        // Qualifier inexistente
        try {
            procesador.procesar("titulo", texto);
        } catch (IllegalArgumentException e) {
            System.out.println("Error esperado: " + e.getMessage());
        }
    }
}
