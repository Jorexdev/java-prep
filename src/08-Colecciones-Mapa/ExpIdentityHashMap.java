import java.util.IdentityHashMap;

public class ExpIdentityHashMap {

    public static void main(String[] args) {

        // IdentityHashMap compara claves con == en lugar de equals()
        // útil cuando necesitas distinguir entre objetos diferentes aunque su contenido sea igual
        IdentityHashMap<String, String> map = new IdentityHashMap<>();

        String k1 = new String("Java"); // nueva instancia en heap
        String k2 = new String("Java"); // otra nueva instancia — k1 != k2 aunque k1.equals(k2)

        map.put(k1, "uno");
        map.put(k2, "dos"); // clave distinta porque == compara referencias, no contenido

        System.out.println("IdentityHashMap tiene " + map.size() + " entradas: " + map); // 2 entradas
    }
}
