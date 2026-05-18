import java.util.function.UnaryOperator;

public class Ejercicio6 {

    public static void main(String[] args) {

        UnaryOperator<String> caesar = texto -> {
            StringBuilder sb = new StringBuilder();
            for (char c : texto.toCharArray()) {
                if (Character.isLetter(c)) {
                    // mantiene mayúscula/minúscula y hace wrap en el alfabeto
                    char base = Character.isUpperCase(c) ? 'A' : 'a';
                    sb.append((char) (base + (c - base + 3) % 26));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        };

        System.out.println(caesar.apply("Hola Mundo"));  // Krod Pxqgr
        System.out.println(caesar.apply("Java"));         // Mdyd
    }
}
