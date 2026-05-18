import java.time.LocalDateTime;
import java.util.List;
public class Ejercicio2 {
    abstract static class Notificacion {
        protected final String destinatario;
        protected final String asunto;
        private final LocalDateTime timestamp = LocalDateTime.now();
        Notificacion(String destinatario, String asunto) {
            this.destinatario = destinatario; this.asunto = asunto;
        }
        abstract void enviar();
        void log() {
            System.out.println("[LOG " + timestamp.toLocalTime() + "] " + getClass().getSimpleName() +
                               " → " + destinatario + ": " + asunto);
        }
    }
    static class NotificacionEmail extends Notificacion {
        NotificacionEmail(String dest, String asunto) { super(dest, asunto); }
        @Override void enviar() { System.out.println("[EMAIL] SMTP → " + destinatario + ": " + asunto); }
    }
    static class NotificacionSMS extends Notificacion {
        NotificacionSMS(String dest, String asunto) { super(dest, asunto); }
        @Override void enviar() { System.out.println("[SMS] → " + destinatario + ": " + asunto); }
    }
    static class NotificacionPush extends Notificacion {
        NotificacionPush(String dest, String asunto) { super(dest, asunto); }
        @Override void enviar() { System.out.println("[PUSH] → " + destinatario + ": " + asunto); }
    }
    public static void main(String[] args) {
        List<Notificacion> notificaciones = List.of(
            new NotificacionEmail("user@example.com", "Bienvenido"),
            new NotificacionSMS("+34600000000", "Código: 1234"),
            new NotificacionPush("device-token-abc", "Nuevo mensaje")
        );
        notificaciones.forEach(n -> { n.enviar(); n.log(); System.out.println(); });
    }
}
