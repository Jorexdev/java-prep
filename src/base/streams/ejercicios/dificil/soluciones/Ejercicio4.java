package base.streams.ejercicios.dificil.soluciones;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio4 {

    public static void main(String[] args) {

        List<Word> words = List.of(
                new Word("casa", "ES"),
                new Word("house", "EN"),
                new Word("maison", "FR"),
                new Word("casa", "PT"),
                new Word("casa", "IT"),
                new Word("house", "AU"),
                new Word("haus", "DE"),
                new Word("casa", "ES")
        );

        Map<String, List<String>> languagesByText = words.stream()
                .collect(Collectors.groupingBy(
                        Word::getText,
                        Collectors.mapping(
                                Word::getLanguage,
                                Collectors.collectingAndThen(
                                        Collectors.toSet(),
                                        set -> set.stream().sorted().collect(Collectors.toList())
                                )
                        )
                ));

        languagesByText.forEach((text, langs) ->
                System.out.println(text + " -> " + langs)
        );
    }

    static class Word {
        private final String text;
        private final String language;

        public Word(String text, String language) {
            this.text = text;
            this.language = language;
        }

        public String getText() {
            return text;
        }

        public String getLanguage() {
            return language;
        }

        @Override
        public String toString() {
            return text + " (" + language + ")";
        }
    }
}
