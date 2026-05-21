import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Ejercicio1 {

    // --- Beans de ejemplo ---

    static class Config {
        private final String entorno;

        Config() {
            this.entorno = "produccion";
            System.out.println("  [new] Config()");
        }

        String getEntorno() { return entorno; }
    }

    static class Cache {
        private final Config config;

        Cache(Config config) {
            this.config = config;
            System.out.println("  [new] Cache(Config)");
        }

        String info() { return "Cache[entorno=" + config.getEntorno() + "]"; }
    }

    static class AppService {
        private final Cache cache;
        private final Config config;

        AppService(Cache cache, Config config) {
            this.cache = cache;
            this.config = config;
            System.out.println("  [new] AppService(Cache, Config)");
        }

        String describe() {
            return "AppService usa " + cache.info() + " y Config en " + config.getEntorno();
        }
    }

    // --- IoC Container ---

    static class IoCContainer {
        private final Map<Class<?>, Class<?>> registered = new HashMap<>();
        private final Map<Class<?>, Object> singletons = new HashMap<>();
        private final Set<Class<?>> enCreacion = new HashSet<>();

        void register(Class<?> clase) {
            registered.put(clase, clase);
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> tipo) {
            if (singletons.containsKey(tipo)) {
                return (T) singletons.get(tipo);
            }

            if (enCreacion.contains(tipo)) {
                throw new RuntimeException("Dependencia circular detectada: " + tipo.getSimpleName());
            }

            if (!registered.containsKey(tipo)) {
                throw new IllegalStateException("Clase no registrada: " + tipo.getSimpleName());
            }

            enCreacion.add(tipo);
            try {
                Constructor<?> constructor = Arrays.stream(tipo.getDeclaredConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow();

                Object[] args = Arrays.stream(constructor.getParameterTypes())
                    .map(paramType -> getBean(paramType))
                    .toArray();

                T instancia = (T) constructor.newInstance(args);
                singletons.put(tipo, instancia);
                return instancia;

            } catch (Exception e) {
                throw new RuntimeException("Error instanciando " + tipo.getSimpleName(), e);
            } finally {
                enCreacion.remove(tipo);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== IoC Container con reflection ===\n");

        IoCContainer container = new IoCContainer();
        container.register(Config.class);
        container.register(Cache.class);
        container.register(AppService.class);

        System.out.println("Solicitando AppService (resuelve toda la cadena):");
        AppService service = container.getBean(AppService.class);

        System.out.println("\nResultado: " + service.describe());

        System.out.println("\nVerificando singleton de Config:");
        Config c1 = container.getBean(Config.class);
        Config c2 = container.getBean(Config.class);
        System.out.println("c1 == c2: " + (c1 == c2) + " <- mismo objeto");

        // Config dentro de Cache es la misma que se obtiene directamente
        Cache cache = container.getBean(Cache.class);
        System.out.println("Config en Cache == Config directo: " + (c1 == c2));
    }
}
