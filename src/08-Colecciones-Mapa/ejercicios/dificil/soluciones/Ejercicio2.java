import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Ejercicio2 {

    static class Clave {
        int valor; // campo MUTABLE — peligroso como clave de HashMap

        Clave(int valor) { this.valor = valor; }

        @Override
        public int hashCode() {
            return Objects.hash(valor); // basado en campo mutable
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Clave)) return false;
            return valor == ((Clave) o).valor;
        }
    }

    public static void main(String[] args) {
        Map<Clave, String> mapa = new HashMap<>();
        Clave clave = new Clave(42);

        mapa.put(clave, "dato importante");
        System.out.println("Antes de mutar:");
        System.out.println("  hashCode: " + clave.hashCode());
        System.out.println("  get: " + mapa.get(clave)); // "dato importante"

        // MUTACIÓN: cambiamos el valor que afecta al hashCode
        clave.valor = 99;

        System.out.println("\nDespués de mutar (valor=99):");
        System.out.println("  hashCode: " + clave.hashCode()); // distinto bucket
        System.out.println("  get: " + mapa.get(clave)); // null — perdemos la entrada

        // El dato sigue en el mapa, pero en el bucket del hashCode anterior
        System.out.println("  containsKey: " + mapa.containsKey(clave)); // false
        System.out.println("  mapa no está vacío: " + !mapa.isEmpty());   // true

        /*
         * PROBLEMA: Al mutar 'clave.valor', el hashCode cambió.
         * HashMap busca en el bucket correspondiente al nuevo hashCode (99),
         * pero el dato fue almacenado en el bucket del hashCode original (42).
         * El dato queda "huérfano" — no puede recuperarse.
         *
         * SOLUCIÓN: Usar solo campos INMUTABLES en hashCode/equals,
         * o usar clases inmutables como clave (String, Integer, record, etc.).
         */
    }
}
