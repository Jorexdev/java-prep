import java.util.List;
public class Ejercicio2 {
    abstract static class Animal {
        protected final String nombre;
        Animal(String nombre) { this.nombre = nombre; }
        abstract void hablar();
        void respirar() { System.out.println(nombre + " respira"); }
    }
    static class Perro extends Animal {
        Perro(String nombre) { super(nombre); }
        @Override void hablar() { System.out.println(nombre + " dice: Guau!"); }
    }
    static class Gato extends Animal {
        Gato(String nombre) { super(nombre); }
        @Override void hablar() { System.out.println(nombre + " dice: Miau!"); }
    }
    public static void main(String[] args) {
        List<Animal> animales = List.of(new Perro("Rex"), new Gato("Luna"), new Perro("Max"));
        animales.forEach(a -> { a.hablar(); a.respirar(); });
    }
}
