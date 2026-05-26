import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class ExpCurrying {

    public static void main(String[] args) {

        // ======================================
        // 1. FUNCIÓN CURRIFICADA — Function<A, Function<B, R>>
        //    En lugar de (a, b) → r, se aplica un argumento a la vez
        // ======================================

        // BiFunction normal: necesita ambos argumentos a la vez
        BiFunction<Integer, Integer, Integer> sumaNormal = (a, b) -> a + b;

        // Versión currificada: devuelve otra función al recibir el primer argumento
        Function<Integer, Function<Integer, Integer>> sumaC = a -> b -> a + b;

        System.out.println("BiFunction: " + sumaNormal.apply(3, 5));
        System.out.println("Currificada: " + sumaC.apply(3).apply(5));

        // La ventaja: podemos fijar el primer argumento y reutilizar la función resultante
        Function<Integer, Integer> suma10 = sumaC.apply(10); // aplicación parcial
        System.out.println("suma10(7) = " + suma10.apply(7));
        System.out.println("suma10(20) = " + suma10.apply(20));

        // ======================================
        // 2. APLICACIÓN PARCIAL — utilitario genérico
        // ======================================

        // partial fija el primer argumento de cualquier BiFunction
        Function<Integer, Integer> multiplicarPor3 = partial(sumaNormal, 3);
        // Con suma no es tan útil, pero el patrón aplica a cualquier BiFunction:
        BiFunction<String, String, String> formatear = (template, valor) ->
                template.replace("{}", valor);
        Function<String, String> formatearError  = partial(formatear, "ERROR: {}");
        Function<String, String> formatearAviso  = partial(formatear, "AVISO: {}");

        System.out.println(formatearError.apply("conexión perdida"));
        System.out.println(formatearAviso.apply("memoria al 90%"));

        // ======================================
        // 3. CURRIFICACIÓN DE FORMAT — template fijo, valor variable
        // ======================================

        Function<String, Function<String, String>> plantilla =
                template -> valor -> template.replace("{}", valor);

        Function<String, String> moneda  = plantilla.apply("{}€");
        Function<String, String> codigo  = plantilla.apply("[{}]");

        System.out.println("moneda: " + moneda.apply("49.99"));
        System.out.println("codigo: " + codigo.apply("ERR-404"));

        // ======================================
        // 4. UNCURRYING — de currificada a BiFunction
        // ======================================

        BiFunction<Integer, Integer, Integer> sumaUncurrificada = uncurry(sumaC);
        System.out.println("uncurry(sumaC).apply(4, 6) = " + sumaUncurrificada.apply(4, 6));

        // ======================================
        // 5. DEMO PRÁCTICA — validadores con aplicación parcial
        // ======================================

        // Función currificada: recibe el límite y devuelve un validador reutilizable
        Function<Integer, Predicate<String>> validadorLongitud =
                minLen -> s -> s != null && s.length() >= minLen;

        Predicate<String> validarPassword  = validadorLongitud.apply(8);
        Predicate<String> validarNombre    = validadorLongitud.apply(2);
        Predicate<String> validarNickname  = validadorLongitud.apply(3);

        List<String> candidatos = List.of("Al", "Jorex", "admin123", "x", "superuser99");

        System.out.println("\n--- Validación de passwords (min 8) ---");
        candidatos.stream()
                .filter(validarPassword)
                .forEach(s -> System.out.println("  OK: " + s));

        System.out.println("--- Validación de nombres (min 2) ---");
        candidatos.stream()
                .filter(validarNombre)
                .forEach(s -> System.out.println("  OK: " + s));

        // Construir mapa de validadores con fábrica currificada
        Map<String, Predicate<String>> validadores = Map.of(
                "password",  validadorLongitud.apply(8),
                "nombre",    validadorLongitud.apply(2),
                "nickname",  validadorLongitud.apply(3)
        );

        String campo = "password";
        String valor = "abc";
        boolean valido = validadores.get(campo).test(valor);
        System.out.println("¿'" + valor + "' válido como " + campo + "? " + valido);
    }

    // Fija el primer argumento de una BiFunction devolviendo una Function parcialmente aplicada
    static <A, B, R> Function<B, R> partial(BiFunction<A, B, R> f, A a) {
        return b -> f.apply(a, b);
    }

    // Convierte una función currificada en una BiFunction normal
    static <A, B, R> BiFunction<A, B, R> uncurry(Function<A, Function<B, R>> f) {
        return (a, b) -> f.apply(a).apply(b);
    }
}
