import java.util.Map;

public class Ejercicio6 {

    interface Serializer {
        String serialize(Map<String, String> data);
        String contentType();
    }

    static class JsonSerializer implements Serializer {
        @Override
        public String serialize(Map<String, String> data) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String> e : data.entrySet()) {
                if (!first) sb.append(", ");
                sb.append("\"").append(e.getKey()).append("\": \"").append(e.getValue()).append("\"");
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        @Override
        public String contentType() { return "application/json"; }
    }

    static class XmlSerializer implements Serializer {
        @Override
        public String serialize(Map<String, String> data) {
            StringBuilder sb = new StringBuilder("<response>\n");
            for (Map.Entry<String, String> e : data.entrySet()) {
                sb.append("  <").append(e.getKey()).append(">")
                  .append(e.getValue())
                  .append("</").append(e.getKey()).append(">\n");
            }
            sb.append("</response>");
            return sb.toString();
        }

        @Override
        public String contentType() { return "application/xml"; }
    }

    static class PlainTextSerializer implements Serializer {
        @Override
        public String serialize(Map<String, String> data) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> e : data.entrySet()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(e.getKey()).append("=").append(e.getValue());
            }
            return sb.toString();
        }

        @Override
        public String contentType() { return "text/plain"; }
    }

    record EndpointResponse(String contentType, String body) {}

    static class NegotiatedEndpoint {
        EndpointResponse handle(String path, Map<String, String> data, String acceptHeader) {
            Serializer serializer = switch (acceptHeader) {
                case "application/json" -> new JsonSerializer();
                case "application/xml"  -> new XmlSerializer();
                default                 -> new PlainTextSerializer();
            };
            String body = serializer.serialize(data);
            return new EndpointResponse(serializer.contentType(), body);
        }
    }

    public static void main(String[] args) {
        NegotiatedEndpoint endpoint = new NegotiatedEndpoint();

        Map<String, String> producto = Map.of(
            "id",     "42",
            "nombre", "Monitor 4K",
            "precio", "349.99"
        );

        String[] accepts = {"application/json", "application/xml", "text/plain"};

        for (String accept : accepts) {
            EndpointResponse resp = endpoint.handle("/productos/42", producto, accept);
            System.out.println("--- Accept: " + accept + " ---");
            System.out.println("Content-Type: " + resp.contentType());
            System.out.println(resp.body());
            System.out.println();
        }
    }
}
