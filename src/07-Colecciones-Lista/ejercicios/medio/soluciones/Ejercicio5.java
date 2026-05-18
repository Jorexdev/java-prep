import java.util.List;

public class Ejercicio5 {

    public static int segundoMayor(List<Integer> lista) {
        if (lista == null || lista.size() < 2) {
            throw new IllegalArgumentException("La lista debe tener al menos 2 elementos.");
        }

        int max1 = Integer.MIN_VALUE;
        int max2 = Integer.MIN_VALUE;

        for (int num : lista) {
            if (num > max1) {
                max2 = max1;
                max1 = num;
            } else if (num > max2 && num != max1) {
                max2 = num;
            }
        }

        if (max2 == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("La lista no tiene 2 elementos distintos.");
        }
        return max2;
    }

    public static void main(String[] args) {
        List<Integer> lista = List.of(3, 1, 4, 1, 5, 9, 2, 6);
        System.out.println("Lista: " + lista);
        System.out.println("Segundo mayor: " + segundoMayor(lista)); // 6

        List<Integer> lista2 = List.of(10, 10, 10);
        try {
            segundoMayor(lista2);
        } catch (IllegalArgumentException e) {
            System.out.println("Error esperado: " + e.getMessage());
        }
    }
}
