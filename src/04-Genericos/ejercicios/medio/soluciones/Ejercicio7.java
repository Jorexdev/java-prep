import java.util.List;

public class Ejercicio7 {

    static class Animal {
        private final String nombre;
        private final double pesoKg;

        Animal(String nombre, double pesoKg) {
            this.nombre = nombre;
            this.pesoKg = pesoKg;
        }

        double getPesoKg() { return pesoKg; }

        @Override
        public String toString() { return nombre + "(" + pesoKg + "kg)"; }
    }

    static class Perro extends Animal {
        Perro(String nombre, double pesoKg) { super(nombre, pesoKg); }
    }

    static class Gato extends Animal {
        Gato(String nombre, double pesoKg) { super(nombre, pesoKg); }
    }

    // ? extends Animal — acepta List<Perro>, List<Gato> o List<Animal>
    static double sumaPesos(List<? extends Animal> animales) {
        double total = 0;
        for (Animal a : animales) {
            total += a.getPesoKg();
        }
        return total;
    }

    public static void main(String[] args) {

        List<Perro> perros = List.of(new Perro("Rex", 25.0), new Perro("Toby", 18.5));
        List<Gato>  gatos  = List.of(new Gato("Misi", 4.2), new Gato("Luna", 3.8));
        List<Animal> todos = List.of(new Perro("Max", 30.0), new Gato("Nala", 5.0));

        System.out.println("Peso perros: " + sumaPesos(perros) + " kg");
        System.out.println("Peso gatos:  " + sumaPesos(gatos)  + " kg");
        System.out.println("Peso todos:  " + sumaPesos(todos)  + " kg");
    }
}
