import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Ejercicio3 {

    // Grafo ponderado: {nodoOrigen, nodoDestino, peso}
    static final int NODOS = 5;
    static final int[][] ARISTAS = {
        {0, 1, 4},
        {0, 2, 1},
        {2, 1, 2},
        {1, 3, 5},
        {2, 3, 8},
        {3, 4, 2}
    };

    public static void main(String[] args) {
        int origen = 0;
        int[] dist = new int[NODOS];
        int[] prev = new int[NODOS];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[origen] = 0;

        // PQ: int[0]=nodo, int[1]=distancia
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{origen, 0});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int nodo = curr[0], distActual = curr[1];

            if (distActual > dist[nodo]) continue; // entrada obsoleta

            for (int[] arista : ARISTAS) {
                int u = arista[0], v = arista[1], peso = arista[2];
                // Grafo dirigido según tabla; también procesamos el sentido inverso donde aplique
                if (u == nodo) {
                    int nuevaDist = dist[nodo] + peso;
                    if (nuevaDist < dist[v]) {
                        dist[v] = nuevaDist;
                        prev[v] = nodo;
                        pq.offer(new int[]{v, nuevaDist});
                    }
                }
            }
        }

        System.out.println("Dijkstra desde nodo " + origen + ":");
        for (int i = 0; i < NODOS; i++) {
            System.out.printf("  nodo %d → distancia mínima = %d, camino: %s%n",
                    i, dist[i], reconstruirCamino(prev, origen, i));
        }
    }

    static String reconstruirCamino(int[] prev, int origen, int destino) {
        if (prev[destino] == -1 && destino != origen) return "sin camino";
        StringBuilder sb = new StringBuilder();
        for (int n = destino; n != -1; n = prev[n]) {
            sb.insert(0, n);
            if (prev[n] != -1) sb.insert(0, "→");
        }
        return sb.toString();
    }
}
