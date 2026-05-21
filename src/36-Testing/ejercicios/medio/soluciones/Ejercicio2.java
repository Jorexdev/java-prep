import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio2 {

    static class ServicioNotificaciones {
        private final List<String> log = new ArrayList<>();

        void enviarSms(String telefono, String mensaje) {
            String entrada = "SMS → " + telefono + ": " + mensaje;
            log.add(entrada);
            System.out.println("  [REAL] " + entrada);
        }

        void enviarEmail(String email, String asunto) {
            String entrada = "EMAIL → " + email + ": " + asunto;
            log.add(entrada);
            System.out.println("  [REAL] " + entrada);
        }

        List<String> getLog() { return log; }
    }

    static class Spy {
        private final ServicioNotificaciones real;
        private final Map<String, Runnable> overrides = new HashMap<>();
        private final List<String> llamadasInterceptadas = new ArrayList<>();
        private String ultimoEmailTo;
        private String ultimoEmailAsunto;

        Spy(ServicioNotificaciones real) { this.real = real; }

        void overrideEmail() {
            overrides.put("enviarEmail", null);
        }

        void enviarSms(String telefono, String mensaje) {
            real.enviarSms(telefono, mensaje);
        }

        void enviarEmail(String email, String asunto) {
            if (overrides.containsKey("enviarEmail")) {
                ultimoEmailTo     = email;
                ultimoEmailAsunto = asunto;
                llamadasInterceptadas.add("enviarEmail(" + email + ", " + asunto + ")");
                System.out.println("  [SPY interceptado] enviarEmail → " + email);
            } else {
                real.enviarEmail(email, asunto);
            }
        }

        int contarInterceptadas() { return llamadasInterceptadas.size(); }
        String getUltimoEmailTo() { return ultimoEmailTo; }
        String getUltimoEmailAsunto() { return ultimoEmailAsunto; }
    }

    static void assertEquals(Object expected, Object actual, String nombre) {
        if (expected.equals(actual)) {
            System.out.println("PASS: " + nombre);
        } else {
            System.out.println("FAIL: " + nombre + " — esperado <" + expected + "> pero fue <" + actual + ">");
        }
    }

    public static void main(String[] args) {
        ServicioNotificaciones real = new ServicioNotificaciones();
        Spy spy = new Spy(real);
        spy.overrideEmail();

        System.out.println("--- Ejecutando notificaciones ---");
        spy.enviarSms("600123456", "Tu pedido ha sido enviado");
        spy.enviarEmail("cliente@example.com", "Confirmación de pedido #42");
        System.out.println();

        assertEquals(1, real.getLog().size(), "SMS ejecutado en implementación real (1 entrada en log real)");
        assertEquals(1, spy.contarInterceptadas(), "enviarEmail interceptado exactamente una vez");
        assertEquals("cliente@example.com", spy.getUltimoEmailTo(), "email interceptado con destinatario correcto");
        assertEquals("Confirmación de pedido #42", spy.getUltimoEmailAsunto(), "email interceptado con asunto correcto");

        boolean emailNoEsReal = real.getLog().stream().noneMatch(e -> e.startsWith("EMAIL"));
        if (emailNoEsReal) {
            System.out.println("PASS: el email NO llegó a la implementación real");
        } else {
            System.out.println("FAIL: el email llegó a la implementación real cuando no debería");
        }
    }
}
