import java.util.HashMap;
import java.util.Map;

public class Ejercicio3 {

    static class ContenedorIoC {
        private final Map<Class<?>, Object> beans = new HashMap<>();

        void register(Class<?> tipo, Object instancia) {
            beans.put(tipo, instancia);
            System.out.println("Registrado bean: " + tipo.getSimpleName() + " = " + instancia);
        }

        @SuppressWarnings("unchecked")
        <T> T get(Class<T> tipo) {
            Object bean = beans.get(tipo);
            if (bean == null) {
                throw new IllegalStateException("Bean no encontrado para tipo: " + tipo.getSimpleName());
            }
            return (T) bean;
        }

        int size() {
            return beans.size();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Contenedor IoC mínimo ===\n");

        ContenedorIoC contenedor = new ContenedorIoC();

        // Registrar beans — en Spring esto lo hace el classpath scan o @Bean
        contenedor.register(String.class, "hola");
        contenedor.register(Integer.class, 42);

        System.out.println("\nBeans registrados: " + contenedor.size());
        System.out.println();

        // Recuperar beans — en Spring: context.getBean(String.class)
        String texto = contenedor.get(String.class);
        Integer numero = contenedor.get(Integer.class);

        System.out.println("Recuperado String: " + texto);
        System.out.println("Recuperado Integer: " + numero);

        System.out.println();

        // Tipo no registrado — lanza excepción
        try {
            contenedor.get(Double.class);
        } catch (IllegalStateException e) {
            System.out.println("Error esperado: " + e.getMessage());
        }
    }
}
