import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Ejercicio3 {

    static class CacheCaliente {
        CacheCaliente() {
            System.out.println("[LAZY] Inicializando CacheCaliente... (carga pesada simulada)");
        }

        String obtener(String clave) {
            return "valor-cache[" + clave + "]";
        }
    }

    static class OtroServicio {
        OtroServicio() {
            System.out.println("[EAGER] OtroServicio creado inmediatamente");
        }
    }

    static class ContenedorLazy {
        // Mapa de fábricas (Supplier) — el bean no existe hasta que se pide
        private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();
        // Mapa de instancias ya creadas
        private final Map<Class<?>, Object> instancias = new HashMap<>();

        <T> void registerLazy(Class<T> tipo, Supplier<T> factory) {
            factories.put(tipo, factory);
            System.out.println("Bean registrado como LAZY: " + tipo.getSimpleName() + " (no inicializado)");
        }

        <T> void registerEager(Class<T> tipo, T instancia) {
            instancias.put(tipo, instancia);
            System.out.println("Bean registrado como EAGER: " + tipo.getSimpleName() + " (ya inicializado)");
        }

        @SuppressWarnings("unchecked")
        <T> T get(Class<T> tipo) {
            // Si ya existe la instancia, devolverla
            if (instancias.containsKey(tipo)) {
                System.out.println("[GET] Devolviendo instancia existente: " + tipo.getSimpleName());
                return (T) instancias.get(tipo);
            }
            // Si hay fábrica lazy, crear ahora (primera vez)
            if (factories.containsKey(tipo)) {
                System.out.println("[GET] Primera petición de " + tipo.getSimpleName() + " — creando ahora...");
                T instancia = (T) factories.get(tipo).get();
                instancias.put(tipo, instancia); // guardar para siguientes peticiones
                factories.remove(tipo);
                return instancia;
            }
            throw new IllegalStateException("Bean no registrado: " + tipo.getSimpleName());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Lazy Initialization ===\n");

        ContenedorLazy contenedor = new ContenedorLazy();

        System.out.println("\n-- Registrando beans --");
        contenedor.registerLazy(CacheCaliente.class, CacheCaliente::new);
        contenedor.registerEager(OtroServicio.class, new OtroServicio());

        System.out.println("\n-- Aquí CacheCaliente NO se ha inicializado aún --");
        System.out.println("(No debe aparecer '[LAZY] Inicializando...' hasta ahora)\n");

        System.out.println("-- Primera llamada a get(CacheCaliente) --");
        CacheCaliente cache1 = contenedor.get(CacheCaliente.class);
        System.out.println("Resultado: " + cache1.obtener("usuario-1"));

        System.out.println("\n-- Segunda llamada a get(CacheCaliente) --");
        CacheCaliente cache2 = contenedor.get(CacheCaliente.class);
        System.out.println("Resultado: " + cache2.obtener("usuario-2"));

        System.out.println("\ncache1 == cache2: " + (cache1 == cache2) + " <- misma instancia (singleton lazy)");
    }
}
