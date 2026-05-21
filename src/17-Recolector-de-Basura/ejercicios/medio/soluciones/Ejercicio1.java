import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Reference Queue Demo ===");
        System.out.println();

        ReferenceQueue<String> queue = new ReferenceQueue<>();
        List<WeakReference<String>> refs = new ArrayList<>();

        // Crear 5 objetos con WeakReference registrada en la queue
        String[] strongRefs = new String[5];
        for (int i = 0; i < 5; i++) {
            strongRefs[i] = new String("objeto-" + i);
            refs.add(new WeakReference<>(strongRefs[i], queue));
            System.out.println("Creado: " + strongRefs[i]);
        }

        System.out.println();
        System.out.println("Verificando antes de eliminar referencias fuertes:");
        for (int i = 0; i < refs.size(); i++) {
            System.out.println("  refs[" + i + "].get() = " + refs.get(i).get());
        }

        System.out.println();
        System.out.println("Eliminando todas las referencias fuertes...");
        for (int i = 0; i < 5; i++) {
            strongRefs[i] = null;
        }

        System.gc();
        Thread.sleep(300);

        // Vaciar la queue
        System.out.println("Vaciando la ReferenceQueue...");
        int enqueued = 0;
        java.lang.ref.Reference<?> ref;
        while ((ref = queue.poll()) != null) {
            enqueued++;
            System.out.println("  Encolada referencia #" + enqueued + " -> get()=" + ref.get());
        }

        System.out.println();
        System.out.println("Referencias encoladas: " + enqueued + " de 5");
        System.out.println();
        System.out.println("=== Explicación ===");
        System.out.println("Cuando el GC recolecta un objeto con WeakReference registrada en");
        System.out.println("una ReferenceQueue, encola esa referencia automáticamente.");
        System.out.println("Así podemos reaccionar (liberar recursos) cuando el objeto desaparece.");
    }
}
