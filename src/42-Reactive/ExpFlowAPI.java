import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

// Java Flow API (java.util.concurrent.Flow) — JDK 9+
// Las 4 interfaces: Publisher, Subscriber, Subscription, Processor
public class ExpFlowAPI {

    // Subscriber personalizado: imprime elementos y solicita uno a uno (backpressure manual)
    static class ImpresionSubscriber implements Flow.Subscriber<String> {

        private Flow.Subscription subscription;
        private final String nombre;

        ImpresionSubscriber(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public void onSubscribe(Flow.Subscription s) {
            this.subscription = s;
            System.out.println("[" + nombre + "] Suscrito. Solicitando primer elemento.");
            s.request(1); // solicitar solo 1 — backpressure manual
        }

        @Override
        public void onNext(String item) {
            System.out.println("[" + nombre + "] Recibido: " + item);
            // Simular procesamiento lento
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            subscription.request(1); // solicitar siguiente cuando estemos listos
        }

        @Override
        public void onError(Throwable t) {
            System.out.println("[" + nombre + "] Error: " + t.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("[" + nombre + "] Stream completado.");
        }
    }

    // Processor: transforma String a mayúsculas en el medio de la cadena
    static class MayusculasProcessor extends SubmissionPublisher<String>
            implements Flow.Processor<String, String> {

        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription s) {
            this.subscription = s;
            s.request(Long.MAX_VALUE); // el processor solicita todos los elementos del upstream
        }

        @Override
        public void onNext(String item) {
            // transformar y publicar al downstream
            submit(item.toUpperCase());
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

    public static void main(String[] args) throws Exception {

        System.out.println("=== Java Flow API — Publisher / Subscriber ===\n");

        // --- Ejemplo 1: SubmissionPublisher con un subscriber ---
        System.out.println("-- Ejemplo 1: SubmissionPublisher simple --");
        try (SubmissionPublisher<String> publisher = new SubmissionPublisher<>()) {
            ImpresionSubscriber sub = new ImpresionSubscriber("Sub-A");
            publisher.subscribe(sub);

            List<String> datos = List.of("java", "reactive", "streams", "flow", "api");
            for (String d : datos) {
                publisher.submit(d);
            }
            // esperar a que el subscriber procese (SubmissionPublisher es async)
            publisher.close();
            Thread.sleep(600); // tiempo para que el subscriber termine
        }

        System.out.println();

        // --- Ejemplo 2: Múltiples subscribers independientes ---
        System.out.println("-- Ejemplo 2: Múltiples subscribers --");
        try (SubmissionPublisher<String> publisher = new SubmissionPublisher<>()) {
            publisher.subscribe(new ImpresionSubscriber("Sub-1"));
            publisher.subscribe(new ImpresionSubscriber("Sub-2"));

            publisher.submit("mensaje-A");
            publisher.submit("mensaje-B");
            publisher.close();
            Thread.sleep(500);
        }

        System.out.println();

        // --- Ejemplo 3: Pipeline con Processor ---
        System.out.println("-- Ejemplo 3: Pipeline Publisher → Processor → Subscriber --");
        try (SubmissionPublisher<String> origen = new SubmissionPublisher<>()) {
            MayusculasProcessor processor = new MayusculasProcessor();
            ImpresionSubscriber destino = new ImpresionSubscriber("Sub-Final");

            // construir pipeline: origen → processor → destino
            origen.subscribe(processor);
            processor.subscribe(destino);

            origen.submit("hola");
            origen.submit("mundo");
            origen.submit("reactor");

            origen.close();
            Thread.sleep(400);
        }

        System.out.println();
        System.out.println("=== Resumen ===");
        System.out.println("Publisher<T>    — produce elementos (SubmissionPublisher es la impl JDK)");
        System.out.println("Subscriber<T>   — onSubscribe → onNext* → (onError | onComplete)");
        System.out.println("Subscription    — request(n) controla el flujo, cancel() cancela");
        System.out.println("Processor<T,R>  — extiende Publisher+Subscriber, transforma en medio");
    }
}
