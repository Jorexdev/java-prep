public class Ejercicio5 {

    interface ServicioNotificacion {
        void enviar(String mensaje);
    }

    static class EmailService implements ServicioNotificacion {
        @Override public void enviar(String mensaje) {
            System.out.println("Email: " + mensaje);
        }
    }

    static class SmsService implements ServicioNotificacion {
        @Override public void enviar(String mensaje) {
            System.out.println("SMS: " + mensaje);
        }
    }

    static class Notificador {
        private final ServicioNotificacion servicio;

        Notificador(ServicioNotificacion servicio) {
            this.servicio = servicio;
        }

        void notificar(String mensaje) {
            servicio.enviar(mensaje);
        }
    }

    public static void main(String[] args) {
        Notificador porEmail = new Notificador(new EmailService());
        Notificador porSms   = new Notificador(new SmsService());

        porEmail.notificar("Pedido confirmado");
        porSms.notificar("Código de verificación: 4821");
    }
}
