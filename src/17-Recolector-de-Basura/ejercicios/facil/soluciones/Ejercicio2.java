import java.lang.ref.WeakReference;

public class Ejercicio2 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== WeakReference vs Strong Reference ===");
        System.out.println();

        // --- Referencia fuerte ---
        String strong = new String("soy fuerte");
        System.out.println("Strong reference antes de gc: " + strong);
        System.gc();
        Thread.sleep(100);
        System.out.println("Strong reference después de gc: " + strong);
        System.out.println("La referencia fuerte nunca se pierde.");
        System.out.println();

        // --- WeakReference ---
        String obj = new String("soy débil");
        WeakReference<String> weakRef = new WeakReference<>(obj);

        System.out.println("WeakRef antes de eliminar strong ref: " + weakRef.get());

        // Eliminar la única referencia fuerte
        obj = null;

        System.out.println("WeakRef después de obj=null (antes de gc): " + weakRef.get());

        // Pedir al GC que recolecte
        System.gc();
        Thread.sleep(200);

        String result = weakRef.get();
        if (result == null) {
            System.out.println("WeakRef después de gc: null -> objeto recolectado");
        } else {
            System.out.println("WeakRef después de gc: " + result + " (GC no ejecutó aún)");
        }

        System.out.println();
        System.out.println("Conclusión: WeakReference no impide la recolección.");
        System.out.println("Útil para: caches que no deben retener objetos innecesariamente.");
    }
}
