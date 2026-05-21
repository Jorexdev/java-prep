import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Ejercicio4 {

    static class CacheCaliente {
        private final List<String> datos;

        CacheCaliente() {
            System.out.println("[CacheCaliente] Inicializando cache (tardará 2s simulados)...");
            // Simula una carga costosa
            datos = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                datos.add("item-" + i);
            }
            System.out.println("[CacheCaliente] Cache lista con " + datos.size() + " elementos");
        }

        String get(int idx) {
            return datos.get(idx);
        }

        int size() { return datos.size(); }
    }

    static class ContenedorLazy {
        private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();
        private final Map<Class<?>, Object> instancias = new HashMap<>();

        <T> void register(Class<T> tipo, Supplier<T> factory, boolean lazy) {
            if (lazy) {
                factories.put(tipo, factory);
                System.out.println("[Contenedor] " + tipo.getSimpleName() + " registrado como LAZY");
            } else {
                System.out.println("[Contenedor] " + tipo.getSimpleName() + " registrado como EAGER — inicializando ahora:");
                T instancia = factory.get();
                instancias.put(tipo, instancia);
            }
        }

        @SuppressWarnings("unchecked")
        <T> T get(Class<T> tipo) {
            if (instancias.containsKey(tipo)) {
                return (T) instancias.get(tipo);
            }
            if (factories.containsKey(tipo)) {
                System.out.println("[Contenedor] Primera solicitud de " + tipo.getSimpleName() + " — creando ahora:");
                T instancia = (T) factories.get(tipo).get();
                instancias.put(tipo, instancia);
                factories.remove(tipo);
                return instancia;
            }
            throw new IllegalStateException("Bean no registrado: " + tipo.getSimpleName());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @Lazy simulado: Eager vs Lazy ===\n");

        System.out.println("--- Comportamiento EAGER ---");
        ContenedorLazy contenedor1 = new ContenedorLazy();
        System.out.println("Registrando CacheCaliente como EAGER...");
        contenedor1.register(CacheCaliente.class, CacheCaliente::new, false);
        System.out.println("(Nota: ya se inicializó al registrar)");

        System.out.println("\n--- Comportamiento LAZY ---");
        ContenedorLazy contenedor2 = new ContenedorLazy();
        System.out.println("Registrando CacheCaliente como LAZY...");
        contenedor2.register(CacheCaliente.class, CacheCaliente::new, true);
        System.out.println("(Nota: NO se inicializó aún)");

        System.out.println("\n--- Usando el bean lazy por primera vez ---");
        CacheCaliente cache = contenedor2.get(CacheCaliente.class);
        System.out.println("Elemento 5: " + cache.get(5));

        System.out.println("\n--- Segunda solicitud del bean lazy ---");
        CacheCaliente cache2 = contenedor2.get(CacheCaliente.class);
        System.out.println("cache == cache2: " + (cache == cache2) + " (singleton lazy)");
    }
}
