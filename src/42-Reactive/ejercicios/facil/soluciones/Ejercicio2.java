import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

// Pipeline: strings → filter(longitud>3) → map(toUpperCase) → print
public class Ejercicio2 {

    // Processor: filtra strings de longitud > 3 y las convierte a mayúsculas
    static class FilterMapProcessor extends SubmissionPublisher<String>
            implements Flow.Processor<String, String> {

        private Flow.Subscription upstream;

        @Override
        public void onSubscribe(Flow.Subscription s) {
            this.upstream = s;
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(String item) {
            if (item.length() > 3) {
                System.out.println("  [Processor] '" + item + "' pasa el filtro → '" + item.toUpperCase() + "'");
                submit(item.toUpperCase());
            } else {
                System.out.println("  [Processor] '" + item + "' DESCARTADA (longitud=" + item.length() + ")");
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

    // Subscriber final: imprime el resultado
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
            System.out.println("  [Salida] Completado. " + count + " strings procesadas.");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Pipeline: filter(longitud>3) → map(toUpperCase) ===\n");

        List<String> entrada = List.of("hi", "java", "go", "reactive", "streams", "ok", "flow");
        System.out.println("Entrada: " + entrada);
        System.out.println();

        try (SubmissionPublisher<String> origen = new SubmissionPublisher<>()) {
            FilterMapProcessor processor = new FilterMapProcessor();
            PrintSubscriber destino = new PrintSubscriber();

            origen.subscribe(processor);
            processor.subscribe(destino);

            for (String s : entrada) {
                origen.submit(s);
            }

            origen.close();
            Thread.sleep(400);
        }
    }
}
