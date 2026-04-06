package solid.segregacioninterfaces;

/*
    I - Interface Segregation Principle (ISP)

    ¿Qué es?
    Ningún cliente debe depender de métodos que no usa.
    Es mejor tener interfaces pequeñas y específicas que una interfaz grande ("fat interface").

    ¿Para qué sirve?
    Evitar que las clases implementen métodos vacíos o que lancen UnsupportedOperationException
    porque la interfaz les obliga a declarar comportamiento que no les corresponde.

    ¿Cuándo aplicarlo?
    - Cuando una implementación deja métodos vacíos o con throws.
    - Cuando cambiar un método en la interfaz afecta a clases que no lo usan.

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre ISP y SRP?
    - ¿Cómo se relaciona ISP con la inyección de dependencias?
    - ¿Cuándo es aceptable tener una interfaz grande?
*/

/*
    MAL - Una sola interfaz obliga al Robot a implementar eat(), que no necesita.
*/
interface WorkerFat {
    void work();
    void eat();   // el robot no debería tener que implementar esto
}

class RobotBad implements WorkerFat {
    public void work() { System.out.println("El robot trabaja"); }
    public void eat()  { throw new UnsupportedOperationException("Los robots no comen"); }
}

/*
    BIEN - Interfaces segregadas: cada clase implementa solo lo que necesita.
*/
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}

class Human implements Workable, Eatable {
    public void work() { System.out.println("El humano trabaja"); }
    public void eat()  { System.out.println("El humano come"); }
}

class Robot implements Workable {
    public void work() { System.out.println("El robot trabaja"); }
}

public class InterfaceSegregation {
    public static void main(String[] args) {
        Human h = new Human();
        h.work();
        h.eat();

        Robot r = new Robot();
        r.work();
    }
}
