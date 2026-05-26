import java.util.List;

public class ExpAbstractSealed {

    public static void main(String[] args) {

        List<Shape> shapes = List.of(
                new Circle(5),
                new Rectangle(4, 6),
                new Triangle(3, 4, 5, 6)   // lados + altura
        );

        for (Shape s : shapes) {
            System.out.printf("%-20s  area=%.2f  perimetro=%.2f%n",
                    s.getClass().getSimpleName(), s.area(), s.perimeter());
        }

        // switch exhaustivo — el compilador sabe que no hay más subtipos
        // no hace falta default, cualquier case no cubierto es error de compilación
        double totalArea = shapes.stream()
                .mapToDouble(s -> switch (s) {
                    case Circle    c -> c.area();
                    case Rectangle r -> r.area();
                    case Triangle  t -> t.area();
                })
                .sum();

        System.out.printf("%nÁrea total de %d figuras: %.2f%n", shapes.size(), totalArea);

        // Clasificación semántica con switch expression + pattern matching
        for (Shape s : shapes) {
            String tipo = switch (s) {
                case Circle    c when c.radius() > 4 -> "círculo grande";
                case Circle    c                     -> "círculo pequeño";
                case Rectangle r when r.width() == r.height() -> "cuadrado";
                case Rectangle r                     -> "rectángulo";
                case Triangle  t                     -> "triángulo";
            };
            System.out.println(s.getClass().getSimpleName() + " -> " + tipo);
        }
    }

    // sealed restringe la jerarquía: solo Circle, Rectangle y Triangle pueden extender Shape
    sealed abstract static class Shape permits Circle, Rectangle, Triangle {
        abstract double area();
        abstract double perimeter();
    }

    static final class Circle extends Shape {
        private final double r;
        Circle(double r) { this.r = r; }
        double radius() { return r; }
        @Override public double area()      { return Math.PI * r * r; }
        @Override public double perimeter() { return 2 * Math.PI * r; }
    }

    static final class Rectangle extends Shape {
        private final double w, h;
        Rectangle(double w, double h) { this.w = w; this.h = h; }
        double width()  { return w; }
        double height() { return h; }
        @Override public double area()      { return w * h; }
        @Override public double perimeter() { return 2 * (w + h); }
    }

    static final class Triangle extends Shape {
        private final double a, b, c, height;
        Triangle(double a, double b, double c, double height) {
            this.a = a; this.b = b; this.c = c; this.height = height;
        }
        @Override public double area()      { return 0.5 * b * height; }
        @Override public double perimeter() { return a + b + c; }
    }
}
