import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Ejercicio2 {

    static class PageResponse<T> {
        List<T> content;
        int page;
        int size;
        int totalPages;
        long totalElements;

        PageResponse(List<T> content, int page, int size, long totalElements) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = (int) Math.ceil((double) totalElements / size);
        }

        @Override
        public String toString() {
            return "PageResponse{page=" + page + ", size=" + size
                + ", totalPages=" + totalPages + ", totalElements=" + totalElements
                + ", content=" + content + "}";
        }
    }

    static <T> PageResponse<T> paginar(List<T> lista, int page, int size) {
        int from = page * size;
        int to = Math.min(from + size, lista.size());
        List<T> slice = (from >= lista.size()) ? List.of() : lista.subList(from, to);
        return new PageResponse<>(slice, page, size, lista.size());
    }

    public static void main(String[] args) {
        List<String> items = IntStream.rangeClosed(1, 20)
            .mapToObj(i -> "Item-" + i)
            .collect(Collectors.toList());

        System.out.println("-- Página 0, tamaño 5 --");
        System.out.println(paginar(items, 0, 5));

        System.out.println("\n-- Página 2, tamaño 5 --");
        System.out.println(paginar(items, 2, 5));

        System.out.println("\n-- Página 3, tamaño 7 --");
        System.out.println(paginar(items, 3, 7));
    }
}
