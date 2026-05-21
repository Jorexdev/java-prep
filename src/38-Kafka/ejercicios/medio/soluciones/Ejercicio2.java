import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Ejercicio2 {

    static class IdempotentBroker {
        private final Map<String, Long> lastSeq = new HashMap<>();

        boolean accept(String producerId, long seqNum, String message) {
            long expected = lastSeq.getOrDefault(producerId, -1L) + 1;
            if (seqNum != expected) {
                System.out.println("[BROKER] DUPLICADO rechazado — producerId=" + producerId
                        + " seqNum=" + seqNum + " (esperado=" + expected + ") msg='" + message + "'");
                return false;
            }
            lastSeq.put(producerId, seqNum);
            System.out.println("[BROKER] ACEPTADO — seqNum=" + seqNum + " msg='" + message + "'");
            return true;
        }
    }

    static class IdempotentProducer {
        private final String producerId;
        private final AtomicLong seqNum = new AtomicLong(0);
        private final IdempotentBroker broker;

        IdempotentProducer(String producerId, IdempotentBroker broker) {
            this.producerId = producerId;
            this.broker = broker;
        }

        void send(String message) {
            long seq = seqNum.getAndIncrement();
            System.out.println("[PRODUCER] enviando seqNum=" + seq + " msg='" + message + "'");
            broker.accept(producerId, seq, message);
        }

        void resend(long seq, String message) {
            System.out.println("[PRODUCER] reenviando seqNum=" + seq + " msg='" + message + "' (reintento)");
            broker.accept(producerId, seq, message);
        }
    }

    public static void main(String[] args) {
        IdempotentBroker broker = new IdempotentBroker();
        IdempotentProducer producer = new IdempotentProducer("producer-1", broker);

        System.out.println("[FASE 1] Envío normal de 5 mensajes");
        for (int i = 0; i < 5; i++) {
            producer.send("mensaje-" + (i + 1));
        }

        System.out.println("\n[FASE 2] Reenvío del mensaje 3 (seqNum=2) — simula red duplicada");
        producer.resend(2L, "mensaje-3");
    }
}
