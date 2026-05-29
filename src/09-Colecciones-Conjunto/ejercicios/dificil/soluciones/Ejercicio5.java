import java.util.BitSet;

// Bloom Filter simplificado: estructura probabilista sin falsos negativos, con falsos positivos

public class Ejercicio5 {

    static class BloomFilter {
        private final BitSet bits;
        private final int size;
        private static final int NUM_HASH = 3;

        BloomFilter(int size) {
            this.size = size;
            this.bits = new BitSet(size);
        }

        // Función hash parametrizada por semilla para independencia
        private int hash(String valor, int semilla) {
            int h = semilla;
            for (char c : valor.toCharArray()) {
                h = h * 31 + c;
                h ^= (h >>> 16);
            }
            // Asegurar índice positivo dentro del rango
            return Math.abs(h % size);
        }

        public void add(String valor) {
            for (int i = 0; i < NUM_HASH; i++) {
                bits.set(hash(valor, 1000 + i * 997));
            }
        }

        public boolean mightContain(String valor) {
            for (int i = 0; i < NUM_HASH; i++) {
                if (!bits.get(hash(valor, 1000 + i * 997))) {
                    return false; // bit no marcado → definitivamente NO está
                }
            }
            return true; // todos los bits marcados → probablemente está
        }

        public double tasaRelleno() {
            return (double) bits.cardinality() / size * 100;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Bloom Filter simplificado ===\n");

        // Tamaño del filtro: ~10 bits por elemento (1000 palabras → 10000 bits)
        BloomFilter filtro = new BloomFilter(10_000);

        // --- Añadir 1000 palabras conocidas ---
        System.out.println("Añadiendo 1000 palabras conocidas...");
        for (int i = 0; i < 1_000; i++) {
            filtro.add("palabra_conocida_" + i);
        }
        System.out.printf("Bits marcados: %d / 10000 (tasa de relleno: %.1f%%)%n%n",
                          new BitSet(10_000).cardinality(), filtro.tasaRelleno());

        // --- Verificar que TODAS las palabras conocidas devuelven true (sin falsos negativos) ---
        int falsosNegativos = 0;
        for (int i = 0; i < 1_000; i++) {
            if (!filtro.mightContain("palabra_conocida_" + i)) {
                falsosNegativos++;
            }
        }
        System.out.println("Verificacion de 1000 palabras conocidas:");
        System.out.println("  Falsos negativos: " + falsosNegativos +
                           " (deben ser siempre 0 — propiedad garantizada del Bloom Filter)");

        // --- Probar 200 palabras NO añadidas y contar falsos positivos ---
        int falsosPositivos = 0;
        for (int i = 1000; i < 1200; i++) {
            if (filtro.mightContain("palabra_desconocida_" + i)) {
                falsosPositivos++;
            }
        }
        double tasaFP = (double) falsosPositivos / 200 * 100;
        System.out.println("\nVerificacion de 200 palabras NO añadidas:");
        System.out.printf("  Falsos positivos: %d / 200 (tasa: %.1f%%)%n", falsosPositivos, tasaFP);
        System.out.println("  Tasa esperada con 3 hashes y 10 bits/elem: ~1-3%");

        System.out.println("\n=== Conclusiones ===");
        System.out.println("- Bloom Filter NUNCA da falsos negativos");
        System.out.println("- Si mightContain() devuelve false → el elemento definitivamente NO está");
        System.out.println("- Si devuelve true → PROBABLEMENTE está (puede haber colisiones de bits)");
        System.out.println("- Usos reales: caché de URLs visitadas, filtros de spam, HBase/Cassandra");
    }
}
