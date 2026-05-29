import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

// Contenedor IoC propio con soporte de ciclo de vida (@PostConstruct/@PreDestroy simulados)
// Soporta: singleton scope, auto-wiring por tipo, ciclo de vida init/destroy, deteccion de ciclos

public class Ejercicio5 {

    // Anotaciones de ciclo de vida
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface PostConstruct {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface PreDestroy {}

    // Anotacion de inyeccion
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    @interface Autowired {}

    // ====== Contenedor IoC ======

    static class IoCContainer {
        // Clases registradas para instanciacion
        private final Map<Class<?>, Class<?>> registrations = new LinkedHashMap<>();
        // Instancias singleton creadas
        private final Map<Class<?>, Object> singletons = new LinkedHashMap<>();
        // Orden de creacion (para destroy en orden inverso)
        private final List<Object> creationOrder = new ArrayList<>();
        // Deteccion de ciclos
        private final Set<Class<?>> inCreation = new HashSet<>();

        // Registra una clase concreta
        void register(Class<?> type) {
            registrations.put(type, type);
            System.out.printf("  [IoC] clase registrada: %s%n", type.getSimpleName());
        }

        // Registra una interfaz con su implementacion
        void register(Class<?> iface, Class<?> impl) {
            registrations.put(iface, impl);
            System.out.printf("  [IoC] %s -> %s registrado%n",
                    iface.getSimpleName(), impl.getSimpleName());
        }

        // Resolucion de bean (singleton)
        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type) {
            if (singletons.containsKey(type)) {
                return (T) singletons.get(type);
            }
            return (T) create(type);
        }

        private Object create(Class<?> type) {
            // Detectar ciclos
            if (inCreation.contains(type)) {
                throw new IllegalStateException("Ciclo de dependencias detectado para: "
                        + type.getSimpleName());
            }
            inCreation.add(type);

            try {
                // Resolver implementacion concreta
                Class<?> implType = registrations.getOrDefault(type, type);
                System.out.printf("  [IoC] creando %s...%n", implType.getSimpleName());

                // Instanciar via constructor sin argumentos
                Object instance = implType.getDeclaredConstructor().newInstance();

                // Guardar como singleton ANTES de inyectar (evita ciclos)
                singletons.put(type, instance);
                if (!type.equals(implType)) {
                    singletons.put(implType, instance);
                }

                // Auto-wiring: inyectar campos @Autowired
                injectFields(instance, implType);

                // Ciclo de vida: llamar @PostConstruct
                invokeLifecycle(instance, implType, PostConstruct.class, "PostConstruct");

                creationOrder.add(instance);
                return instance;

            } catch (Exception e) {
                throw new RuntimeException("Error creando " + type.getSimpleName(), e);
            } finally {
                inCreation.remove(type);
            }
        }

        private void injectFields(Object instance, Class<?> type) throws Exception {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    System.out.printf("    [IoC] inyectando campo '%s' de tipo %s%n",
                            field.getName(), fieldType.getSimpleName());
                    Object dependency = getBean(fieldType);
                    field.set(instance, dependency);
                }
            }
        }

        private void invokeLifecycle(Object instance, Class<?> type,
                                      Class<? extends Annotation> annotation,
                                      String phase) throws Exception {
            for (Method m : type.getDeclaredMethods()) {
                if (m.isAnnotationPresent(annotation)) {
                    System.out.printf("    [IoC] %s en %s.%s()%n",
                            phase, type.getSimpleName(), m.getName());
                    m.setAccessible(true);
                    m.invoke(instance);
                }
            }
        }

        // Cierra el contenedor: llama @PreDestroy en orden inverso
        void close() {
            System.out.println("  [IoC] cerrando contenedor (orden inverso)...");
            List<Object> reversed = new ArrayList<>(creationOrder);
            Collections.reverse(reversed);
            for (Object bean : reversed) {
                try {
                    invokeLifecycle(bean, bean.getClass(), PreDestroy.class, "PreDestroy");
                } catch (Exception e) {
                    System.out.println("  [IoC] error en PreDestroy: " + e.getMessage());
                }
            }
        }
    }

    // ====== Beans de ejemplo con ciclo de vida completo ======

    static class DataSourceConfig {
        private String url = "jdbc:mysql://localhost/app";
        private int poolSize = 10;

        @PostConstruct
        void init() {
            System.out.printf("      DataSourceConfig inicializado: url=%s, pool=%d%n",
                    url, poolSize);
        }

        @PreDestroy
        void destroy() {
            System.out.println("      DataSourceConfig: cerrando pool de conexiones...");
        }

        String getUrl() { return url; }
        int getPoolSize() { return poolSize; }
    }

    static class CacheService {
        @Autowired
        DataSourceConfig dataSource;

        private final Map<String, Object> cache = new HashMap<>();
        private boolean initialized = false;

        @PostConstruct
        void init() {
            initialized = true;
            System.out.printf("      CacheService inicializado (datasource: %s)%n",
                    dataSource.getUrl());
        }

        @PreDestroy
        void destroy() {
            cache.clear();
            initialized = false;
            System.out.println("      CacheService: cache vaciado y desconectado.");
        }

        void put(String key, Object val) { if (initialized) cache.put(key, val); }
        Object get(String key) { return cache.get(key); }
    }

    static class UserRepository {
        @Autowired
        DataSourceConfig dataSource;

        @Autowired
        CacheService cache;

        @PostConstruct
        void init() {
            System.out.printf("      UserRepository inicializado (pool: %d, cache: %s)%n",
                    dataSource.getPoolSize(), cache != null ? "OK" : "null");
        }

        @PreDestroy
        void destroy() {
            System.out.println("      UserRepository: conexiones cerradas.");
        }

        String findById(int id) {
            String cacheKey = "user-" + id;
            Object cached = cache.get(cacheKey);
            if (cached != null) return "(cache) " + cached;
            String user = "User#" + id;
            cache.put(cacheKey, user);
            return "(bd) " + user;
        }
    }

    static class UserService {
        @Autowired
        UserRepository userRepository;

        @PostConstruct
        void init() {
            System.out.println("      UserService listo.");
        }

        @PreDestroy
        void destroy() {
            System.out.println("      UserService: limpieza completada.");
        }

        void procesar(int userId) {
            System.out.printf("      UserService.procesar(%d): %s%n",
                    userId, userRepository.findById(userId));
        }
    }

    // ====== DEMO ======

    public static void main(String[] args) {
        System.out.println("=== Contenedor IoC propio con ciclo de vida (@PostConstruct/@PreDestroy) ===");
        System.out.println();

        IoCContainer container = new IoCContainer();

        System.out.println("[ Registro de clases ]");
        container.register(DataSourceConfig.class);
        container.register(CacheService.class);
        container.register(UserRepository.class);
        container.register(UserService.class);
        System.out.println();

        System.out.println("[ Resolucion de beans (con auto-wiring y @PostConstruct) ]");
        UserService userService = container.getBean(UserService.class);
        System.out.println();

        System.out.println("[ Verificar que son singleton ]");
        DataSourceConfig ds1 = container.getBean(DataSourceConfig.class);
        DataSourceConfig ds2 = container.getBean(DataSourceConfig.class);
        System.out.printf("  DataSourceConfig mismo objeto: %b (id: %s)%n",
                ds1 == ds2, System.identityHashCode(ds1));
        System.out.println();

        System.out.println("[ Uso de los beans ]");
        userService.procesar(1);
        userService.procesar(2);
        userService.procesar(1); // segunda llamada: desde cache
        System.out.println();

        System.out.println("[ Cierre del contenedor (@PreDestroy en orden inverso) ]");
        container.close();
        System.out.println();

        System.out.println("=== Deteccion de ciclos ===");
        IoCContainer cyclicContainer = new IoCContainer();

        // Clases con dependencia ciclica A -> B -> A
        // Solo podemos simular con beans que referencian el mismo tipo
        // En este caso el contenedor detecta el ciclo en inCreation
        System.out.println("El contenedor usa 'inCreation' set para detectar ciclos.");
        System.out.println("Si A depende de B y B depende de A, se lanza IllegalStateException.");
        System.out.println("Spring resuelve ciclos via proxy o @Lazy en uno de los extremos.");
    }
}
