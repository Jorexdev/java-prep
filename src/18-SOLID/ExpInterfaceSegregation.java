// MAL - Una sola interfaz obliga al Robot a implementar eat(), que no necesita.
interface WorkerFat {
    void work();
    void eat();   // el robot no debería tener que implementar esto
}

class RobotBad implements WorkerFat {
    public void work() { System.out.println("El robot trabaja"); }
    public void eat()  { throw new UnsupportedOperationException("Los robots no comen"); }
}

// BIEN - Interfaces segregadas: cada clase implementa solo lo que necesita.
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

public class ExpInterfaceSegregation {
    public static void main(String[] args) {
        Human h = new Human();
        h.work();
        h.eat();

        Robot r = new Robot();
        r.work();
    }
}
