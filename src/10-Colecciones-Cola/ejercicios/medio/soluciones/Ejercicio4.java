import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Ejercicio4 {

    static Map<Integer, List<Integer>> construirGrafo() {
        Map<Integer, List<Integer>> grafo = new HashMap<>();
        for (int i = 0; i < 6; i++) grafo.put(i, new ArrayList<>());

        // Aristas no dirigidas
        int[][] aristas = {{0,1},{0,2},{1,3},{2,3},{3,4},{4,5},{2,5}};
        for (int[] a : aristas) {
            grafo.get(a[0]).add(a[1]);
            grafo.get(a[1]).add(a[0]);
        }
        return grafo;
    }

    static void bfs(Map<Integer, List<Integer>> grafo, int inicio) {
        Set<Integer> visitados = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();

        queue.offer(inicio);
        visitados.add(inicio);

        System.out.print("BFS desde nodo " + inicio + ": ");
        while (!queue.isEmpty()) {
            int nodo = queue.poll();
            System.out.print(nodo + " ");

            for (int vecino : grafo.get(nodo)) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    queue.offer(vecino);
                }
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Map<Integer, List<Integer>> grafo = construirGrafo();

        System.out.println("Grafo: 0-1, 0-2, 1-3, 2-3, 3-4, 4-5, 2-5");
        bfs(grafo, 0);
    }
}
