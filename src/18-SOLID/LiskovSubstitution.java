// MAL - Violación clásica: Rectangle/Square.
// Square hereda de Rectangle pero rompe el contrato porque en un cuadrado
// al cambiar el ancho también cambia el alto, lo que no se espera de un Rectangle.
class Rectangle {
    protected int width, height;
    public void setWidth(int w)  { this.width = w; }
    public void setHeight(int h) { this.height = h; }
    public int area() { return width * height; }
}

class Square extends Rectangle {
    // Viola LSP: cambiar el ancho también cambia el alto, rompiendo el contrato de Rectangle
    @Override public void setWidth(int w)  { this.width = w; this.height = w; }
    @Override public void setHeight(int h) { this.width = h; this.height = h; }
}

// BIEN - Separar jerarquías cuando el comportamiento no es sustituible.
// Ave y AvesVoladoras son jerarquías distintas, cada una sustituible en su contexto.
class Bird {
    void eat() { System.out.println("El ave está comiendo"); }
}

class FlyingBird extends Bird {
    void fly() { System.out.println("Estoy volando"); }
}

// Sparrow puede sustituir a FlyingBird sin romper nada
class Sparrow extends FlyingBird { }

// Penguin hereda de Bird directamente, no de FlyingBird
// No tiene fly() porque no vuela: no viola el contrato de Bird
class Penguin extends Bird { }

public class LiskovSubstitution {
    public static void main(String[] args) {
        // Violación: si un método espera Rectangle y recibe Square, area() se comporta diferente
        Rectangle rect = new Square();
        rect.setWidth(5);
        rect.setHeight(3);
        System.out.println("Area esperada: 15, Area real: " + rect.area()); // 9, no 15

        // Correcto: ambas aves son sustituibles en su jerarquía correspondiente
        Bird sparrow = new Sparrow();
        Bird penguin = new Penguin();
        sparrow.eat();
        penguin.eat();
    }
}
