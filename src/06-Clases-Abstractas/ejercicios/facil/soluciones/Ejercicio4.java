public class Ejercicio4 {
    interface Volador { void volar(); }
    abstract static class Animal {
        protected final String nombre;
        Animal(String nombre) { this.nombre = nombre; }
        abstract void hablar();
        void respirar() { System.out.println(nombre + " respira"); }
    }
    abstract static class Ave extends Animal {
        Ave(String nombre) { super(nombre); }
        @Override void hablar() { System.out.println(nombre + " canta: pío pío"); }
    }
    static class Aguila extends Ave implements Volador {
        Aguila(String nombre) { super(nombre); }
        @Override public void volar() { System.out.println(nombre + " vuela a gran altitud"); }
    }
    public static void main(String[] args) {
        Aguila aguila = new Aguila("Zeus");
        aguila.hablar();
        aguila.respirar();
        aguila.volar();
    }
}
