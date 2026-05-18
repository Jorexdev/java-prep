import java.util.List;

public class Ejercicio1 {

    static int parsearEntero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido: " + s);
            return -1;
        }
    }

    public static void main(String[] args) {
        // Caso válido
        int resultado1 = parsearEntero("42");
        System.out.println("parsearEntero(\"42\") = " + resultado1);

        // Caso inválido: no es número
        int resultado2 = parsearEntero("abc");
        System.out.println("parsearEntero(\"abc\") = " + resultado2);

        // Caso null: Integer.parseInt lanza NumberFormatException para null
        int resultado3 = parsearEntero(null);
        System.out.println("parsearEntero(null) = " + resultado3);
    }
}
