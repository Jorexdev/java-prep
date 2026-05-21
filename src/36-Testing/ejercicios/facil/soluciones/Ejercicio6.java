import java.util.ArrayList;
import java.util.List;

public class Ejercicio6 {

    interface EmailService {
        void send(String to, String subject);
    }

    static class MockEmailService implements EmailService {
        record Llamada(String to, String subject) {}
        private final List<Llamada> llamadas = new ArrayList<>();

        @Override
        public void send(String to, String subject) {
            llamadas.add(new Llamada(to, subject));
        }

        int contarLlamadas() { return llamadas.size(); }
        Llamada getLlamada(int index) { return llamadas.get(index); }
    }

    static class ServicioRegistro {
        private final EmailService emailService;

        ServicioRegistro(EmailService emailService) {
            this.emailService = emailService;
        }

        void registrar(String nombre, String email) {
            if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre inválido");
            if (email == null || !email.contains("@")) throw new IllegalArgumentException("Email inválido");
            emailService.send(email, "Bienvenido, " + nombre);
        }
    }

    static void assertEquals(Object expected, Object actual, String nombre) {
        if (expected.equals(actual)) {
            System.out.println("PASS: " + nombre);
        } else {
            System.out.println("FAIL: " + nombre + " — esperado <" + expected + "> pero fue <" + actual + ">");
        }
    }

    public static void main(String[] args) {
        MockEmailService mock = new MockEmailService();
        ServicioRegistro svc = new ServicioRegistro(mock);

        svc.registrar("Elena", "elena@example.com");

        assertEquals(1, mock.contarLlamadas(), "send llamado exactamente una vez");
        assertEquals("elena@example.com", mock.getLlamada(0).to(), "send recibe el email correcto");
        assertEquals("Bienvenido, Elena", mock.getLlamada(0).subject(), "send recibe el asunto correcto");
    }
}
