import java.util.List;

public class Ejercicio6 {

    // List<?> acepta cualquier tipo parametrizado sin importar el argumento de tipo
    // Solo permite operaciones de lectura segura (Object)
    static void imprimirLista(List<?> lista) {
        for (Object elemento : lista) {
            System.out.print(elemento + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        List<String> strings  = List.of("Java", "Python", "Go");
        List<Integer> enteros = List.of(1, 2, 3, 4, 5);
        List<Double>  reales  = List.of(1.1, 2.2, 3.3);

        // El mismo método sirve para los tres tipos sin sobrecarga
        imprimirLista(strings);
        imprimirLista(enteros);
        imprimirLista(reales);
    }
}
