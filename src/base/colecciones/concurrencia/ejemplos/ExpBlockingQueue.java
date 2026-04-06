package base.colecciones.concurrencia.ejemplos;

import java.util.concurrent.*;

public class ExpBlockingQueue {

    /*
        BlockingQueue es una interfaz de cola que soporta operaciones BLOQUEANTES:
        - put(E): bloquea si está llena (hasta que haya hueco)
        - take(): bloquea si está vacía (hasta que haya elemento)
        Implementaciones comunes:
          * ArrayBlockingQueue   → capacidad fija, respaldo por array.
          * LinkedBlockingQueue  → capacidad opcional (ilimitada por defecto), respaldo enlazado.
        Útil para productor-consumidor.
     */

    /*                             KEY FEATURES                            */

    /*
        - Operaciones con bloqueo (put/take) y con timeout (offer/ poll con tiempo).
        - Thread-safe.
        - Iteradores débilmente consistentes.
    */

    public static void main(String[] args) throws InterruptedException {

        // ArrayBlockingQueue con capacidad fija
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(2);

        // put(E): añade y BLOQUEA si está llena
        abq.put("Java");        // ok
        abq.put("Spring");      // ok
        // abq.put("Hibernate"); // se bloquearía (capacidad 2)

        // offer(E, timeout, unit): intenta añadir con tiempo de espera
        boolean added = abq.offer("Hibernate", 200, TimeUnit.MILLISECONDS); // false si no hay hueco en 200ms
        System.out.println("offer con timeout → " + added);

        // take(): obtiene y BLOQUEA si está vacía
        System.out.println("take() → " + abq.take()); // retira "Java"

        // peek(): ve cabeza sin eliminar
        System.out.println("peek() → " + abq.peek()); // Spring

        // remainingCapacity(): capacidad libre restante
        System.out.println("remainingCapacity → " + abq.remainingCapacity());

        // drainTo(Collection): drena elementos a otra colección
        var destino = new java.util.ArrayList<String>();
        int drenados = abq.drainTo(destino); // mueve "Spring"
        System.out.println("drainTo → " + drenados + ", destino: " + destino);

        // LinkedBlockingQueue (capacidad opcional; por defecto "ilimitada")
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

        // add(E) / offer(E): añaden sin bloquear (LBQ crecerá hasta memoria)
        lbq.add("Tarea-1");
        lbq.offer("Tarea-2");
        System.out.println("LBQ → " + lbq);

        // poll(timeout): obtiene con tiempo de espera (null si expira)
        String t = lbq.poll(100, TimeUnit.MILLISECONDS);
        System.out.println("poll(timeout) → " + t);

        // put/take también disponibles (bloqueantes)
        lbq.put("Tarea-3"); // añade
        System.out.println("take() → " + lbq.take()); // obtiene (o espera si vacío)

        // size / isEmpty
        System.out.println("size → " + lbq.size() + ", isEmpty → " + lbq.isEmpty());

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - offer(E): no bloqueante (devuelve false si llena).
        // - poll(): no bloqueante (devuelve null si vacía).
        // - remove(Object): elimina una ocurrencia si existe.
        // - toArray / iterator: iteradores débilmente consistentes en concurrencia.
        // - Otras implementaciones: PriorityBlockingQueue, DelayQueue, SynchronousQueue, LinkedTransferQueue.
    }
}
