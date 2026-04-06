package solid.abiertocerrado;

/*
    O - Open/Closed Principle (OCP)

    ¿Qué es?
    Las clases deben estar abiertas a extensión pero cerradas a modificación.
    Añadir nuevas funcionalidades extendiendo el código, sin tocar el existente.

    ¿Para qué sirve?
    Evitar que cada vez que añades un nuevo caso tengas que modificar código que ya funciona,
    arriesgándote a introducir bugs. Se logra usando abstracciones (interfaces o clases abstractas)
    en lugar de condicionales.

    ¿Cuándo aplicarlo?
    - Cuando una clase crece con if/else o switch para cada nuevo tipo.
    - Cuando añadir una nueva variante obliga a modificar lógica existente.

    Preguntas típicas de entrevista:
    - ¿Cómo se aplica OCP sin interfaces? (herencia, composición)
    - ¿Qué patrones de diseño implementan OCP? (Strategy, Decorator, Factory)
    - ¿Es posible aplicar OCP al 100%? ¿O siempre hay que modificar algo?
*/

/*
    MAL - Cada vez que añades una figura debes modificar esta clase.
    Viola OCP porque está cerrada a extensión pero abierta a modificación.
*/
class AreaCalculatorBad {
    public double calculate(Object shape) {
        if (shape instanceof CircleBad c) {
            return Math.PI * c.radius * c.radius;
        } else if (shape instanceof SquareBad s) {
            return s.side * s.side;
        }
        // si añades Triángulo, tienes que venir aquí a modificar
        throw new IllegalArgumentException("Forma no soportada");
    }
}
class CircleBad { double radius; CircleBad(double r) { this.radius = r; } }
class SquareBad { double side;   SquareBad(double s) { this.side = s; }   }

/*
    BIEN - Para añadir una nueva figura, solo creas una nueva clase que implemente Shape.
    AreaCalculator no se toca nunca.
*/
interface Shape {
    double area();
}

class Circle implements Shape {
    double radius;
    Circle(double r) { this.radius = r; }
    public double area() { return Math.PI * radius * radius; }
}

class Square implements Shape {
    double side;
    Square(double s) { this.side = s; }
    public double area() { return side * side; }
}

// Nueva figura: no tocamos nada existente
class Triangle implements Shape {
    double base, height;
    Triangle(double b, double h) { this.base = b; this.height = h; }
    public double area() { return (base * height) / 2; }
}

public class OpenClosed {
    public static void main(String[] args) {
        Shape[] shapes = { new Circle(5), new Square(4), new Triangle(6, 3) };

        for (Shape s : shapes) {
            System.out.printf("%s - Area: %.2f%n", s.getClass().getSimpleName(), s.area());
        }
    }
}
