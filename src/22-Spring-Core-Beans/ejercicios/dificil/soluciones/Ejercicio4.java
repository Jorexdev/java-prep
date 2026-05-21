import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Ejercicio4 {

    interface BeanNameAware {
        void setBeanName(String name);
    }

    interface ApplicationContextAware {
        void setApplicationContext(AwareContainer ctx);
    }

    static class AwareContainer {
        private final Map<String, Object> beans = new HashMap<>();

        <T> T register(String name, Supplier<T> factory) {
            T bean = factory.get();
            // Aplicar BeanNameAware
            if (bean instanceof BeanNameAware bna) {
                bna.setBeanName(name);
            }
            // Aplicar ApplicationContextAware
            if (bean instanceof ApplicationContextAware aca) {
                aca.setApplicationContext(this);
            }
            beans.put(name, bean);
            return bean;
        }

        Object getBean(String name) { return beans.get(name); }
        String listBeans()          { return beans.keySet().toString(); }
    }

    // Bean que sabe su nombre
    static class ServicioAuditoria implements BeanNameAware {
        private String beanName;

        @Override
        public void setBeanName(String name) {
            this.beanName = name;
            System.out.println("[BeanNameAware] setBeanName(\"" + name + "\") → self-aware");
        }

        void auditar(String accion) {
            System.out.println("[" + beanName + "] AUDIT: " + accion);
        }
    }

    // Bean que busca otros beans
    static class ServicioPedidos implements ApplicationContextAware, BeanNameAware {
        private String beanName;
        private AwareContainer ctx;

        @Override public void setBeanName(String name) {
            this.beanName = name;
            System.out.println("[BeanNameAware] " + name + " recibió su nombre");
        }

        @Override public void setApplicationContext(AwareContainer ctx) {
            this.ctx = ctx;
            System.out.println("[AppContextAware] " + beanName + " recibió el contexto");
        }

        void crearPedido(int id) {
            ServicioAuditoria auditoria = (ServicioAuditoria) ctx.getBean("servicioAuditoria");
            auditoria.auditar("Pedido #" + id + " creado por " + beanName);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== AwareInterfaces: el contenedor inyecta metadatos ===\n");
        AwareContainer ctx = new AwareContainer();

        System.out.println("--- Registrando servicioAuditoria ---");
        ctx.register("servicioAuditoria", ServicioAuditoria::new);

        System.out.println("\n--- Registrando servicioPedidos ---");
        ServicioPedidos pedidos = ctx.register("servicioPedidos", ServicioPedidos::new);

        System.out.println("\n--- Usando los beans ---");
        pedidos.crearPedido(101);
        pedidos.crearPedido(102);

        System.out.println("\nBeans en el contexto: " + ctx.listBeans());
    }
}
