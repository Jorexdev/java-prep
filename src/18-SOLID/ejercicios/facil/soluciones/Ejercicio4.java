public class Ejercicio4 {

    interface Trabajable {
        void trabajar();
    }

    interface Descansable {
        void comer();
        void dormir();
    }

    static class Humano implements Trabajable, Descansable {
        private final String nombre;
        Humano(String nombre) { this.nombre = nombre; }

        @Override public void trabajar() { System.out.println(nombre + " está trabajando"); }
        @Override public void comer()    { System.out.println(nombre + " está comiendo"); }
        @Override public void dormir()   { System.out.println(nombre + " está durmiendo"); }
    }

    static class Robot implements Trabajable {
        private final String id;
        Robot(String id) { this.id = id; }

        @Override public void trabajar() { System.out.println("Robot " + id + " ejecutando tarea"); }
    }

    public static void main(String[] args) {
        Humano h = new Humano("Ana");
        Robot r = new Robot("R2D2");

        h.trabajar(); h.comer(); h.dormir();
        r.trabajar();
    }
}
