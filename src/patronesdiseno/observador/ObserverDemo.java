package patronesdiseno.observador;

import java.util.ArrayList;
import java.util.List;

/*
    PATRÓN OBSERVER - Comportamiento

    ¿Qué es?
    Define una relación de suscripción entre objetos: cuando un sujeto
    cambia de estado, notifica automáticamente a todos sus observadores.

    ¿Para qué sirve?
    Para desacoplar al emisor de eventos de los que reaccionan a ellos.
    El sujeto no sabe quiénes son sus observadores ni qué hacen con el evento.

    ¿Cuándo usarlo?
    - Cuando un cambio en un objeto debe disparar acciones en otros objetos.
    - Sistemas de notificaciones, eventos de UI, buses de dominio.
    - Cuando no quieres dependencia directa entre emisor y receptor.

    ¿Cuándo NO usarlo?
    - Si los observadores son pocos y fijos, una llamada directa es más clara.
    - Cuando el orden de notificación importa y no puedes garantizarlo.

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre Observer y el patrón Pub/Sub?
    - ¿Cómo evitas un memory leak con observadores que no se desuscriben?
    - ¿Cómo se relaciona con los eventos de Spring (@EventListener)?
    - ¿Cómo lo implementarías con lambdas en vez de interfaces?
*/
public class ObserverDemo {

    /*
        Contrato del observador.
        El sujeto solo conoce esta abstracción, nunca la implementación concreta.
        Así puedes agregar nuevos tipos de observadores sin tocar el sujeto.
    */
    interface Observer {
        void update(String evento);
    }

    /*
        Cada observador decide qué hacer con el evento.
        El sujeto no sabe ni le importa lo que hacen aquí.
    */
    static class LogObserver implements Observer {
        public void update(String evento) {
            System.out.println("[LOG] " + evento);
        }
    }

    static class EmailObserver implements Observer {
        private final String destinatario;

        EmailObserver(String destinatario) {
            this.destinatario = destinatario;
        }

        public void update(String evento) {
            System.out.println("[EMAIL = " + destinatario + "] " + evento);
        }
    }

    /*
        El sujeto gestiona la lista de observadores y los notifica al publicar un evento.
        Expone suscribir/desuscribir para que los observadores entren y salgan
        en tiempo de ejecución sin modificar esta clase.
    */
    static class SistemaPedidos {
        private final List<Observer> observadores = new ArrayList<>();

        public void suscribir(Observer o)   { observadores.add(o); }
        public void desuscribir(Observer o) { observadores.remove(o); }

        public void nuevoPedido(String pedido) {
            System.out.println("\n[PEDIDO] " + pedido);
            observadores.forEach(o -> o.update(pedido));
        }
    }

    public static void main(String[] args) {
        SistemaPedidos sistema = new SistemaPedidos();

        Observer log   = new LogObserver();
        Observer email = new EmailObserver("admin@tienda.com");

        // Ambos observadores activos
        sistema.suscribir(log);
        sistema.suscribir(email);
        sistema.nuevoPedido("Pedido #101 - Laptop");

        // Desuscribimos el log, solo queda el email
        sistema.desuscribir(log);
        sistema.nuevoPedido("Pedido #102 - Monitor");
    }

}
