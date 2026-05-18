import java.util.List;

public class Ejercicio3 {

    static <T> void imprimirTodos(List<T> lista) {
        for (T elemento : lista) {
            System.out.println(elemento);
        }
    }

    public static void main(String[] args) {
        System.out.println("-- Strings --");
        imprimirTodos(List.of("Java", "Genéricos", "Son", "Útiles"));

        System.out.println("-- Doubles --");
        imprimirTodos(List.of(3.14, 2.71, 1.41));

        System.out.println("-- Integers --");
        imprimirTodos(List.of(100, 200, 300));
    }
}
