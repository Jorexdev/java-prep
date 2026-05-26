import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

public class ExpClosureCapture {

    // Campo de instancia: los lambdas pueden leer Y mutar campos (no son locales)
    private String estado = "ACTIVO";
    private int contadorInstancia = 0;

    public static void main(String[] args) {
        new ExpClosureCapture().ejecutar();
    }

    void ejecutar() {

        // ======================================
        // 1. CAPTURA DE VARIABLE LOCAL (effectively-final)
        // ======================================

        String mensaje = "evento recibido"; // effectively-final: nunca se reasigna
        Runnable handler = () -> System.out.println("Handler: " + mensaje);
        handler.run();

        // POR QUÉ no se puede mutar: la lambda puede vivir más que el stack frame
        // donde 'mensaje' fue creada. Si 'mensaje' mutara, el lambda vería un valor
        // inconsistente o ya liberado.

        // INCORRECTO — no compila:
        // int contador = 0;
        // Runnable r = () -> contador++;  // error: variable used in lambda should be effectively final

        // WORKAROUND con int[]: el array es effectively-final (misma referencia),
        // pero su contenido sí puede cambiar
        int[] contadorLocal = {0};
        Runnable incrementar = () -> contadorLocal[0]++;
        incrementar.run();
        incrementar.run();
        System.out.println("contadorLocal con int[]: " + contadorLocal[0]);

        // ======================================
        // 2. CAPTURA CON AtomicInteger (patrón idiomático para contadores en lambdas)
        // ======================================

        AtomicInteger atomic = new AtomicInteger(0);
        List<String> eventos = List.of("click", "hover", "click", "scroll", "click");

        Consumer<String> procesarEvento = e -> {
            if ("click".equals(e)) atomic.incrementAndGet(); // thread-safe, effectively-final reference
        };

        eventos.forEach(procesarEvento);
        System.out.println("Clics procesados: " + atomic.get()); // 3

        // ======================================
        // 3. CAPTURA DE 'this' — acceso a campos de la instancia
        // ======================================

        // El lambda captura implícitamente 'this' cuando accede a campos de instancia
        Supplier<String> leerEstado = () -> this.estado; // 'this' = la instancia de ExpClosureCapture
        System.out.println("Estado inicial: " + leerEstado.get());

        // Los campos de instancia SÍ son mutables desde el lambda (no son locales)
        Runnable cambiarEstado = () -> this.estado = "INACTIVO";
        cambiarEstado.run();
        System.out.println("Estado tras lambda: " + leerEstado.get());

        // ======================================
        // 4. ESTADO EXTERNO CAPTURADO — simulación de event handler
        // ======================================

        List<String> log = new ArrayList<>(); // effectively-final reference, contenido mutable
        String contexto = "UI";               // effectively-final, no se reasigna

        // El lambda "recuerda" tanto 'log' como 'contexto' del scope donde fue creado
        Consumer<String> logHandler = evento ->
                log.add("[" + contexto + "] " + evento);

        logHandler.accept("boton_click");
        logHandler.accept("form_submit");

        System.out.println("Log capturado: " + log);

        // ======================================
        // 5. CONTADOR DE INSTANCIA — campo mutable vía this
        // ======================================

        // Útil en frameworks donde el lambda se llama múltiples veces (listeners, callbacks)
        Runnable tick = () -> contadorInstancia++;
        tick.run();
        tick.run();
        tick.run();
        System.out.println("Ticks de instancia: " + contadorInstancia);
    }
}
