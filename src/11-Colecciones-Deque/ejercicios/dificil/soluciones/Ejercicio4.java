import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ejercicio4 {

    // Tags que no tienen cierre (self-closing)
    static final java.util.Set<String> VOID_TAGS = java.util.Set.of(
            "br", "hr", "img", "input", "link", "meta"
    );

    static boolean esHTMLValido(String html) {
        Deque<String> stack = new ArrayDeque<>();
        Pattern pattern = Pattern.compile("<(/?)([a-zA-Z][a-zA-Z0-9]*)([^>]*)>");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            boolean esCierre = !matcher.group(1).isEmpty();
            String tag       = matcher.group(2).toLowerCase();
            boolean autoClose = matcher.group(3).trim().endsWith("/");

            if (VOID_TAGS.contains(tag) || autoClose) continue; // ignorar void tags

            if (!esCierre) {
                stack.push(tag); // apertura: apilar
            } else {
                // cierre: debe coincidir con el tope del stack
                if (stack.isEmpty() || !stack.pop().equals(tag)) return false;
            }
        }
        return stack.isEmpty();
    }

    public static void main(String[] args) {
        String[] casos = {
            "<div><p></p></div>",
            "<div><p></div></p>",
            "<html><body><h1></h1><p>texto</p></body></html>",
            "<div><span></div>",
            "<br/><div></div>",
            "<ul><li></li><li></li></ul>"
        };

        for (String html : casos) {
            System.out.printf("  %-45s → %s%n", html,
                    esHTMLValido(html) ? "VALIDO" : "INVALIDO");
        }
    }
}
