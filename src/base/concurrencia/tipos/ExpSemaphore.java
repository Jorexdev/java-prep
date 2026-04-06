package base.concurrencia.tipos;

import java.util.concurrent.Semaphore;

/*
    SEMAPHORE

    ¿Qué es?
    Mecanismo de control de concurrencia basado en permisos.
    Controla cuántos hilos pueden acceder a un recurso simultáneamente.

    ¿Para qué sirve?
    Para limitar el paralelismo en secciones o recursos con capacidad máxima.
    Útil para simular pools de conexiones, throttling o rate limiting.

    ¿Cuándo usarlo?
    - Cuando quieres permitir N hilos simultáneos (no solo 1 como con synchronized).
    - Para limitar el acceso a un recurso con capacidad finita.

    ¿Cuándo NO usarlo?
    - Si solo necesitas exclusión mutua simple: Semaphore(1) funciona pero synchronized es más claro.

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre Semaphore(1) y synchronized?
    - ¿Qué significa el parámetro fair=true?
    - ¿Cómo simularías un pool de conexiones con Semaphore?
*/
public class ExpSemaphore {

    /*
        3 permisos: máximo 3 hilos pueden estar dentro de tarea() al mismo tiempo.
        fair=true: los hilos adquieren permisos en orden de llegada (FIFO).
    */
    private static final Semaphore sem = new Semaphore(3, true);

    private static void tarea(int id) {
        try {
            sem.acquire();  // toma un permiso, bloquea si no hay disponibles
            System.out.println("Tarea " + id + " ENTRA (permisos libres: " + sem.availablePermits() + ")");
            Thread.sleep(100);
            System.out.println("Tarea " + id + " SALE");
        } catch (InterruptedException ignored) {
        } finally {
            sem.release();  // devuelve el permiso para que otro hilo pueda entrar
        }
    }

    public static void main(String[] args) {
        // 10 hilos intentan entrar, pero solo 3 pueden estar dentro a la vez
        for (int i = 1; i <= 10; i++) {
            final int id = i;
            new Thread(() -> tarea(id)).start();
        }
    }
}
