import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class ExpConfig {

    // Agrupa propiedades relacionadas — equivalente a @ConfigurationProperties en Spring Boot
    static class AppConfig {
        final String nombre;
        final int timeout;
        final int reintentos;

        AppConfig(String nombre, int timeout, int reintentos) {
            this.nombre    = nombre;
            this.timeout   = timeout;
            this.reintentos = reintentos;
        }
    }

    @Configuration
    static class SpringConfig {

        // Necesario para resolver placeholders ${...} en @Value
        @Bean
        static PropertySourcesPlaceholderConfigurer configurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        // @Value con default: si la propiedad no existe usa el valor tras ':'
        @Bean
        AppConfig appConfig(
                @Value("${app.nombre}")          String nombre,
                @Value("${app.timeout:30}")      int timeout,
                @Value("${app.reintentos:3}")    int reintentos) {
            return new AppConfig(nombre, timeout, reintentos);
        }
    }

    public static void main(String[] args) {
        // Simula application.properties cargado en el contexto
        // En Spring Boot esto lo hace automáticamente al arrancar
        var ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().getPropertySources().addFirst(
            new MapPropertySource("application.properties", Map.of(
                "app.nombre",   "java-prep",
                "app.timeout",  "45"
                // app.reintentos no definida → usará el default 3
            ))
        );
        ctx.register(SpringConfig.class);
        ctx.refresh();

        AppConfig config = ctx.getBean(AppConfig.class);
        System.out.println("Nombre:     " + config.nombre);
        System.out.println("Timeout:    " + config.timeout + "s");
        System.out.println("Reintentos: " + config.reintentos);  // 3 (default)

        // Acceso directo al Environment — útil para leer propiedades de forma programática
        var env = ctx.getEnvironment();
        System.out.println("Via env:    " + env.getProperty("app.nombre"));
        System.out.println("Missing:    " + env.getProperty("app.debug", "false"));

        ctx.close();
    }
}
