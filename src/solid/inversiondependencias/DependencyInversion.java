package solid.inversiondependencias;

/*
    D - Dependency Inversion Principle (DIP)

    ¿Qué es?
    Los módulos de alto nivel no deben depender de módulos de bajo nivel.
    Ambos deben depender de abstracciones.
    Las abstracciones no deben depender de los detalles, los detalles deben depender de las abstracciones.

    ¿Para qué sirve?
    Desacoplar las capas de la aplicación. Si la lógica de negocio depende directamente
    de una implementación concreta (MySQL, EmailService...), cambiar esa implementación
    requiere modificar la lógica de negocio. Con DIP, depende de una interfaz y puedes
    cambiar la implementación sin tocar nada más.

    ¿Cuándo aplicarlo?
    - Cuando la capa de negocio instancia directamente clases de infraestructura.
    - Cuando necesitas intercambiar implementaciones (real vs mock en tests).
    - Siempre que uses inyección de dependencias.

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre DIP e inyección de dependencias?
    - ¿Cómo implementa Spring DIP con @Autowired?
    - ¿Puedes tener DIP sin un framework de DI?
*/

/*
    MAL - Switch depende directamente de Lamp (clase concreta).
    Para controlar un Fan tendríamos que modificar Switch.
*/
class LampDirect {
    public void turnOn() { System.out.println("Lámpara encendida"); }
}

class SwitchBad {
    private LampDirect lamp = new LampDirect(); // acoplado a la implementación
    public void toggle() { lamp.turnOn(); }
}

/*
    BIEN - Switch depende de la abstracción Switchable.
    Puedes pasarle cualquier dispositivo sin modificar Switch.
    Esto es exactamente lo que hace Spring con @Autowired + interfaces.
*/
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
