import java.util.HashMap;
import java.util.Map;

// En Spring Boot, @Profile filtra qué beans se registran según el perfil activo.
// SPRING_PROFILES_ACTIVE=prod o --spring.profiles.active=prod activan el perfil.
// Aquí se replica la misma lógica con una variable String activeProfile.
public class ExpPerfiles {

    interface ServicioEmail {
        void enviar(String destinatario, String asunto);
    }

    // @Profile("dev") — Solo activo cuando el perfil activo es "dev"
    static class EmailFalso implements ServicioEmail {
        @Override
        public void enviar(String destinatario, String asunto) {
            System.out.println("[DEV] Email simulado → " + destinatario + ": " + asunto);
        }
    }

    // @Profile("prod") — Solo activo cuando el perfil activo es "prod"
    static class EmailSMTP implements ServicioEmail {
        @Override
        public void enviar(String destinatario, String asunto) {
            System.out.println("[PROD] SMTP → " + destinatario + ": " + asunto);
        }
    }

    // Registro de beans que respeta perfiles — equivalente al contenedor de Spring Boot
    static class ProfileRegistry {
        private final String activeProfile;  // equivale a SPRING_PROFILES_ACTIVE
        private final Map<Class<?>, Object> beans = new HashMap<>();

        ProfileRegistry(String activeProfile) {
            this.activeProfile = activeProfile;
        }

        // Registra el bean solo si el perfil declarado coincide con el activo.
        // Equivale a anotar un @Bean con @Profile("dev") / @Profile("prod").
        <T> void registerForProfile(Class<T> type, String profile, java.util.function.Supplier<T> factory) {
            if (profile.equals(activeProfile)) {   // @Profile("dev") / @Profile("prod")
                beans.put(type, factory.get());
            }
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type) {
            Object bean = beans.get(type);
            if (bean == null) throw new IllegalStateException(
                "Bean no encontrado para el tipo " + type.getSimpleName() +
                " con perfil activo: " + activeProfile);
            return (T) bean;
        }
    }

    // Equivalente a @Configuration — declara qué beans existen y bajo qué perfil
    static class AppConfig {

        static void configure(ProfileRegistry registry) {
            // @Bean @Profile("dev")
            registry.registerForProfile(ServicioEmail.class, "dev",  EmailFalso::new);

            // @Bean @Profile("prod")
            registry.registerForProfile(ServicioEmail.class, "prod", EmailSMTP::new);
        }
    }

    static void ejecutarConPerfil(String perfil) {
        // En Spring Boot: SPRING_PROFILES_ACTIVE=prod o --spring.profiles.active=prod
        // Aquí lo establecemos directamente en el ProfileRegistry antes del refresh
        ProfileRegistry registry = new ProfileRegistry(perfil);
        AppConfig.configure(registry);

        System.out.println("Perfil activo: " + perfil);
        ServicioEmail email = registry.getBean(ServicioEmail.class);
        email.enviar("jorex@example.com", "Bienvenido a java-prep");
    }

    public static void main(String[] args) {
        ejecutarConPerfil("dev");
        System.out.println();
        ejecutarConPerfil("prod");
    }
}
