import java.util.*;

public class Ejercicio4 {

    static class HeapObject {
        String name;
        int survivals;

        HeapObject(String name) {
            this.name = name;
            this.survivals = 0;
        }

        @Override
        public String toString() {
            return name + "(s=" + survivals + ")";
        }
    }

    static List<HeapObject> youngGen = new ArrayList<>();
    static List<HeapObject> oldGen = new ArrayList<>();
    static int youngCapacity = 10;
    static int oldCapacity = 50;
    static int objectCounter = 0;

    static void allocate(int count) {
        for (int i = 0; i < count; i++) {
            if (youngGen.size() < youngCapacity) {
                youngGen.add(new HeapObject("Obj" + (++objectCounter)));
            } else {
                // Young lleno: trigger minor GC antes de añadir
                minorGc();
                youngGen.add(new HeapObject("Obj" + (++objectCounter)));
            }
        }
    }

    static void minorGc() {
        List<HeapObject> survivors = new ArrayList<>();
        int collected = 0;

        for (HeapObject obj : youngGen) {
            obj.survivals++;
            if (obj.survivals >= 2) {
                // Promover a Old generation
                if (oldGen.size() < oldCapacity) {
                    oldGen.add(obj);
                }
                // Si Old está lleno, simplemente se descarta (simplificación)
            } else {
                // Sobrevive en Young
                survivors.add(obj);
            }
        }
        collected = youngGen.size() - survivors.size() - (oldGen.size() > 0 ? 0 : 0);
        youngGen.clear();
        youngGen.addAll(survivors);
    }

    public static void main(String[] args) {
        System.out.println("=== Simulación de Generaciones GC ===");
        System.out.println("Young capacity: " + youngCapacity + " | Old capacity: " + oldCapacity);
        System.out.println();

        for (int cycle = 1; cycle <= 5; cycle++) {
            allocate(8);
            minorGc();

            System.out.println("--- Ciclo " + cycle + " ---");
            System.out.println("Young Gen (" + youngGen.size() + "/" + youngCapacity + "): " + youngGen);
            System.out.println("Old Gen   (" + oldGen.size() + "/" + oldCapacity + "): " + oldGen);
            System.out.println();
        }

        System.out.println("=== Resumen Final ===");
        System.out.println("Total objetos creados: " + objectCounter);
        System.out.println("Objetos en Young: " + youngGen.size());
        System.out.println("Objetos en Old:   " + oldGen.size());
    }
}
