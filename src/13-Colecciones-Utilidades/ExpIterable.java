import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExpIterable {

    public static void main(String[] args) {

        // Iterable es la superinterfaz de todas las colecciones del JCF
        // implementar Iterable habilita el uso de for-each en la clase
        List<String> lista = new ArrayList<>(List.of("Java", "Spring", "Hibernate"));

        // forma manual con iterator() — útil cuando necesitas remove() durante la iteración
        Iterator<String> it = lista.iterator();
        while (it.hasNext()) {
            System.out.println("iterator() → " + it.next());
        }

        // forma compacta con for-each — Java llama a iterator() internamente
        for (String elem : lista) {
            System.out.println("foreach   → " + elem);
        }
    }
}
