import java.util.LinkedHashMap;
import java.util.Map;

public class Ejercicio6 {

    static class NoSuchBeanException extends RuntimeException {
        NoSuchBeanException(String nombre) {
            super("No existe bean con nombre '" + nombre + "'");
        }
    }

    // Servicios de ejemplo
    static class DataSource {
        private final String url;
        DataSource(String url) { this.url = url; }
        String conectar() { return "Conectado a: " + url; }
    }

    static class UserRepository {
        String buscar(int id) { return "Usuario#" + id; }
    }

    static class EmailService {
        String enviar(String to, String asunto) {
            return "Email enviado a " + to + " | Asunto: " + asunto;
        }
    }

    // Named container — equivale al ApplicationContext de Spring con nombres
    static class NamedContainer {
        private final Map<String, Object> beans = new LinkedHashMap<>();

        void register(String nombre, Object bean) {
            beans.put(nombre, bean);
            System.out.println("Bean registrado: \"" + nombre + "\" -> " + bean.getClass().getSimpleName());
        }

        @SuppressWarnings("unchecked")
        <T> T get(String nombre) {
            Object bean = beans.get(nombre);
            if (bean == null) {
                throw new NoSuchBeanException(nombre);
            }
            return (T) bean;
        }

        void listar() {
            System.out.println("Beans registrados (" + beans.size() + "):");
            beans.forEach((nombre, bean) ->
                System.out.println("  \"" + nombre + "\" -> " + bean.getClass().getSimpleName()));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Named Beans ===\n");

        NamedContainer container = new NamedContainer();

        // Registrar beans con nombres personalizados
        container.register("dataSource", new DataSource("jdbc:postgresql://localhost:5432/myapp"));
        container.register("userRepo", new UserRepository());
        container.register("emailService", new EmailService());

        System.out.println();
        container.listar();

        System.out.println();

        // Recuperar y usar por nombre — como context.getBean("dataSource") en Spring
        DataSource ds = container.get("dataSource");
        System.out.println(ds.conectar());

        UserRepository repo = container.get("userRepo");
        System.out.println(repo.buscar(42));

        EmailService email = container.get("emailService");
        System.out.println(email.enviar("user@example.com", "Bienvenido"));

        System.out.println();

        // Bean inexistente
        try {
            container.get("cacheService");
        } catch (NoSuchBeanException e) {
            System.out.println("Error esperado: " + e.getMessage());
        }
    }
}
