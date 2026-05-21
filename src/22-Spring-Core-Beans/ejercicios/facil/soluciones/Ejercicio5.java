import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Ejercicio5 {

    static class ServicioC {
        ServicioC() {
            System.out.println("Iniciando ServicioC (sin dependencias)");
        }
        String info() { return "ServicioC"; }
    }

    static class ServicioB {
        private final ServicioC c;
        ServicioB(ServicioC c) {
            this.c = c;
            System.out.println("Iniciando ServicioB (depende de: " + c.info() + ")");
        }
        String info() { return "ServicioB -> " + c.info(); }
    }

    static class ServicioA {
        private final ServicioB b;
        ServicioA(ServicioB b) {
            this.b = b;
            System.out.println("Iniciando ServicioA (depende de: " + b.info() + ")");
        }
        String info() { return "ServicioA -> " + b.info(); }
    }

    // Registro: nombre -> (dependencias, factory)
    static class BeanEntry {
        final List<String> dependencias;
        final Supplier<Object> factory;
        Object instancia;

        BeanEntry(List<String> dependencias, Supplier<Object> factory) {
            this.dependencias = dependencias;
            this.factory = factory;
        }
    }

    static class OrdenContenedor {
        private final Map<String, BeanEntry> registro = new LinkedHashMap<>();

        void register(String nombre, List<String> dependencias, Supplier<Object> factory) {
            registro.put(nombre, new BeanEntry(dependencias, factory));
        }

        void startAll() {
            System.out.println("Calculando orden topológico...");
            List<String> orden = ordenTopologico();
            System.out.println("Orden de inicio: " + orden + "\n");

            for (String nombre : orden) {
                BeanEntry entry = registro.get(nombre);
                entry.instancia = entry.factory.get();
            }
        }

        private List<String> ordenTopologico() {
            List<String> resultado = new ArrayList<>();
            Map<String, Boolean> visitado = new HashMap<>();

            for (String nombre : registro.keySet()) {
                visitar(nombre, visitado, resultado);
            }
            return resultado;
        }

        private void visitar(String nombre, Map<String, Boolean> visitado, List<String> resultado) {
            if (Boolean.TRUE.equals(visitado.get(nombre))) return;

            visitado.put(nombre, true);
            BeanEntry entry = registro.get(nombre);
            for (String dep : entry.dependencias) {
                visitar(dep, visitado, resultado);
            }
            resultado.add(nombre);
        }

        Object get(String nombre) {
            BeanEntry entry = registro.get(nombre);
            if (entry == null || entry.instancia == null) {
                throw new IllegalStateException("Bean no iniciado: " + nombre);
            }
            return entry.instancia;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @DependsOn simulado — orden de inicialización ===\n");

        OrdenContenedor contenedor = new OrdenContenedor();

        // A depende de B, B depende de C — registramos en orden "desorden"
        contenedor.register("servicioA", List.of("servicioB"), () -> {
            ServicioB b = (ServicioB) contenedor.get("servicioB");
            return new ServicioA(b);
        });
        contenedor.register("servicioB", List.of("servicioC"), () -> {
            ServicioC c = (ServicioC) contenedor.get("servicioC");
            return new ServicioB(c);
        });
        contenedor.register("servicioC", List.of(), ServicioC::new);

        contenedor.startAll();

        System.out.println("\nResultado final:");
        ServicioA a = (ServicioA) contenedor.get("servicioA");
        System.out.println(a.info());
    }
}
