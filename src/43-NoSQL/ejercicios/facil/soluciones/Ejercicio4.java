import java.util.*;
import java.util.stream.*;

/**
 * Ejercicio 4 — Sorted Set (ranking)
 * Simula ZADD/ZRANGE/ZRANK de Redis con TreeMap.
 */
public class Ejercicio4 {

    static class SortedSet {
        // score → nombre (TreeMap ordena por clave = score ascendente)
        // Limitación: scores duplicados se sobrescriben. En Redis real, admite scores iguales.
        private final Map<String, Double> scores = new LinkedHashMap<>();

        void zadd(String nombre, double score) {
            scores.put(nombre, score);
        }

        List<String> zrange() {
            return scores.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        int zrank(String nombre) {
            List<String> ranked = zrange();
            return ranked.indexOf(nombre);  // -1 si no existe
        }
    }

    public static void main(String[] args) {
        SortedSet leaderboard = new SortedSet();

        leaderboard.zadd("ana",    1500.0);
        leaderboard.zadd("carlos", 2300.0);
        leaderboard.zadd("bea",    1800.0);
        leaderboard.zadd("david",  900.0);
        leaderboard.zadd("elena",  2100.0);

        List<String> ranking = leaderboard.zrange();
        System.out.println("Ranking (menor → mayor puntuación):");
        for (int i = 0; i < ranking.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, ranking.get(i));
        }

        System.out.println("\nPosición de 'bea' (0-indexed): " + leaderboard.zrank("bea"));
        // Posición 2 → 3er lugar (0=david, 1=ana, 2=bea, 3=elena, 4=carlos)
    }
}
