import java.util.ArrayList;
import java.util.List;

public class ExpComodines {
    public static void main(String[] args) {
        List<? super Integer> lista = new ArrayList<>();

        lista.add(42);   // ✅ puedo meter enteros sin problema
        Object obj = lista.get(0); // ✅ compila, devuelve Object
     //   Number num = lista.get(0); // ❌ error de compilación

        System.out.println(obj);

        List<? extends Number> lista2 = new ArrayList<Integer>();

        Number n = lista2.get(0); // ✅ permitido

    }
}
