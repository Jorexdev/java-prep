import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Ejercicio3 {
    public static void main(String[] args) {
        List<String> lista = List.of("a", "b", "c", "d", "e");
        System.out.println("Lista original:  " + lista);

        // Apilamos todos los elementos
        Deque<String> stack = new ArrayDeque<>();
        for (String s : lista) stack.push(s);

        // Desapilamos en orden inverso
        List<String> invertida = new ArrayList<>();
        while (!stack.isEmpty()) {
            invertida.add(stack.pop());
        }

        System.out.println("Lista invertida: " + invertida);
    }
}
