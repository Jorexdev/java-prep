import java.util.ArrayList;
import java.util.List;

public class Ejercicio4 {

    interface Formatter {
        String formatear(String texto);
    }

    // En Spring: // @Component("mayusculas") // @Primary
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

    static class ContenedorSimple {
        private final List<Formatter> formatters = new ArrayList<>();

        // El primero registrado actúa como @Primary
        void register(Formatter formatter) {
            formatters.add(formatter);
            System.out.println("Registrado: " + formatter.getClass().getSimpleName()
                + (formatters.size() == 1 ? " [PRIMARY]" : ""));
        }

        // Devuelve el primary (el primero) — igual que @Primary en Spring
        Formatter getFormatter() {
            if (formatters.isEmpty()) {
                throw new IllegalStateException("No hay formatters registrados");
            }
            return formatters.get(0);
        }

        List<Formatter> getAll() {
            return formatters;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @Primary simulado ===\n");

        ContenedorSimple contenedor = new ContenedorSimple();
        contenedor.register(new FormatterMayusculas()); // primary
        contenedor.register(new FormatterMinusculas());

        System.out.println("\nTotal formatters: " + contenedor.getAll().size());

        Formatter primary = contenedor.getFormatter();
        System.out.println("Primary: " + primary.getClass().getSimpleName());

        String texto = "Hola Mundo Spring IoC";
        System.out.println("\nTexto original:  " + texto);
        System.out.println("Con primary:     " + primary.formatear(texto));
    }
}
