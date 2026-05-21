public class Ejercicio2 {

    static class JvmConfig {
        final int heapInitialMB;
        final int heapMaxMB;
        final int newRatio;      // Old/Young
        final int survivorRatio; // Eden/Survivor

        JvmConfig(int heapInitialMB, int heapMaxMB, int newRatio, int survivorRatio) {
            this.heapInitialMB = heapInitialMB;
            this.heapMaxMB = heapMaxMB;
            this.newRatio = newRatio;
            this.survivorRatio = survivorRatio;
        }

        void printDistribution() {
            int metaspaceMB = 256;
            int youngMB = heapMaxMB / (newRatio + 1);
            int oldMB = heapMaxMB - youngMB;
            int survivorMB = youngMB / (survivorRatio + 2);
            int edenMB = youngMB - 2 * survivorMB;

            System.out.println("=== JVM Heap Distribution ===");
            System.out.printf("  Heap Max:       %4d MB%n", heapMaxMB);
            System.out.printf("  Young Gen:      %4d MB (%.0f%%)%n", youngMB, youngMB * 100.0 / heapMaxMB);
            System.out.printf("    Eden:         %4d MB%n", edenMB);
            System.out.printf("    Survivor x2:  %4d MB each%n", survivorMB);
            System.out.printf("  Old Gen:        %4d MB (%.0f%%)%n", oldMB, oldMB * 100.0 / heapMaxMB);
            System.out.printf("  Metaspace:      %4d MB (off-heap)%n", metaspaceMB);
        }

        void validate() {
            System.out.println("=== Validation ===");
            if (heapMaxMB < heapInitialMB)
                System.out.println("  ERROR: heapMax (" + heapMaxMB + ") < heapInitial (" + heapInitialMB + ")");
            else
                System.out.println("  OK: heapMax >= heapInitial");

            if (newRatio < 1 || newRatio > 10)
                System.out.println("  WARN: newRatio=" + newRatio + " fuera del rango recomendado [1-10]");
            else
                System.out.println("  OK: newRatio=" + newRatio);

            if (survivorRatio < 2 || survivorRatio > 16)
                System.out.println("  WARN: survivorRatio=" + survivorRatio + " fuera del rango recomendado [2-16]");
            else
                System.out.println("  OK: survivorRatio=" + survivorRatio);
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Config estándar (-Xms512m -Xmx2048m -XX:NewRatio=2 -XX:SurvivorRatio=8) ---");
        JvmConfig standard = new JvmConfig(512, 2048, 2, 8);
        standard.printDistribution();
        standard.validate();

        System.out.println();
        System.out.println("--- Config incorrecta (heapMax < heapInitial) ---");
        JvmConfig bad = new JvmConfig(4096, 1024, 2, 8);
        bad.validate();

        System.out.println();
        System.out.println("--- Config G1GC friendly (-Xmx8192m -XX:NewRatio=1 -XX:SurvivorRatio=6) ---");
        JvmConfig g1 = new JvmConfig(2048, 8192, 1, 6);
        g1.printDistribution();
        g1.validate();
    }
}
