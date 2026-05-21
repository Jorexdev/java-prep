import java.util.ArrayList;
import java.util.List;

public class Ejercicio3 {

    enum EscapeStatus { ESCAPES, NO_ESCAPES }

    static class Point {
        int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }

    // Almacén para demostrar escape al heap
    static Point storedPoint;
    static List<Point> pointList = new ArrayList<>();

    // 1. Escapa por return — el llamador retiene la referencia
    static Point method1() {
        Point p = new Point(1, 2);
        return p; // ESCAPES: retorna al caller
    }

    // 2. Escapa al almacenarse en campo de instancia
    static void method2() {
        Point p = new Point(3, 4);
        storedPoint = p; // ESCAPES: asignado a campo estático
    }

    // 3. Escapa porque otro método lo almacena
    static void method3() {
        Point p = new Point(5, 6);
        storeIt(p); // ESCAPES: el método receptor lo guarda
    }

    static void storeIt(Point p) {
        pointList.add(p);
    }

    // 4. NO escapa — uso estrictamente local
    static int method4() {
        Point p = new Point(7, 8);
        int sum = p.x + p.y; // NO ESCAPES: p no sale del scope
        return sum;
    }

    static class EscapeAnalyzer {
        record Analysis(String method, String description, EscapeStatus status, String jvmOptimization) {}

        static List<Analysis> analyze() {
            return List.of(
                new Analysis("method1", "Objeto devuelto por return",
                    EscapeStatus.ESCAPES,
                    "JVM no puede optimizar: el caller retiene la ref"),
                new Analysis("method2", "Objeto almacenado en campo estático",
                    EscapeStatus.ESCAPES,
                    "JVM no puede optimizar: el campo vive en el heap"),
                new Analysis("method3", "Objeto pasado a método que lo almacena",
                    EscapeStatus.ESCAPES,
                    "JVM no puede optimizar sin inter-procedural analysis"),
                new Analysis("method4", "Objeto usado solo localmente",
                    EscapeStatus.NO_ESCAPES,
                    "JVM puede: stack allocation (evita GC), scalar replacement (descompone en campos)")
            );
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Escape Analysis Simulation ===\n");
        for (var a : EscapeAnalyzer.analyze()) {
            System.out.printf("[%s] %s%n", a.status(), a.method());
            System.out.printf("  Descripción:   %s%n", a.description());
            System.out.printf("  JVM:           %s%n%n", a.jvmOptimization());
        }

        System.out.println("--- Demostración ---");
        Point escapado = method1();
        System.out.println("method1 devolvió: " + escapado.x + "," + escapado.y);
        method2();
        System.out.println("method2 guardó en campo estático: " + storedPoint.x + "," + storedPoint.y);
        method3();
        System.out.println("method3 añadió a lista: " + pointList.size() + " elemento(s)");
        int result = method4();
        System.out.println("method4 devolvió suma (objeto no escapó): " + result);

        System.out.println("\n--- Flags JVM para habilitar optimizaciones ---");
        System.out.println("  -XX:+DoEscapeAnalysis      (activo por defecto en JDK 21)");
        System.out.println("  -XX:+EliminateAllocations  (scalar replacement)");
        System.out.println("  -XX:+PrintEscapeAnalysis   (debug)");
    }
}
