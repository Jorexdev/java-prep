public class ExpInheritanceVsComposition {

    public static void main(String[] args) {

        System.out.println("=== HERENCIA PROFUNDA (frágil) ===");
        Dog dog = new Dog("Rex");
        dog.move();
        dog.makeSound();
        dog.breathe();  // heredado de Mammal
        // Problema: si Animal.move() cambia a "dash", Dog hereda el cambio aunque no lo quiera

        System.out.println("\n=== VIOLACIÓN DE LISKOV ===");
        // PoliceDog sobreescribe makeSound() para silenciar — viola LSP:
        // el cliente esperaba un sonido y recibe silencio
        Animal policeAnimal = new PoliceDog("K9");
        policeAnimal.makeSound();   // silencio inesperado

        System.out.println("\n=== COMPOSICIÓN (flexible) ===");
        DogC labrador = new DogC("Labrador", new Walk(), new Bark());
        labrador.act();

        // RobotDog no puede entrar en la jerarquía Animal, pero con composición es trivial
        DogC robotDog = new DogC("RobotDog", new WheelDrive(), new Beep());
        robotDog.act();

        // Cambiar comportamiento en runtime sin subclasificar
        System.out.println("\n=== COMPORTAMIENTO EN RUNTIME ===");
        DogC quietDog = new DogC("Silencioso", new Walk(), new Silent());
        quietDog.act();
    }

    // ── Jerarquía de herencia profunda ──────────────────────────────────────

    abstract static class Animal {
        final String name;
        Animal(String name) { this.name = name; }
        // Cambiar este método afecta a TODA la jerarquía sin avisar (fragile base)
        void move()      { System.out.println(name + " se mueve"); }
        abstract void makeSound();
    }

    abstract static class Mammal extends Animal {
        Mammal(String name) { super(name); }
        void breathe() { System.out.println(name + " respira aire"); }
    }

    static class Dog extends Mammal {
        Dog(String name) { super(name); }
        @Override public void makeSound() { System.out.println(name + ": Guau"); }
    }

    // Violación LSP: PoliceDog silencia makeSound(), rompiendo el contrato de Animal
    static class PoliceDog extends Dog {
        PoliceDog(String name) { super(name); }
        @Override public void makeSound() {
            // perro de policía en servicio no ladra — pero el contrato decía "emite sonido"
            System.out.println(name + ": [silencio — viola LSP]");
        }
    }

    // ── Composición: comportamiento delegado a estrategias ──────────────────

    interface Locomotion { void move(String name); }
    interface Sound      { void emit(String name); }

    static class Walk       implements Locomotion { public void move(String n) { System.out.println(n + " camina"); } }
    static class WheelDrive implements Locomotion { public void move(String n) { System.out.println(n + " rueda sobre ruedas"); } }

    static class Bark   implements Sound { public void emit(String n) { System.out.println(n + ": Guau"); } }
    static class Beep   implements Sound { public void emit(String n) { System.out.println(n + ": Bip-bip"); } }
    static class Silent implements Sound { public void emit(String n) { System.out.println(n + ": [sin sonido]"); } }

    // DogC no extiende ninguna clase — sus capacidades vienen de los objetos que contiene
    static class DogC {
        private final String name;
        private final Locomotion locomotion;
        private final Sound sound;

        DogC(String name, Locomotion locomotion, Sound sound) {
            this.name = name; this.locomotion = locomotion; this.sound = sound;
        }

        void act() { locomotion.move(name); sound.emit(name); }
    }
}
