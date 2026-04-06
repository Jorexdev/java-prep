package base.streams.ejemplosercicios.dificil.soluciones;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class Ejercicio9 {

    public static void main(String[] args) {

        List<Document> documents = List.of(
                new Document("Guía de Java", "Ana López", LocalDate.of(2023, 5, 10)),
                new Document("Introducción a Streams", "Carlos Pérez", LocalDate.of(2024, 1, 2)),
                new Document("Patrones de Diseño", "Beatriz Ruiz", LocalDate.of(2022, 11, 3)),
                new Document("Refactoring", null, LocalDate.of(2023, 8, 20)),
                new Document(null, "David Gómez", LocalDate.of(2024, 3, 15)),
                new Document("Docker Básico", "Ana López", null),
                new Document(null, null, null)
        );

        List<Document> sorted = documents.stream()
                .sorted(
                        Comparator.comparing((Document d) -> hasNulls(d) ? 1 : 0)
                                .thenComparing(Document::getDate, Comparator.nullsFirst(Comparator.reverseOrder()))
                                .thenComparing(Document::getAuthor, Comparator.nullsFirst(String::compareTo))
                )
                .toList();

        sorted.forEach(System.out::println);
    }

    private static boolean hasNulls(Document d) {
        return d.getDate() == null || d.getAuthor() == null;
    }

    static class Document {
        private final String title;
        private final String author;
        private final LocalDate date;

        public Document(String title, String author, LocalDate date) {
            this.title = title;
            this.author = author;
            this.date = date;
        }

        public String getAuthor() {
            return author;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "Document{" +
                    "title='" + title + '\'' +
                    ", author='" + author + '\'' +
                    ", date=" + date +
                    '}';
        }
    }
}
