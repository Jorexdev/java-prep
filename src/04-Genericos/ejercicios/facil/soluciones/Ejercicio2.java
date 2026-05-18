import java.util.List;

public class Ejercicio2 {

    // Método genérico: el parámetro de tipo <T> se declara antes del tipo de retorno
    static <T> T primero(List<T> lista) {
        if (lista == null || lista.isEmpty()) {
            throw new IllegalArgumentException("La lista está vacía");
        }
        return lista.get(0);
    }

    public static void main(String[] args) {
        List<String> nombres = List.of("Ana", "Bea", "Carlos");
        String primerNombre = primero(nombres);
        System.out.println("Primer nombre: " + primerNombre); // Ana

        List<Integer> numeros = List.of(10, 20, 30);
        Integer primerNumero = primero(numeros);
        System.out.println("Primer número: " + primerNumero); // 10
    }
}
