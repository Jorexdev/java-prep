import java.util.*;

public class Ejercicio1 {

    static class HeapObject {
        final String name;
        int age; // número de minor GCs sobrevividos

        HeapObject(String name) {
            this.name = name;
            this.age = 0;
        }

        @Override public String toString() { return name + "(age=" + age + ")"; }
    }

    static class GenerationalGC {
        private final List<HeapObject> young = new ArrayList<>();
        private final List<HeapObject> old = new ArrayList<>();
        private final int youngCapacity;
        private final int oldCapacity;
        private final int promotionThreshold = 2;
        private int gcMinorCount = 0;
        private int gcMajorCount = 0;

        // Nombres de objetos que deben sobrevivir (no son garbage)
        private final Set<String> liveObjects;

        GenerationalGC(int youngCapacity, int oldCapacity, Set<String> liveObjects) {
            this.youngCapacity = youngCapacity;
            this.oldCapacity = oldCapacity;
            this.liveObjects = liveObjects;
        }

        void allocate(String name) {
            if (young.size() >= youngCapacity) {
                minorGc();
            }
            young.add(new HeapObject(name));
            System.out.println("  [ALLOC] " + name + " -> Young");
        }

        void minorGc() {
            gcMinorCount++;
            System.out.println("\n  === Minor GC #" + gcMinorCount + " ===");
            List<HeapObject> survivors = new ArrayList<>();
            int collected = 0;
            int promoted = 0;

            for (HeapObject obj : young) {
                if (liveObjects.contains(obj.name)) {
                    obj.age++;
                    if (obj.age >= promotionThreshold && old.size() < oldCapacity) {
                        old.add(obj);
                        promoted++;
                        System.out.println("  [PROMOTE] " + obj + " -> Old");
                    } else {
                        survivors.add(obj);
                    }
                } else {
                    collected++;
                    System.out.println("  [COLLECT] " + obj.name + " (garbage)");
                }
            }

            young.clear();
            young.addAll(survivors);
            System.out.println("  Minor GC: " + collected + " recogidos, " + promoted + " promovidos");
        }

        void majorGc() {
            gcMajorCount++;
            System.out.println("\n  === Major GC #" + gcMajorCount + " (Full GC) ===");
            minorGc();

            int collected = 0;
            old.removeIf(obj -> {
                if (!liveObjects.contains(obj.name)) {
                    System.out.println("  [COLLECT-OLD] " + obj.name);
                    return true;
                }
                return false;
            });
            System.out.println("  Major GC completado. Old size: " + old.size());
        }

        void printState() {
            System.out.println("  Young [" + young.size() + "/" + youngCapacity + "]: " + young);
            System.out.println("  Old   [" + old.size() + "/" + oldCapacity + "]:   " + old);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Generational GC Simulation ===\n");

        // Objetos "vivos" (no son garbage)
        Set<String> live = new HashSet<>(Arrays.asList(
            "A", "B", "C", "E", "F", "H", "I", "K", "L", "N",
            "O", "P", "R", "S", "T"
        ));

        GenerationalGC gc = new GenerationalGC(10, 50, live);
        String[] names = {"A","B","C","D","E","F","G","H","I","J",
                          "K","L","M","N","O","P","Q","R","S","T"};

        for (int i = 0; i < 20; i++) {
            gc.allocate(names[i]);

            if ((i + 1) % 10 == 0) {
                gc.majorGc();
            } else if ((i + 1) % 5 == 0) {
                gc.minorGc();
            }

            if ((i + 1) % 5 == 0) {
                System.out.println("\n--- Estado tras " + (i+1) + " allocations ---");
                gc.printState();
            }
        }
    }
}
