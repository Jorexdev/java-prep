import java.util.*;
import java.util.stream.Collectors;

public class Ejercicio6 {

    // @Entity
    static class Autor {
        final int id;
        final String nombre;

        Autor(int id, String nombre) { this.id = id; this.nombre = nombre; }

        @Override
        public String toString() { return "Autor{id=" + id + ", nombre='" + nombre + "'}"; }
    }

    // @Entity
    static class Libro {
        final int id;
        final String titulo;
        final int autorId; // FK simulada

        Libro(int id, String titulo, int autorId) {
            this.id = id;
            this.titulo = titulo;
            this.autorId = autorId;
        }

        @Override
        public String toString() {
            return "Libro{id=" + id + ", titulo='" + titulo + "', autorId=" + autorId + "}";
        }
    }

    static class LibroRepository {
        private final List<Libro>       libros = new ArrayList<>();
        private final Map<Integer, Autor> autores = new HashMap<>();
        private int queryCount = 0;

        void resetQueryCount()       { queryCount = 0; }
        int  getQueryCount()         { return queryCount; }

        void cargar() {
            autores.put(1, new Autor(1, "Martin Fowler"));
            autores.put(2, new Autor(2, "Robert C. Martin"));
            autores.put(3, new Autor(3, "Joshua Bloch"));

            libros.add(new Libro(1, "Refactoring",                  1));
            libros.add(new Libro(2, "Patterns of Enterprise App.",   1));
            libros.add(new Libro(3, "Clean Code",                   2));
            libros.add(new Libro(4, "Clean Architecture",           2));
            libros.add(new Libro(5, "The Clean Coder",              2));
            libros.add(new Libro(6, "Effective Java",               3));
            libros.add(new Libro(7, "Java Puzzlers",                3));
            libros.add(new Libro(8, "Análisis de Arquitecturas",    1));
        }

        // ---- Modo LAZY (N+1) ----
        // 1 query para todos los libros + 1 query por libro para el autor
        List<String> cargarConLazy() {
            System.out.println("[LAZY] Ejecutando query: SELECT * FROM libros");
            queryCount++;
            List<String> resultados = new ArrayList<>();
            for (Libro libro : libros) {
                System.out.println("[LAZY] Ejecutando query: SELECT * FROM autores WHERE id=" + libro.autorId);
                queryCount++;
                Autor autor = autores.get(libro.autorId);
                resultados.add(libro.titulo + " — " + autor.nombre);
            }
            return resultados;
        }

        // ---- Modo BATCH FETCH ----
        // 1 query para todos los libros + 1 query batch para todos los autores
        List<String> cargarConBatchFetch() {
            System.out.println("[BATCH] Ejecutando query: SELECT * FROM libros");
            queryCount++;

            // Recopilar todos los autorIds únicos
            Set<Integer> autorIds = libros.stream()
                .map(l -> l.autorId)
                .collect(Collectors.toSet());

            System.out.println("[BATCH] Ejecutando query: SELECT * FROM autores WHERE id IN " + autorIds);
            queryCount++;

            // Precargar autores en un Map
            Map<Integer, Autor> autorMap = autorIds.stream()
                .collect(Collectors.toMap(id -> id, autores::get));

            // Resolver sin más queries
            return libros.stream()
                .map(l -> l.titulo + " — " + autorMap.get(l.autorId).nombre)
                .collect(Collectors.toList());
        }
    }

    public static void main(String[] args) {
        LibroRepository repo = new LibroRepository();
        repo.cargar();

        System.out.println("======== MODO LAZY (N+1) ========");
        repo.resetQueryCount();
        List<String> lazyResult = repo.cargarConLazy();
        int lazyQueries = repo.getQueryCount();
        System.out.println("Resultados:");
        lazyResult.forEach(r -> System.out.println("  " + r));
        System.out.println("Total queries (LAZY): " + lazyQueries + "\n");

        System.out.println("======== MODO BATCH FETCH ========");
        repo.resetQueryCount();
        List<String> batchResult = repo.cargarConBatchFetch();
        int batchQueries = repo.getQueryCount();
        System.out.println("Resultados:");
        batchResult.forEach(r -> System.out.println("  " + r));
        System.out.println("Total queries (BATCH): " + batchQueries + "\n");

        System.out.println("--- Comparativa ---");
        System.out.printf("LAZY:  %d queries (1 + N donde N=%d libros)%n",
            lazyQueries, repo.libros.size());
        System.out.printf("BATCH: %d queries (independiente del número de libros)%n",
            batchQueries);
    }
}
