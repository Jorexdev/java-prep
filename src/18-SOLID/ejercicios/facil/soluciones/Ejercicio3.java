public class Ejercicio3 {

    static abstract class Ave {
        abstract String nombre();
        void comer() { System.out.println(nombre() + " está comiendo"); }
    }

    interface Voladora {
        void volar();
    }

    static class Aguila extends Ave implements Voladora {
        @Override public String nombre() { return "Águila"; }
        @Override public void volar() { System.out.println("Águila vuela a gran altura"); }
    }

    static class Pinguino extends Ave {
        @Override public String nombre() { return "Pingüino"; }
        void nadar() { System.out.println("Pingüino nada velozmente"); }
    }

    static void hacerVolar(Voladora v) {
        v.volar();
    }

    public static void main(String[] args) {
        Aguila aguila = new Aguila();
        Pinguino pinguino = new Pinguino();

        hacerVolar(aguila);
        pinguino.nadar();
        aguila.comer();
        pinguino.comer();
    }
}
