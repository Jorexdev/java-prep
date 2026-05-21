// Ejercicio 4 — Test profile
// EmailService real vs FakeEmailService para perfil "test".
// ServiceContainer usa el servicio correcto según perfil.

import java.util.ArrayList;
import java.util.List;

public class Ejercicio4 {

    // Contrato del servicio
    interface EmailService {
        void sendWelcomeEmail(String to, String name);
        void sendPasswordReset(String to, String token);
        List<String> getSentEmails(); // para verificación en tests
    }

    // Implementación real (perfil "dev" y "prod")
    // @Component
    // @Profile("!test")
    static class RealEmailService implements EmailService {
        private final List<String> sent = new ArrayList<>();

        @Override
        public void sendWelcomeEmail(String to, String name) {
            String msg = String.format("SMTP → Enviando email de bienvenida a %s (%s)", to, name);
            System.out.println("[RealEmailService] " + msg);
            System.out.println("[RealEmailService]   Conectando a SMTP server...");
            System.out.println("[RealEmailService]   Autenticando con credenciales...");
            System.out.println("[RealEmailService]   Email enviado con éxito ✓");
            sent.add("welcome:" + to);
        }

        @Override
        public void sendPasswordReset(String to, String token) {
            System.out.printf("[RealEmailService] SMTP → Enviando reset password a %s (token: %s...)%n",
                to, token.substring(0, 8));
            System.out.println("[RealEmailService]   Email de reset enviado ✓");
            sent.add("reset:" + to);
        }

        @Override
        public List<String> getSentEmails() { return sent; }
    }

    // Implementación fake para tests — NO envía emails reales
    // @Component
    // @Profile("test")
    static class FakeEmailService implements EmailService {
        private final List<String> sent = new ArrayList<>();

        @Override
        public void sendWelcomeEmail(String to, String name) {
            System.out.printf("[FakeEmailService] SIMULADO — welcome email para %s (%s) [NO enviado]%n", to, name);
            sent.add("welcome:" + to);
        }

        @Override
        public void sendPasswordReset(String to, String token) {
            System.out.printf("[FakeEmailService] SIMULADO — reset email para %s [NO enviado]%n", to);
            sent.add("reset:" + to);
        }

        @Override
        public List<String> getSentEmails() { return sent; }
    }

    // Servicio de negocio que usa EmailService
    static class UserService {
        private final EmailService emailService;

        UserService(EmailService emailService) {
            this.emailService = emailService;
        }

        void registerUser(String email, String name) {
            System.out.println("[UserService] Registrando usuario: " + name);
            // ... lógica de negocio ...
            emailService.sendWelcomeEmail(email, name);
        }

        void resetPassword(String email) {
            System.out.println("[UserService] Solicitando reset de contraseña para: " + email);
            String token = "tok-" + System.nanoTime();
            emailService.sendPasswordReset(email, token);
        }
    }

    // Contenedor que simula el contexto de Spring
    static class ServiceContainer {
        private final EmailService emailService;
        private final UserService userService;

        ServiceContainer(String profile) {
            this.emailService = "test".equals(profile)
                ? new FakeEmailService()
                : new RealEmailService();
            this.userService = new UserService(emailService);
            System.out.printf("[Container] Perfil '%s' → usando %s%n%n",
                profile, emailService.getClass().getSimpleName());
        }

        UserService getUserService() { return userService; }
        EmailService getEmailService() { return emailService; }
    }

    static void runScenario(String profile) {
        System.out.println("=".repeat(50));
        System.out.println("Escenario con perfil: " + profile);
        System.out.println("=".repeat(50));

        ServiceContainer container = new ServiceContainer(profile);

        container.getUserService().registerUser("alice@example.com", "Alice");
        System.out.println();
        container.getUserService().resetPassword("bob@example.com");
        System.out.println();

        List<String> sent = container.getEmailService().getSentEmails();
        System.out.println("Emails registrados en memoria: " + sent);
        System.out.printf("¿Se enviaron emails reales? %s%n%n",
            (container.getEmailService() instanceof RealEmailService) ? "SÍ ✓" : "NO (fake) ✓");
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 4 — Test profile ===\n");

        runScenario("dev");
        runScenario("test");

        System.out.println("--- Ventaja del test profile ---");
        System.out.println("Los tests unitarios/integración usan FakeEmailService:");
        System.out.println("  • No se envían emails reales durante los tests");
        System.out.println("  • Se puede verificar que se llamó con los parámetros correctos");
        System.out.println("  • Los tests son rápidos (sin I/O de red)");
        System.out.println("  • En Spring: @SpringBootTest activa automáticamente @Profile(\"test\")");
    }
}
