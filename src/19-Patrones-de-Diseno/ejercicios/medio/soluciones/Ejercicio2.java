import java.util.*;

public class Ejercicio2 {

    interface Comando {
        void ejecutar();
        void deshacer();
    }

    static class Editor {
        private final StringBuilder texto = new StringBuilder();
        void escribir(String s)   { texto.append(s); }
        void eliminar(int n)      { if (n <= texto.length()) texto.delete(texto.length() - n, texto.length()); }
        void aMayusculas()        { String s = texto.toString().toUpperCase(); texto.setLength(0); texto.append(s); }
        void aMinusculas()        { String s = texto.toString().toLowerCase(); texto.setLength(0); texto.append(s); }
        String getTexto()         { return texto.toString(); }
    }

    static class EscribirTexto implements Comando {
        private final Editor editor;
        private final String texto;
        EscribirTexto(Editor e, String t) { this.editor = e; this.texto = t; }
        @Override public void ejecutar() { editor.escribir(texto); }
        @Override public void deshacer() { editor.eliminar(texto.length()); }
    }

    static class MayusculasTexto implements Comando {
        private final Editor editor;
        private String anterior;
        MayusculasTexto(Editor e) { this.editor = e; }
        @Override public void ejecutar() { anterior = editor.getTexto(); editor.aMayusculas(); }
        @Override public void deshacer() { editor.eliminar(editor.getTexto().length()); editor.escribir(anterior); }
    }

    static class HistorialComandos {
        private final Deque<Comando> historial  = new ArrayDeque<>();
        private final Deque<Comando> rehacibles = new ArrayDeque<>();

        void ejecutar(Comando c) {
            c.ejecutar();
            historial.push(c);
            rehacibles.clear();
        }

        void deshacer() {
            if (!historial.isEmpty()) {
                Comando c = historial.pop();
                c.deshacer();
                rehacibles.push(c);
            }
        }

        void rehacer() {
            if (!rehacibles.isEmpty()) {
                Comando c = rehacibles.pop();
                c.ejecutar();
                historial.push(c);
            }
        }
    }

    public static void main(String[] args) {
        Editor editor = new Editor();
        HistorialComandos h = new HistorialComandos();

        h.ejecutar(new EscribirTexto(editor, "Hola "));
        h.ejecutar(new EscribirTexto(editor, "mundo"));
        System.out.println(editor.getTexto()); // Hola mundo

        h.ejecutar(new MayusculasTexto(editor));
        System.out.println(editor.getTexto()); // HOLA MUNDO

        h.deshacer();
        System.out.println(editor.getTexto()); // Hola mundo

        h.deshacer();
        System.out.println(editor.getTexto()); // Hola

        h.rehacer();
        System.out.println(editor.getTexto()); // Hola mundo
    }
}
