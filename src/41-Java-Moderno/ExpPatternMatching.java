import java.util.List;
import java.util.Map;

// Pattern Matching (Java 16 para instanceof, Java 21 para switch patterns)
// Combina test de tipo + binding en una sola expresión, eliminando casts explícitos.

// Jerarquía AST para demostrar deconstruction patterns
// Prefijo 'Ast' para evitar colisiones con records de otros Exp*.java en el mismo directorio
sealed interface AstNodo permits AstNum, AstSuma, AstProd, AstNeg {}
record AstNum(double valor) implements AstNodo {}
record AstSuma(AstNodo izq, AstNodo der) implements AstNodo {}
record AstProd(AstNodo izq, AstNodo der) implements AstNodo {}
record AstNeg(AstNodo hijo) implements AstNodo {}

// Jerarquía para demostrar guarded patterns
sealed interface Animal permits Perro, Gato, Pajaro {}
record Perro(String nombre, String raza) implements Animal {}
record Gato(String nombre, boolean esDomestico) implements Animal {}
record Pajaro(String nombre, boolean puedeVolar) implements Animal {}

public class ExpPatternMatching {

    // 1. instanceof clásico vs pattern matching
    static String formatear_clasico(Object obj) {
        // Antes: check + cast redundante + nueva variable
        if (obj instanceof String) {
            String s = (String) obj; // cast explícito
            return "String de longitud " + s.length() + ": " + s;
        } else if (obj instanceof Integer) {
            Integer i = (Integer) obj;
            return "Integer: " + i;
        }
        return "Desconocido: " + obj;
    }

    static String formatear(Object obj) {
        // Pattern matching: test + binding en una sola expresión — sin cast explícito
        if (obj instanceof String s) {
            return "String de longitud " + s.length() + ": " + s;
        } else if (obj instanceof Integer i) {
            return "Integer: " + i;
        } else if (obj instanceof List<?> lista) {
            return "List con " + lista.size() + " elementos";
        } else if (obj instanceof Map<?,?> mapa) {
            return "Map con " + mapa.size() + " entradas";
        }
        return "Desconocido: " + obj;
    }

    // 2. Pattern matching con && (guard en instanceof)
    static String clasificarString(Object obj) {
        // La variable 's' solo existe en el scope donde instanceof es true
        if (obj instanceof String s && s.length() > 10) {
            return "String largo: " + s.toUpperCase();
        } else if (obj instanceof String s && !s.isBlank()) {
            return "String corto: " + s;
        } else if (obj instanceof String s) {
            return "String vacio o blank";
        }
        return "No es String";
    }

    // 3. Switch con pattern matching y when (Java 21)
    static String describir(Object obj) {
        return switch (obj) {
            case Integer i when i < 0      -> "negativo: " + i;
            case Integer i when i == 0     -> "cero";
            case Integer i                 -> "positivo: " + i;
            case String s when s.isBlank() -> "string vacio";
            case String s                  -> "string: \"" + s + "\"";
            case Double d                  -> String.format("double: %.3f", d);
            case int[] arr                 -> "array de " + arr.length + " ints";
            case null                      -> "null";
            default                        -> "tipo desconocido: " + obj.getClass().getSimpleName();
        };
    }

    // 4. Guarded patterns con when en sealed switch
    static String clasificarAnimal(Animal a) {
        return switch (a) {
            case Perro p when p.raza().equals("Labrador") -> p.nombre() + " es un Labrador amigable";
            case Perro p                                   -> p.nombre() + " es un perro (" + p.raza() + ")";
            case Gato g when g.esDomestico()               -> g.nombre() + " es un gato domestico";
            case Gato g                                    -> g.nombre() + " es un gato salvaje";
            case Pajaro pb when pb.puedeVolar()            -> pb.nombre() + " puede volar";
            case Pajaro pb                                 -> pb.nombre() + " no puede volar";
        };
    }

