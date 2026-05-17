import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

public class ExpBeans {

    // InitializingBean  → equivalente a @PostConstruct en Spring Boot
    // DisposableBean    → equivalente a @PreDestroy  en Spring Boot
    static class ConexionDB implements InitializingBean, DisposableBean {

        private final String url;

        ConexionDB(String url) {
            this.url = url;
            System.out.println("[CONSTRUCTOR] ConexionDB creada para: " + url);
        }

        @Override
        public void afterPropertiesSet() {
            // Se ejecuta después de que Spring inyecta todas las dependencias
            System.out.println("[INIT] Conexión abierta → " + url);
        }

        @Override
        public void destroy() {
            // Se ejecuta al cerrar el ApplicationContext
            System.out.println("[DESTROY] Conexión cerrada → " + url);
        }

        public void query(String sql) {
            System.out.println("[QUERY] " + sql);
        }
    }

    // Prototype: nueva instancia en cada getBean() / inyección
    static class Tarea {
        private static int contador = 0;
        private final int id = ++contador;

        public void ejecutar() {
            System.out.println("  Tarea #" + id + " ejecutándose");
        }
    }

    @Configuration
    static class AppConfig {

        @Bean
        ConexionDB conexion() {
            return new ConexionDB("jdbc:postgresql://localhost:5432/app");
        }

        @Bean
        @Scope("prototype")
        Tarea tarea() {
            return new Tarea();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Singleton ===");
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {

            ConexionDB c1 = ctx.getBean(ConexionDB.class);
            ConexionDB c2 = ctx.getBean(ConexionDB.class);
            System.out.println("Misma instancia: " + (c1 == c2)); // true

            c1.query("SELECT * FROM usuarios");

            System.out.println("\n=== Prototype ===");
            Tarea t1 = ctx.getBean(Tarea.class);
            Tarea t2 = ctx.getBean(Tarea.class);
            System.out.println("Misma instancia: " + (t1 == t2)); // false
            t1.ejecutar();
            t2.ejecutar();

            System.out.println("\n=== Cerrando contexto ===");
            // Al salir del try-with-resources Spring llama destroy() en los singleton
        }
    }
}
