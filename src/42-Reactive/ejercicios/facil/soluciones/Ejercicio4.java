import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

// SubmissionPublisher con múltiples subscribers independientes
public class Ejercicio4 {

    static class LabeledSubscriber implements Flow.Subscriber<String> {
        private final String label;
        private int count = 0;
        private Flow.Subscription subscription;

        LabeledSubscriber(String label) {
            this.label = label;
        }

        @Override
        public void onSubscribe(Flow.Subscription s) {
            this.subscription = s;
            System.out.println("[" + label + "] Suscrito.");
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(String item) {
            count++;
            System.out.println("[" + label + "] onNext: " + item);
        }

        @Override
        public void onError(Throwable t) {
            System.out.println("[" + label + "] Error: " + t.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("[" + label + "] Completado. Total recibidos: " + count);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== SubmissionPublisher con 3 subscribers ===\n");

        try (SubmissionPublisher<String> publisher = new SubmissionPublisher<>()) {
            // Tres subscribers independientes
            publisher.subscribe(new LabeledSubscriber("Sub-A"));
            publisher.subscribe(new LabeledSubscriber("Sub-B"));
            publisher.subscribe(new LabeledSubscriber("Sub-C"));

            System.out.println("Subscribers registrados: " + publisher.getNumberOfSubscribers());
            System.out.println();

            String[] mensajes = {"msg-1", "msg-2", "msg-3", "msg-4", "msg-5"};
            for (String msg : mensajes) {
                System.out.println("[Publisher] Emitiendo: " + msg);
                publisher.submit(msg);
            }

            publisher.close();
            Thread.sleep(500); // esperar procesamiento

            System.out.println();
            System.out.println("=== Conclusión ===");
            System.out.println("Cada subscriber recibió los mismos 5 mensajes de forma independiente.");
            System.out.println("SubmissionPublisher entrega a cada subscriber en su propio hilo.");
        }
    }
}
