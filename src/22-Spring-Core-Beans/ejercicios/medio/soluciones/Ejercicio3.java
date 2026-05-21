import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Ejercicio3 {

    interface BeanPostProcessor {
        Object postProcessBefore(Object bean, String name);
        Object postProcessAfter(Object bean, String name);
    }

    static class LoggingPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessBefore(Object bean, String name) {
            System.out.println("  [PostProcessor] BEFORE init: " + name + " (" + bean.getClass().getSimpleName() + ")");
            return bean;
        }

        @Override
        public Object postProcessAfter(Object bean, String name) {
            System.out.println("  [PostProcessor] AFTER  init: " + name + " — READY");
            return bean;
        }
    }

    static class Container {
        private final List<BeanPostProcessor> processors = new ArrayList<>();
        private final java.util.Map<String, Object> beans = new java.util.HashMap<>();

        void addPostProcessor(BeanPostProcessor pp) { processors.add(pp); }

        <T> T register(String name, Supplier<T> factory) {
            T bean = factory.get();
            Object current = bean;
            for (BeanPostProcessor pp : processors) {
                current = pp.postProcessBefore(current, name);
            }
            for (BeanPostProcessor pp : processors) {
                current = pp.postProcessAfter(current, name);
            }
            @SuppressWarnings("unchecked")
            T result = (T) current;
            beans.put(name, result);
            return result;
        }
    }

    static class RepositorioUsuarios {
        String query(String id) { return "Usuario[" + id + "]"; }
    }

    static class ServicioEmail {
        void enviar(String to, String msg) { System.out.println("Email → " + to + ": " + msg); }
    }

    static class ControladorPedidos {
        void procesar(int id) { System.out.println("Pedido #" + id + " procesado"); }
    }

    public static void main(String[] args) {
        Container ctx = new Container();
        ctx.addPostProcessor(new LoggingPostProcessor());

        System.out.println("=== Registrando beans con PostProcessor ===");
        var repo = ctx.register("repositorioUsuarios", RepositorioUsuarios::new);
        var mail = ctx.register("servicioEmail", ServicioEmail::new);
        var ctrl = ctx.register("controladorPedidos", ControladorPedidos::new);

        System.out.println("\n=== Usando los beans ===");
        System.out.println(repo.query("42"));
        mail.enviar("test@test.com", "bienvenido");
        ctrl.procesar(1001);
    }
}
