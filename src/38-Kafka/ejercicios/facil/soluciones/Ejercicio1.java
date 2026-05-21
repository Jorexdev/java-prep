import java.util.LinkedList;
import java.util.Queue;

public class Ejercicio1 {

    interface Callback {
        void onCompletion(boolean success, Exception ex);
    }

    static class Topic {
        final String name;
        final Queue<String> messages = new LinkedList<>();

        Topic(String name) {
            this.name = name;
        }
    }

    static class KafkaProducer {
        private int sendCount = 0;

        void send(Topic topic, String message, Callback callback) {
            sendCount++;
            // @ProducerConfig — simula fallo en el tercer envío
            if (sendCount == 3) {
                callback.onCompletion(false, new RuntimeException("Broker timeout en mensaje: " + message));
                return;
            }
            topic.messages.offer(message);
            callback.onCompletion(true, null);
        }
    }

    public static void main(String[] args) {
        Topic pedidos = new Topic("pedidos");
        KafkaProducer producer = new KafkaProducer();

        for (int i = 1; i <= 5; i++) {
            final String msg = "pedido-" + i;
            producer.send(pedidos, msg, (success, ex) -> {
                if (success) {
                    System.out.println("[ACK] Mensaje enviado: " + msg);
                } else {
                    System.out.println("[ERROR] Fallo al enviar '" + msg + "': " + ex.getMessage());
                }
            });
        }

        System.out.println("\nMensajes en topic '" + pedidos.name + "': " + pedidos.messages);
    }
}
