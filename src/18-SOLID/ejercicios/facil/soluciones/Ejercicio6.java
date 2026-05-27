public class Ejercicio6 {

    // SRP: Notificacion solo tiene datos, sin lógica de envío
    static class Notificacion {
        private final String asunto;
        private final String cuerpo;

        Notificacion(String asunto, String cuerpo) {
            this.asunto = asunto;
            this.cuerpo = cuerpo;
        }

        String getAsunto() { return asunto; }
        String getCuerpo() { return cuerpo; }
    }

    // ISP: Destinatario expone solo lo que el enviador necesita
    interface Destinatario {
        String obtenerContacto();
    }

    // LSP: Usuario y Empresa son intercambiables como Destinatario
    static class Usuario implements Destinatario {
        private final String nombre;
        private final String email;

        Usuario(String nombre, String email) {
            this.nombre = nombre;
            this.email = email;
        }

        @Override public String obtenerContacto() { return email; }
        @Override public String toString() { return "Usuario(" + nombre + ")"; }
    }

    static class Empresa implements Destinatario {
        private final String razonSocial;
        private final String emailCorporativo;

        Empresa(String razonSocial, String emailCorporativo) {
            this.razonSocial = razonSocial;
            this.emailCorporativo = emailCorporativo;
        }

        @Override public String obtenerContacto() { return emailCorporativo; }
        @Override public String toString() { return "Empresa(" + razonSocial + ")"; }
    }

    // DIP: el sistema de notificaciones depende de esta abstracción, no de canales concretos
    interface EnviadorNotificacion {
        void enviar(Destinatario destinatario, Notificacion notificacion);
        String canal();
    }

    // OCP: añadir un canal nuevo no requiere modificar nada existente, solo implementar esta interfaz
    static class EnviadorEmail implements EnviadorNotificacion {
        @Override
        public void enviar(Destinatario destinatario, Notificacion notificacion) {
            System.out.println("[EMAIL → " + destinatario.obtenerContacto() + "] "
                    + notificacion.getAsunto() + ": " + notificacion.getCuerpo());
        }

        @Override public String canal() { return "Email"; }
    }

    static class EnviadorSMS implements EnviadorNotificacion {
        @Override
        public void enviar(Destinatario destinatario, Notificacion notificacion) {
            System.out.println("[SMS → " + destinatario.obtenerContacto() + "] "
                    + notificacion.getCuerpo());
        }

        @Override public String canal() { return "SMS"; }
    }

    // OCP: nuevo canal añadido sin tocar ninguna clase existente
    static class EnviadorPush implements EnviadorNotificacion {
        @Override
        public void enviar(Destinatario destinatario, Notificacion notificacion) {
            System.out.println("[PUSH → " + destinatario.obtenerContacto() + "] "
                    + notificacion.getAsunto());
        }

        @Override public String canal() { return "Push"; }
    }

    // SRP: SistemaNotificaciones solo orquesta el envío, no sabe nada del canal concreto
    static class SistemaNotificaciones {
        private final java.util.List<EnviadorNotificacion> enviadores;

        SistemaNotificaciones(java.util.List<EnviadorNotificacion> enviadores) {
            this.enviadores = enviadores;
        }

        void notificar(Destinatario destinatario, Notificacion notificacion) {
            for (EnviadorNotificacion enviador : enviadores) {
                enviador.enviar(destinatario, notificacion);
            }
        }
    }

    public static void main(String[] args) {
        Notificacion bienvenida = new Notificacion(
                "Bienvenido",
                "Tu cuenta ha sido activada correctamente."
        );

        // LSP: tanto Usuario como Empresa se usan de forma intercambiable
        Destinatario usuario  = new Usuario("Ana García", "ana@example.com");
        Destinatario empresa  = new Empresa("Acme S.L.", "info@acme.es");

        // Sistema con dos canales iniciales
        java.util.List<EnviadorNotificacion> canalesIniciales = java.util.List.of(
                new EnviadorEmail(),
                new EnviadorSMS()
        );
        SistemaNotificaciones sistema = new SistemaNotificaciones(canalesIniciales);

        System.out.println("=== Notificaciones con Email y SMS ===");
        sistema.notificar(usuario, bienvenida);
        sistema.notificar(empresa, bienvenida);

        // OCP: añadir Push no modifica SistemaNotificaciones ni los enviadores existentes
        java.util.List<EnviadorNotificacion> canalesConPush = java.util.List.of(
                new EnviadorEmail(),
                new EnviadorSMS(),
                new EnviadorPush()
        );
        SistemaNotificaciones sistemaConPush = new SistemaNotificaciones(canalesConPush);

        System.out.println("\n=== Notificaciones con Push añadido (sin modificar código existente) ===");
        sistemaConPush.notificar(usuario, bienvenida);
    }
}
