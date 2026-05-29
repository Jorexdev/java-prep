import java.util.List;

// Switch Expressions (Java 14 estable) — switch que produce un valor.
// Arrow labels (->) eliminan fall-through. yield para casos con lógica.

enum DiaSemana { LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO }
enum Temporada { PRIMAVERA, VERANO, OTONO, INVIERNO }

sealed interface Figura permits FiguraC, FiguraR, FiguraT {}
record FiguraC(double radio) implements Figura {}
record FiguraR(double ancho, double alto) implements Figura {}
record FiguraT(double base, double altura) implements Figura {}

public class ExpSwitchExpressions {

    // 1. Switch expression básico con arrow labels
    static int numLetras(DiaSemana dia) {
        return switch (dia) {
            case LUNES, VIERNES, DOMINGO -> 6;
            case MARTES                  -> 7;
            case JUEVES, SABADO          -> 8;
            case MIERCOLES               -> 9;
            // Exhaustivo: todos los valores del enum están cubiertos
        };
    }

    // 2. Switch statement clásico vs switch expression
    static String esLaborable_statement(DiaSemana dia) {
        // Statement: no produce valor, requiere variable mutable
        String resultado;
        switch (dia) {
            case LUNES: case MARTES: case MIERCOLES: case JUEVES: case VIERNES:
                resultado = "laborable";
                break;
            default:
                resultado = "fin de semana";
        }
        return resultado;
    }

    static String esLaborable_expression(DiaSemana dia) {
        // Expression: produce valor directamente, sin variable mutable ni break
        return switch (dia) {
            case LUNES, MARTES, MIERCOLES, JUEVES, VIERNES -> "laborable";
            case SABADO, DOMINGO                           -> "fin de semana";
        };
    }

    // 3. yield: necesario cuando el caso tiene lógica
    static String categorizar(int puntuacion) {
        return switch (puntuacion / 10) {
            case 10, 9 -> "Excelente";
            case 8     -> "Notable";
            case 7     -> "Bien";
            case 6     -> "Aprobado";
            default -> {
                if (puntuacion < 0) yield "Invalido";
                String msg = "Suspenso (" + puntuacion + "/100)";
                yield msg; // yield devuelve el valor del bloque
            }
        };
    }

    // 4. Switch expression con tipos (pattern matching, Java 21)
    static String describir(Object obj) {
        return switch (obj) {
            case null                        -> "null";
            case Integer i when i < 0        -> "entero negativo: " + i;
            case Integer i                   -> "entero: " + i;
            case String s when s.length() > 5 -> "string largo: " + s;
            case String s                    -> "string: " + s;
            case Double d                    -> String.format("double: %.2f", d);
            case List<?> l when l.isEmpty()  -> "lista vacia";
            case List<?> l                   -> "lista[" + l.size() + "]";
            default                          -> "tipo: " + obj.getClass().getSimpleName();
        };
    }

    // 5. Switch con sealed class — exhaustivo sin default
    static double area(Figura f) {
        return switch (f) {
            case FiguraC c -> Math.PI * c.radio() * c.radio();
            case FiguraR r -> r.ancho() * r.alto();
            case FiguraT t -> 0.5 * t.base() * t.altura();
            // No default: el compilador verifica exhaustiveness por la sealed class
        };
    }

    // 6. Switch expression devuelto desde un método
    static String estacionDelAno(int mes) {
        if (mes < 1 || mes > 12) return "mes invalido";
        return switch (mes) {
            case 3, 4, 5   -> "Primavera";
            case 6, 7, 8   -> "Verano";
            case 9, 10, 11 -> "Otono";
            case 12, 1, 2  -> "Invierno";
            default        -> "imposible"; // el compilador no sabe que 1-12 cubre todo
        };
    }

    // 7. Switch expression como argumento directo
    static void imprimirPrioridad(int nivel) {
        System.out.println("Prioridad: " + switch (nivel) {
            case 1 -> "CRITICA";
            case 2 -> "ALTA";
            case 3 -> "MEDIA";
            default -> "BAJA";
        });
    }

    // 8. Switch con enums y lambdas/functional
    @FunctionalInterface
    interface Operacion { double aplicar(double a, double b); }

    static Operacion obtenerOperacion(String simbolo) {
        return switch (simbolo) {
            case "+" -> (a, b) -> a + b;
            case "-" -> (a, b) -> a - b;
            case "*" -> (a, b) -> a * b;
            case "/" -> (a, b) -> {
                if (b == 0) throw new ArithmeticException("division por cero");
                yield a / b;
            };
            default -> throw new IllegalArgumentException("Simbolo desconocido: " + simbolo);
        };
    }

    public static void main(String[] args) {

        System.out.println("=== SWITCH EXPRESSIONS ===\n");

        // 1. Arrow labels básico
        System.out.println("--- numLetras por dia ---");
        for (DiaSemana dia : DiaSemana.values()) {
            System.out.printf("  %-10s -> %d letras%n", dia, numLetras(dia));
        }

        // 2. Statement vs Expression
        System.out.println("\n--- Statement vs Expression ---");
        System.out.println("Statement: " + esLaborable_statement(DiaSemana.MARTES));
        System.out.println("Expression: " + esLaborable_expression(DiaSemana.SABADO));

        // 3. yield con lógica
        System.out.println("\n--- yield en bloques ---");
        int[] puntuaciones = {100, 85, 75, 65, 45, -1};
        for (int p : puntuaciones) {
            System.out.printf("  %3d -> %s%n", p, categorizar(p));
        }

        // 4. Pattern matching en switch
        System.out.println("\n--- Switch con pattern matching ---");
        List<Object> valores = List.of(42, -7, "Hola", "Java Moderno", 3.14, List.of(), List.of(1, 2), null);
        valores.forEach(v -> System.out.println("  " + describir(v)));

        // 5. Exhaustiveness con sealed class
        System.out.println("\n--- Sealed class exhaustiveness ---");
        List<Figura> figuras = List.of(new FiguraC(5), new FiguraR(3, 4), new FiguraT(6, 8));
        figuras.forEach(f -> System.out.printf("  %s -> area = %.2f%n", f, area(f)));

        // 6. Estacion del año
        System.out.println("\n--- Estacion del anyo ---");
        for (int mes = 1; mes <= 12; mes++) {
            System.out.printf("  Mes %2d: %s%n", mes, estacionDelAno(mes));
        }

        // 7. Como argumento
        System.out.println("\n--- Como argumento directo ---");
        imprimirPrioridad(1);
        imprimirPrioridad(3);
        imprimirPrioridad(5);

        // 8. Switch que devuelve lambda
        System.out.println("\n--- Switch devolviendo funciones ---");
        String[] simbolos = {"+", "-", "*", "/"};
        for (String s : simbolos) {
            Operacion op = obtenerOperacion(s);
            System.out.printf("  10 %s 3 = %.1f%n", s, op.aplicar(10, 3));
        }
    }
}
