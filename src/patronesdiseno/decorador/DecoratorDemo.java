package patronesdiseno.decorador;

public class DecoratorDemo {

    interface Notifier {
        void send(String msg);
    }

    // Componente base: implementación concreta del comportamiento principal.
    static class EmailNotifier implements Notifier {
        public void send(String msg) {
            System.out.println("Email: " + msg);
        }
    }

    // Decorador base: implementa la misma interfaz y delega al objeto envuelto.
    // Las subclases añaden comportamiento antes o después de la delegación.
    static abstract class NotifierDecorator implements Notifier {
        protected final Notifier wrappee;
        protected NotifierDecorator(Notifier n) { this.wrappee = n; }
    }

    // Cada decorador concreto añade su canal y luego delega al anterior.
    // El orden de apilado define el orden de ejecución.
    static class SmsDecorator extends NotifierDecorator {
        public SmsDecorator(Notifier n) { super(n); }
        public void send(String msg) {
            wrappee.send(msg);
            System.out.println("SMS: " + msg);
        }
    }

    static class SlackDecorator extends NotifierDecorator {
        public SlackDecorator(Notifier n) { super(n); }
        public void send(String msg) {
            wrappee.send(msg);
            System.out.println("Slack: " + msg);
        }
    }

    public static void main(String[] args) {
        Notifier soloEmail = new EmailNotifier();
        Notifier emailYSms = new SmsDecorator(soloEmail);
        Notifier todos     = new SlackDecorator(emailYSms);

        System.out.println("-- Solo email --");
        soloEmail.send("Pedido confirmado");

        System.out.println("-- Email + SMS --");
        emailYSms.send("Pedido enviado");

        System.out.println("-- Email + SMS + Slack --");
        todos.send("Pedido entregado");
    }
}
