public class Ejercicio5 {

    @SuppressWarnings("deprecation")
    static class Recurso {
        private final String nombre;

        Recurso(String nombre) {
            this.nombre = nombre;
            System.out.println("[CREATED]    " + nombre);
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println("[FINALIZED]  " + nombre);
            super.finalize();
        }

        void usar() {
            System.out.println("[USING]      " + nombre);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Object Lifecycle con finalize() ===");
        System.out.println();

        // Crear instancias
        Recurso r1 = new Recurso("ConexionDB");
        Recurso r2 = new Recurso("FileHandle");
        Recurso r3 = new Recurso("Socket");

        System.out.println();
        r1.usar();
        r2.usar();
        r3.usar();
        System.out.println();

        // Eliminar referencias fuertes
        System.out.println("Eliminando referencias (r1=null, r2=null, r3=null)...");
        r1 = null;
        r2 = null;
        r3 = null;

        System.out.println("Solicitando GC y finalización...");
        System.gc();
        System.runFinalization();
        Thread.sleep(500);

        System.out.println();
        System.out.println("=== Notas importantes ===");
        System.out.println("- finalize() está deprecado desde Java 9.");
        System.out.println("- No hay garantía de cuándo (ni si) se ejecuta.");
        System.out.println("- Alternativa moderna: Cleaner API o try-with-resources.");
    }
}