    // 5. Deconstruction patterns en switch (Java 21)
    static double evaluar(AstNodo nodo) {
        return switch (nodo) {
            case AstNum(var v)           -> v;
            case AstNeg(var hijo)        -> -evaluar(hijo);
            case AstSuma(var izq, var der) -> evaluar(izq) + evaluar(der);
            case AstProd(var izq, var der) -> evaluar(izq) * evaluar(der);
        };
    }

    static String mostrarNodo(AstNodo nodo) {
        return switch (nodo) {
            case AstNum(var v)             -> String.valueOf(v);
            case AstNeg(var h)             -> "-(" + mostrarNodo(h) + ")";
            case AstSuma(var i, var d)     -> "(" + mostrarNodo(i) + " + " + mostrarNodo(d) + ")";
            case AstProd(var i, var d)     -> "(" + mostrarNodo(i) + " * " + mostrarNodo(d) + ")";
        };
    }

    // 6. Nested deconstruction patterns
    static String describirNodoAnidado(AstNodo nodo) {
        return switch (nodo) {
            // Deconstruccion anidada: Suma cuyo lado izquierdo también es Suma
            case AstSuma(AstSuma(var a, var b), var c) ->
                "suma de suma: ((" + mostrarNodo(a) + " + " + mostrarNodo(b) + ") + " + mostrarNodo(c) + ")";
            case AstSuma(var i, var d) ->
                "suma simple: " + mostrarNodo(i) + " + " + mostrarNodo(d);
            default -> "otro: " + mostrarNodo(nodo);
        };
    }

    public static void main(String[] args) {

        System.out.println("=== PATTERN MATCHING ===\n");

        // 1. instanceof pattern matching
        System.out.println("--- instanceof pattern matching ---");
        List<Object> objetos = List.of("Hola", 42, List.of(1, 2, 3), Map.of("a", 1), 3.14);
        objetos.forEach(o -> System.out.println("  " + formatear(o)));

        // 2. Guard con &&
        System.out.println("\n--- Guard con && en instanceof ---");
        List<Object> strings = List.of("Este es un string muy largo", "corto", "  ", 123);
        strings.forEach(s -> System.out.println("  " + clasificarString(s)));

        // 3. Switch con pattern matching y when
        System.out.println("\n--- Switch con patterns y when ---");
        List<Object> valores = List.of(-5, 0, 42, "  ", "Java 21", 3.14159, new int[]{1, 2, 3}, null);
        valores.forEach(v -> System.out.println("  " + describir(v)));

        // 4. Sealed + guarded patterns
        System.out.println("\n--- Sealed + guarded patterns ---");
        List<Animal> animales = List.of(
            new Perro("Rex", "Labrador"),
            new Perro("Max", "Bulldog"),
            new Gato("Misu", true),
            new Gato("Tigre", false),
            new Pajaro("Loro", true),
            new Pajaro("Pinguino", false)
        );
        animales.forEach(a -> System.out.println("  " + clasificarAnimal(a)));

        // 5. Deconstruction patterns con AST: (3 + 4) * -(2)
        System.out.println("\n--- Deconstruction patterns (AST) ---");
        AstNodo expr = new AstProd(
            new AstSuma(new AstNum(3), new AstNum(4)),
            new AstNeg(new AstNum(2))
        );
        System.out.println("Expresion: " + mostrarNodo(expr));
        System.out.println("Resultado: " + evaluar(expr));  // -14.0

        // 6. Nested deconstruction
        System.out.println("\n--- Nested deconstruction ---");
        AstNodo suma1     = new AstSuma(new AstNum(1), new AstNum(2));
        AstNodo sumAnidada = new AstSuma(suma1, new AstNum(3));
        AstNodo simpleSuma = new AstSuma(new AstNum(5), new AstNum(6));
        System.out.println(describirNodoAnidado(sumAnidada));
        System.out.println(describirNodoAnidado(simpleSuma));
        System.out.println(describirNodoAnidado(new AstNum(7)));
    }
}
