import java.util.HashMap;
import java.util.Map;

public class Ejercicio1 {

    // Beans de ejemplo
    static class UsuarioServicio {
        String saludar(String nombre) {
            return "Hola, " + nombre + "!";
        }
    }

    static class ProductoRepositorio {
        String buscar(int id) {
            return "Producto#" + id;
        }
    }

    static class PedidoControlador {
        String listar() {
            return "GET /pedidos -> []";
        }
    }

    // ApplicationContext mínimo
    static class AppContext {
        private final Map<String, Object> beansNombre = new HashMap<>();
        private final Map<Class<?>, Object> beansTipo = new HashMap<>();

        void scan(Object... beans) {
            for (Object bean : beans) {
                String nombre = bean.getClass().getSimpleName().toLowerCase();
                beansNombre.put(nombre, bean);
                beansTipo.put(bean.getClass(), bean);
                System.out.println("Bean registrado: " + nombre + " -> " + bean.getClass().getSimpleName());
            }
        }

        // Busca por nombre ignorando mayúsculas/minúsculas
        Object getBean(String name) {
            Object bean = beansNombre.get(name.toLowerCase());
            if (bean == null) {
                throw new IllegalArgumentException("Bean no encontrado: " + name);
            }
            return bean;
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> tipo) {
            Object bean = beansTipo.get(tipo);
            if (bean == null) {
                throw new IllegalArgumentException("Bean no encontrado para tipo: " + tipo.getSimpleName());
            }
            return (T) bean;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== ApplicationContext mínimo ===\n");

        AppContext ctx = new AppContext();
        ctx.scan(
            new UsuarioServicio(),
            new ProductoRepositorio(),
            new PedidoControlador()
        );

        System.out.println();

        // Recuperar por nombre (como ctx.getBean("usuarioServicio") en Spring)
        UsuarioServicio us = (UsuarioServicio) ctx.getBean("usuarioservicio");
        System.out.println("getBean por nombre: " + us.saludar("Jorex"));

        ProductoRepositorio pr = ctx.getBean(ProductoRepositorio.class);
        System.out.println("getBean por tipo:   " + pr.buscar(7));

        PedidoControlador pc = (PedidoControlador) ctx.getBean("PedidoControlador");
        System.out.println("getBean case-insensitive: " + pc.listar());

        System.out.println();

        try {
            ctx.getBean("noExiste");
        } catch (IllegalArgumentException e) {
            System.out.println("Error esperado: " + e.getMessage());
        }
    }
}
