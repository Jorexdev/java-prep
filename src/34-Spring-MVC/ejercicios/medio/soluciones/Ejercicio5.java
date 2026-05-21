import java.util.HashMap;
import java.util.Map;

public class Ejercicio5 {

    interface Serializer {
        String serialize(Object obj);
    }

    static class JsonSerializer implements Serializer {
        @Override
        public String serialize(Object obj) {
            if (obj instanceof Map<?, ?> map) {
                StringBuilder sb = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) sb.append(", ");
                    sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                    first = false;
                }
                sb.append("}");
                return sb.toString();
            }
            return "\"" + obj.toString() + "\"";
        }
    }

    static class PlainTextSerializer implements Serializer {
        @Override
        public String serialize(Object obj) {
            if (obj instanceof Map<?, ?> map) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(entry.getKey()).append("=").append(entry.getValue());
                }
                return sb.toString();
            }
            return obj.toString();
        }
    }

    static class ContentNegotiator {
        Serializer negotiate(String acceptHeader) {
            if ("application/json".equalsIgnoreCase(acceptHeader)) {
                return new JsonSerializer();
            }
            return new PlainTextSerializer();
        }
    }

    public static void main(String[] args) {
        Map<String, String> producto = new HashMap<>();
        producto.put("id", "1");
        producto.put("nombre", "Teclado");
        producto.put("precio", "49.99");

        ContentNegotiator negotiator = new ContentNegotiator();

        System.out.println("-- Accept: application/json --");
        System.out.println(negotiator.negotiate("application/json").serialize(producto));

        System.out.println("\n-- Accept: text/plain --");
        System.out.println(negotiator.negotiate("text/plain").serialize(producto));
    }
}
