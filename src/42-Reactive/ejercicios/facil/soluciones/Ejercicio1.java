import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

// Publisher simple que emite enteros del 1 al 10
public class Ejercicio1 {

    static class IntegerSubscriber implements Flow.Subscriber<Integer> {
        private Flow.Subscription subscription;
        private int total = 0;

        @Override
        public void onSubscribe(Flow.Subscription s) {
            this.subscription = s;
            s.request(Long.MAX_VALUE); // solicitar todos los elementos
        }

        @Override
        public void onNext(Integer item) {
            System.out.println("Recibido: " + item);
            total++;
        }

        @Override
        public void onError(Throwable t) {
            System.out.println("Error: " + t.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("Stream completado. Total recibidos: " + total);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Publisher de enteros 1-10 ===\n");

        try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
            IntegerSubscriber subscriber = new IntegerSubscriber();
            publisher.subscribe(subscriber);

            for (int i = 1; i <= 10; i++) {
                publisher.submit(i);
            }

            publisher.close();
            Thread.sleep(300); // esperar a que el subscriber procese
        }
    }
}
