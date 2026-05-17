import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

public class ExpPerfiles {

    interface ServicioEmail {
        void enviar(String destinatario, String asunto);
    }

    // Solo se registra cuando el perfil activo es "dev"
    @Profile("dev")
    static class EmailFalso implements ServicioEmail {
        @Override
        public void enviar(String destinatario, String asunto) {
            System.out.println("[DEV] Email simulado → " + destinatario + ": " + asunto);
        }
    }

    // Solo se registra cuando el perfil activo es "prod"
    @Profile("prod")
    static class EmailSMTP implements ServicioEmail {
        @Override
        public void enviar(String destinatario, String asunto) {
            System.out.println("[PROD] SMTP → " + destinatario + ": " + asunto);
        }
    }

    @Configuration
    static class AppConfig {

        @Bean
        @Profile("dev")
        ServicioEmail emailDev() { return new EmailFalso(); }

        @Bean
        @Profile("prod")
        ServicioEmail emailProd() { return new EmailSMTP(); }
    }

    static void ejecutarConPerfil(String perfil) {
        // En Spring Boot: SPRING_PROFILES_ACTIVE=prod o --spring.profiles.active=prod
        // Aquí lo activamos programáticamente antes del refresh
        var ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().setActiveProfiles(perfil);
        ctx.register(AppConfig.class);
        ctx.refresh();

        System.out.println("Perfil activo: " + perfil);
        ServicioEmail email = ctx.getBean(ServicioEmail.class);
        email.enviar("jorex@example.com", "Bienvenido a java-prep");

        ctx.close();
    }

    public static void main(String[] args) {
        ejecutarConPerfil("dev");
        System.out.println();
        ejecutarConPerfil("prod");
    }
}
