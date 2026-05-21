import java.util.HashSet;
import java.util.Set;

public class Ejercicio6 {

    static class CircularDependencyException extends RuntimeException {
        CircularDependencyException(String beanName, Set<String> enConstruccion) {
            super("Dependencia circular detectada al crear '" + beanName
                + "'. Cadena de construcción activa: " + enConstruccion);
        }
    }

    static class ContenedorConDeteccion {
        // Registra qué beans están siendo construidos en este momento
        private final Set<String> enConstruccion = new HashSet<>();

        Object crear(String nombreClase) {
            // Si ya está en construcción, hay un ciclo
            if (enConstruccion.contains(nombreClase)) {
                throw new CircularDependencyException(nombreClase, enConstruccion);
            }

            System.out.println("Iniciando construcción de: " + nombreClase);
            enConstruccion.add(nombreClase);

            try {
                // Simula el tiempo de construcción / resolución de dependencias
                Object instancia = simularConstruccion(nombreClase);
                System.out.println("Bean creado con éxito: " + nombreClase);
                return instancia;
            } finally {
                enConstruccion.remove(nombreClase);
            }
        }

        // Simula que ServicioA necesita ServicioB y ServicioB necesita ServicioA
        private Object simularConstruccion(String nombre) {
            return switch (nombre) {
                case "ServicioA-circular" -> {
                    // Para simular la circular, creamos ServicioB que a su vez pide ServicioA
                    crear("ServicioB-circular");
                    yield new Object();
                }
                case "ServicioB-circular" -> {
                    // Esto provoca el ciclo: B pide A cuando A ya está en construcción
                    crear("ServicioA-circular");
                    yield new Object();
                }
                default -> new Object(); // construcción normal sin dependencias circulares
            };
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Circular Dependency Detection ===\n");

        ContenedorConDeteccion contenedor = new ContenedorConDeteccion();

        // Caso 1: construcción normal sin ciclos
        System.out.println("--- Caso 1: sin dependencia circular ---");
        Object bean = contenedor.crear("ServicioNormal");
        System.out.println("Resultado: " + bean);

        System.out.println();

        // Caso 2: dependencia circular
        System.out.println("--- Caso 2: con dependencia circular ---");
        try {
            contenedor.crear("ServicioA-circular");
        } catch (CircularDependencyException e) {
            System.out.println("Excepción capturada: " + e.getMessage());
        }

        System.out.println("\nEl contenedor detectó el ciclo antes de entrar en bucle infinito.");
    }
}
