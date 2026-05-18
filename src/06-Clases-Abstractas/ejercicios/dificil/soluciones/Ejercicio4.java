public class Ejercicio4 {
    abstract static class Pipeline {
        void antes()   {} // hook opcional
        abstract void proceso();
        void despues() {} // hook opcional
        final void ejecutar() { antes(); proceso(); despues(); }
    }
    static class PipelineBasico extends Pipeline {
        @Override void proceso() { System.out.println("Ejecutando proceso básico"); }
    }
    static class PipelineConLog extends Pipeline {
        @Override void antes()   { System.out.println("[LOG] Iniciando pipeline..."); }
        @Override void proceso() { System.out.println("Procesando datos..."); }
        @Override void despues() { System.out.println("[LOG] Pipeline finalizado."); }
    }
    public static void main(String[] args) {
        System.out.println("=== Pipeline Básico ===");
        new PipelineBasico().ejecutar();
        System.out.println("\n=== Pipeline con Log ===");
        new PipelineConLog().ejecutar();
    }
}
