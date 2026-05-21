public class Ejercicio2 {

    // TimingProxy: mide el tiempo de ejecucion de cualquier Runnable
    // Equivale a un @Around advice en Spring AOP
    static class TimingProxy {
        public static void timedRun(String nombre, Runnable tarea) {
            long start = System.currentTimeMillis();
            try {
                tarea.run();
            } finally {
                long elapsed = System.currentTimeMillis() - start;
                System.out.println("[" + nombre + "] -> " + elapsed + "ms");
            }
        }
    }

    // Simulacion de trabajo con sleep
    static void trabajoRapido() throws InterruptedException { Thread.sleep(10); }
    static void trabajoMedio() throws InterruptedException  { Thread.sleep(50); }
    static void trabajoLento() throws InterruptedException  { Thread.sleep(100); }

    // Calculo CPU-puro (sin sleep)
    static long calcularSuma() {
        long suma = 0;
        for (int i = 0; i < 1_000_000; i++) suma += i;
        return suma;
    }

    public static void main(String[] args) {
        System.out.println("=== TimingProxy (proxy de medicion de tiempo) ===\n");

        TimingProxy.timedRun("trabajo-rapido", () -> {
            try { trabajoRapido(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        TimingProxy.timedRun("trabajo-medio", () -> {
            try { trabajoMedio(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        TimingProxy.timedRun("trabajo-lento", () -> {
            try { trabajoLento(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        TimingProxy.timedRun("suma-cpu", () -> {
            long r = calcularSuma();
            System.out.println("  (suma = " + r + ")");
        });

        System.out.println();
        System.out.println("=== Uso en AOP ===");
        System.out.println("En Spring AOP equivaldria a:");
        System.out.println("  @Around(\"execution(* com.example.service.*.*(..))\" )");
        System.out.println("  public Object medirTiempo(ProceedingJoinPoint pjp) throws Throwable {");
        System.out.println("      long start = System.currentTimeMillis();");
        System.out.println("      try { return pjp.proceed(); }");
        System.out.println("      finally { log.info(pjp.getSignature() + \" -> \" + (now - start) + \"ms\"); }");
        System.out.println("  }");
    }
}
