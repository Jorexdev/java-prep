import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Ejercicio4 {

    // Cadena de dependencias: Controlador -> Servicio -> Repositorio
    static class RepositorioImpl {
        // Sin dependencias
        RepositorioImpl() {
            System.out.println("  [new] RepositorioImpl()");
        }

        String findAll() {
            return "[item1, item2, item3]";
        }
    }

    static class ServicioImpl {
        private final RepositorioImpl repo;

        // Spring detecta este constructor y resuelve RepositorioImpl
        ServicioImpl(RepositorioImpl repo) {
            this.repo = repo;
            System.out.println("  [new] ServicioImpl(RepositorioImpl)");
        }

        String listar() {
            return "Servicio -> " + repo.findAll();
        }
    }

    static class ControladorImpl {
        private final ServicioImpl servicio;

        // Spring detecta este constructor y resuelve ServicioImpl
        ControladorImpl(ServicioImpl servicio) {
            this.servicio = servicio;
            System.out.println("  [new] ControladorImpl(ServicioImpl)");
        }

        String manejar() {
            return "Controlador -> " + servicio.listar();
        }
    }

    static class BeanFactory {
        // Registro de clases (no instancias) — Spring también parte de las clases
        private final Map<Class<?>, Class<?>> clases = new HashMap<>();
        // Cache de singletons ya creados
        private final Map<Class<?>, Object> instancias = new HashMap<>();

        void register(Class<?> clase) {
            clases.put(clase, clase);
            System.out.println("Clase registrada: " + clase.getSimpleName());
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> tipo) {
            // Singleton cache
            if (instancias.containsKey(tipo)) {
                return (T) instancias.get(tipo);
            }

            Class<?> clase = clases.get(tipo);
            if (clase == null) {
                throw new IllegalStateException("Clase no registrada: " + tipo.getSimpleName());
            }

            try {
                // Tomar el constructor con más parámetros (greedy autowiring)
                Constructor<?> constructor = getMostSpecificConstructor(clase);
                Class<?>[] paramTypes = constructor.getParameterTypes();

                System.out.println("Auto-wiring " + clase.getSimpleName()
                    + " (" + paramTypes.length + " deps)");

                // Resolver dependencias recursivamente
                Object[] args = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = getBean(paramTypes[i]);
                }

                Object instancia = constructor.newInstance(args);
                instancias.put(tipo, instancia);
                return (T) instancia;

            } catch (Exception e) {
                throw new RuntimeException("Error creando bean: " + tipo.getSimpleName(), e);
            }
        }

        private Constructor<?> getMostSpecificConstructor(Class<?> clase) {
            Constructor<?>[] constructors = clase.getDeclaredConstructors();
            Constructor<?> mejor = constructors[0];
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() > mejor.getParameterCount()) {
                    mejor = c;
                }
            }
            return mejor;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== BeanFactory con auto-wiring por reflection ===\n");

        BeanFactory factory = new BeanFactory();
        factory.register(RepositorioImpl.class);
        factory.register(ServicioImpl.class);
        factory.register(ControladorImpl.class);

        System.out.println("\nSolicitando ControladorImpl (resuelve deps automáticamente):");
        ControladorImpl controlador = factory.getBean(ControladorImpl.class);

        System.out.println("\nResultado: " + controlador.manejar());
    }
}
