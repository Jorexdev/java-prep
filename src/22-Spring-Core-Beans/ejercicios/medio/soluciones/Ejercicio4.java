import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Ejercicio4 {

    interface DisposableBean {
        void destroy();
    }

    static class ApplicationContext {
        private final List<Object> registrationOrder = new ArrayList<>();

        <T> T register(T bean) {
            registrationOrder.add(bean);
            if (bean instanceof Runnable r) r.run(); // simula @PostConstruct
            return bean;
        }

        void close() {
            System.out.println("\n[ApplicationContext] Cerrando contexto...");
            Deque<DisposableBean> toDestroy = new ArrayDeque<>();
            for (Object bean : registrationOrder) {
                if (bean instanceof DisposableBean d) toDestroy.push(d); // push → stack LIFO
            }
            while (!toDestroy.isEmpty()) {
                toDestroy.pop().destroy();
            }
            System.out.println("[ApplicationContext] Contexto cerrado.");
        }
    }

    static class DataSource implements DisposableBean, Runnable {
        @Override public void run()     { System.out.println("[DataSource]     @PostConstruct: pool abierto"); }
        @Override public void destroy() { System.out.println("[DataSource]     @PreDestroy:    pool cerrado"); }
    }

    static class Cache implements DisposableBean, Runnable {
        @Override public void run()     { System.out.println("[Cache]          @PostConstruct: cache calentado"); }
        @Override public void destroy() { System.out.println("[Cache]          @PreDestroy:    cache vaciado"); }
    }

    static class ServicioUsuarios implements DisposableBean, Runnable {
        @Override public void run()     { System.out.println("[ServicioUsers]  @PostConstruct: servicio listo"); }
        @Override public void destroy() { System.out.println("[ServicioUsers]  @PreDestroy:    sesiones cerradas"); }
    }

    static class ControladorRest implements Runnable {
        @Override public void run() { System.out.println("[ControladorRest] @PostConstruct: rutas registradas (no DisposableBean)"); }
    }

    public static void main(String[] args) {
        ApplicationContext ctx = new ApplicationContext();

        System.out.println("=== Iniciando beans (orden de registro) ===");
        ctx.register(new DataSource());
        ctx.register(new Cache());
        ctx.register(new ServicioUsuarios());
        ctx.register(new ControladorRest()); // no DisposableBean → no aparece en destroy

        System.out.println("\n[Aplicación corriendo...]");
        ctx.close();
        System.out.println("\nObserva: destroy en orden INVERSO al de creación.");
    }
}
