import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

// FizzBuzzProcessor: transforma enteros y filtra los que contienen "z"
public class Ejercicio6 {

    static class FizzBuzzProcessor extends SubmissionPublisher<String>
            implements Flow.Processor<Integer, String> {

        private Flow.Subscription subscription;
        private int posicion = 0;

        @Override
        public void onSubscribe(Flow.Subscription s) {
            this.subscription = s;
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(Integer n) {
            posicion++;

            // Transformar según reglas FizzBuzz
            String transformado;
            if (n % 15 == 0) transformado = "FizzBuzz";
            else if (n % 3 == 0) transformado = "Fizz";
            else if (n % 5 == 0) transformado = "Buzz";
            else transformado = String.valueOf(n);

            // Filtrar: solo pasan los que contienen "z"
            if (transformado.contains("z")) {
                System.out.println("  [Processor] " + n + " → '" + transformado + "' (posición=" + posicion + ") PASA");
                submit(transformado + " (n=" + n + ")"); // incluir n original para mostrar posición
            } else {
                System.out.println("  [Processor] " + n + " → '" + transformado + "' — descartado");
            }
        }

        @Override
        public void onError(Throwable t) {
            closeExceptionally(t);
        }

        @Override
        public void onComplete() {
            close();
        }
    }

    static class PrintSubscriber implements Flow.Subscriber<String> {
        private int count = 0;

        @Override
        public void onSubscribe(Flow.Subscription s) {
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(String item) {
            count++;
            System.out.println("  [Salida] " + count + ". " + item);
        }

        @Override
        public void onError(Throwable t) {
            System.out.println("  [Salida] Error: " + t.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("  [Salida] Completado. " + count + " items con 'z' llegaron al downstream.");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== FizzBuzzProcessor: transforma y filtra (solo con 'z') ===\n");
        System.out.println("Reglas: n%15=FizzBuzz, n%3=Fizz, n%5=Buzz, resto=n");
        System.out.println("Filtro: solo pasan los que contienen la letra 'z'\n");

        try (SubmissionPublisher<Integer> origen = new SubmissionPublisher<>()) {
            FizzBuzzProcessor processor = new FizzBuzzProcessor();
            PrintSubscriber destino = new PrintSubscriber();

            origen.subscribe(processor);
            processor.subscribe(destino);

            System.out.println("--- Procesando 1 al 20 ---");
            for (int i = 1; i <= 20; i++) {
                origen.submit(i);
            }

            origen.close();
            Thread.sleep(400);
        }

        System.out.println();
        System.out.println("=== Esperados con 'z': Fizz(3,6,9,12,18), Buzz(5,10,20), FizzBuzz(15) ===");
        System.out.println("Nota: FizzBuzz tiene 'z' (en 'Fizz'). Buzz tiene 'z'. Fizz tiene 'z'.");
    }
}
