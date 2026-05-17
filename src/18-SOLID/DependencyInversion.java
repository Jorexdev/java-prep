// MAL - Switch depende directamente de Lamp (clase concreta).
// Para controlar un Fan tendríamos que modificar Switch.
class LampDirect {
    public void turnOn() { System.out.println("Lámpara encendida"); }
}

class SwitchBad {
    private LampDirect lamp = new LampDirect(); // acoplado a la implementación
    public void toggle() { lamp.turnOn(); }
}

// BIEN - Switch depende de la abstracción Switchable.
// Puedes pasarle cualquier dispositivo sin modificar Switch.
// Esto es exactamente lo que hace Spring con @Autowired + interfaces.
interface Switchable {
    void turnOn();
}

class Lamp implements Switchable {
    public void turnOn() { System.out.println("La lámpara está encendida"); }
}

class Fan implements Switchable {
    public void turnOn() { System.out.println("El ventilador está encendido"); }
}

class Switch {
    private final Switchable device;

    // La dependencia se inyecta desde fuera, no se crea aquí
    public Switch(Switchable device) { this.device = device; }

    public void toggle() { device.turnOn(); }
}

public class DependencyInversion {
    public static void main(String[] args) {
        // Podemos pasar cualquier Switchable sin tocar Switch
        new Switch(new Lamp()).toggle();
        new Switch(new Fan()).toggle();

        // En tests, pasaríamos un mock de Switchable
        Switchable mock = () -> System.out.println("[Mock] Dispositivo encendido");
        new Switch(mock).toggle();
    }
}
