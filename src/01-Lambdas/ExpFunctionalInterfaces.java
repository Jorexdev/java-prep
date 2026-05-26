import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class ExpFunctionalInterfaces {

    public static void main(String[] args) {

        // ======================================
        // 1. Function<T,R> — transforma T en R
        // ======================================

        Function<String, Integer> longitud = String::length;
        Function<Integer, String> intToStr  = n -> "num:" + n;

        // compose: primero intToStr, luego longitud (orden inverso al de andThen)
        Function<Integer, Integer> longitudDeStr = longitud.compose(intToStr);
        System.out.println("compose: longitud de 'num:42' = " + longitudDeStr.apply(42));

        // andThen: primero longitud, luego intToStr
        Function<String, String> longitudComoStr = longitud.andThen(intToStr);
        System.out.println("andThen: 'Java' → " + longitudComoStr.apply("Java"));

        // ======================================
        // 2. Predicate<T> — condición booleana
        // ======================================

        Predicate<String> esLargo   = s -> s.length() > 5;
        Predicate<String> empiezaConS = s -> s.startsWith("S");

        // and: ambas condiciones deben cumplirse
        Predicate<String> largoYconS = esLargo.and(empiezaConS);
        // or: al menos una condición
        Predicate<String> largoOconS = esLargo.or(empiezaConS);
        // negate: invierte el resultado
        Predicate<String> esCorto = esLargo.negate();

        List<String> palabras = List.of("Spring", "Boot", "Security", "Kafka", "Streams");
        System.out.println("and (largo Y empieza S): " +
                palabras.stream().filter(largoYconS).toList());
        System.out.println("or  (largo O empieza S): " +
                palabras.stream().filter(largoOconS).toList());
        System.out.println("negate (corto):          " +
                palabras.stream().filter(esCorto).toList());

        // ======================================
        // 3. Consumer<T> — consume T sin devolver nada
        // ======================================

        Consumer<String> imprimir    = System.out::println;
        Consumer<String> imprimirMay = s -> System.out.println(">> " + s.toUpperCase());

        // andThen: encadena dos consumidores sobre el mismo valor
        Consumer<String> dobleConsumo = imprimir.andThen(imprimirMay);
        dobleConsumo.accept("hola consumer");

        // ======================================
        // 4. Supplier<T> — provee un T sin recibir argumentos
        // ======================================

        Supplier<List<Producto>> catalogoDefault = () ->
                List.of(new Producto("Laptop", "Electrónica", 999.0),
                        new Producto("Teclado", "Electrónica", 49.0),
                        new Producto("Café", "Alimentación", 8.5));

        List<Producto> catalogo = catalogoDefault.get();

        // ======================================
        // 5. BiFunction<T,U,R> y variantes
        // ======================================

        BiFunction<String, Double, Producto> crearProducto =
                (nombre, precio) -> new Producto(nombre, "Sin categoría", precio);
        System.out.println("BiFunction: " + crearProducto.apply("Mouse", 25.0));

        // UnaryOperator<T> extiende Function<T,T> — misma entrada y salida
        UnaryOperator<String> normalizar = s -> s.trim().toLowerCase();
        System.out.println("UnaryOperator: '" + normalizar.apply("  KAFKA  ") + "'");

        // BinaryOperator<T> extiende BiFunction<T,T,T> — útil en reduce
        BinaryOperator<Double> sumarPrecios = Double::sum;
        double total = catalogo.stream()
                .map(Producto::precio)
                .reduce(0.0, sumarPrecios);
        System.out.println("BinaryOperator reduce: total=" + total);

        // ======================================
        // DEMO FINAL — pipeline con funciones compuestas
        // ======================================

        Predicate<Producto> esElectrónica   = p -> "Electrónica".equals(p.categoria());
        Predicate<Producto> precioBajo      = p -> p.precio() < 100.0;
        Function<Producto, String> formatear =
                p -> p.nombre() + " (" + p.precio() + "€)";

        System.out.println("\n--- Electrónica con precio < 100 ---");
        catalogo.stream()
                .filter(esElectrónica.and(precioBajo))
                .map(formatear)
                .forEach(System.out::println);

        // collectingAndThen: colectar y luego aplicar una transformación final
        String resumen = catalogo.stream()
                .filter(esElectrónica)
                .map(Producto::nombre)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("Electrónica: " + resumen);
    }

    record Producto(String nombre, String categoria, double precio) {
        @Override public String toString() {
            return nombre + "@" + precio + "€";
        }
    }
}
