import java.util.*;

public class Ejercicio3 {

    // =================== JERARQUIA JSON ===================
    sealed interface JsonValue permits JsonNull, JsonBool, JsonNumber, JsonString, JsonArray, JsonObject {}
    record JsonNull() implements JsonValue { public String toString() { return "null"; } }
    record JsonBool(boolean value) implements JsonValue {}
    record JsonNumber(double value) implements JsonValue {}
    record JsonString(String value) implements JsonValue {}
    record JsonArray(List<JsonValue> elements) implements JsonValue {}
    record JsonObject(Map<String, JsonValue> fields) implements JsonValue {}

    // =================== STRINGIFY ===================
    static String stringify(JsonValue v) {
        return switch (v) {
            case JsonNull n               -> "null";
            case JsonBool(var b)          -> String.valueOf(b);
            case JsonNumber(var d)        -> d == Math.floor(d) && !Double.isInfinite(d)
                                                ? String.valueOf((long) d) : String.valueOf(d);
            case JsonString(var s)        -> "\"" + escapeString(s) + "\"";
            case JsonArray(var elems)     -> {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < elems.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(stringify(elems.get(i)));
                }
                sb.append("]");
                yield sb.toString();
            }
            case JsonObject(var fields)   -> {
                StringBuilder sb = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<String, JsonValue> entry : fields.entrySet()) {
                    if (!first) sb.append(", ");
                    sb.append("\"").append(entry.getKey()).append("\": ");
                    sb.append(stringify(entry.getValue()));
                    first = false;
                }
                sb.append("}");
                yield sb.toString();
            }
        };
    }

    private static String escapeString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // =================== ACCESO ===================
    static Optional<JsonValue> get(JsonValue obj, String key) {
        if (obj instanceof JsonObject(var fields)) {
            return Optional.ofNullable(fields.get(key));
        }
        return Optional.empty();
    }

    // =================== PARSER ===================
    // Parser recursivo descendente simple
    static JsonValue parse(String json) {
        return new Parser(json.strip()).parseValue();
    }

    static class Parser {
        private final String input;
        private int pos = 0;

        Parser(String input) { this.input = input; }

        JsonValue parseValue() {
            skipWhitespace();
            if (pos >= input.length()) throw new IllegalArgumentException("JSON vacio");
            char c = input.charAt(pos);
            return switch (c) {
                case 'n' -> parseNull();
                case 't', 'f' -> parseBool();
                case '"' -> parseString();
                case '[' -> parseArray();
                case '{' -> parseObject();
                default -> {
                    if (c == '-' || Character.isDigit(c)) yield parseNumber();
                    throw new IllegalArgumentException("Token inesperado: '" + c + "' en pos=" + pos);
                }
            };
        }

        private JsonNull parseNull() {
            expect("null");
            return new JsonNull();
        }

        private JsonBool parseBool() {
            if (input.startsWith("true", pos)) { pos += 4; return new JsonBool(true); }
            if (input.startsWith("false", pos)) { pos += 5; return new JsonBool(false); }
            throw new IllegalArgumentException("Booleano invalido en pos=" + pos);
        }

        private JsonNumber parseNumber() {
            int start = pos;
            if (pos < input.length() && input.charAt(pos) == '-') pos++;
            while (pos < input.length() && (Character.isDigit(input.charAt(pos)) ||
                   input.charAt(pos) == '.' || input.charAt(pos) == 'e' ||
                   input.charAt(pos) == 'E' || input.charAt(pos) == '+' ||
                   input.charAt(pos) == '-' && pos > start)) pos++;
            return new JsonNumber(Double.parseDouble(input.substring(start, pos)));
        }

        private JsonString parseString() {
            expect("\"");
            StringBuilder sb = new StringBuilder();
            while (pos < input.length() && input.charAt(pos) != '"') {
                char c = input.charAt(pos++);
                if (c == '\\') {
                    char esc = input.charAt(pos++);
                    sb.append(switch (esc) {
                        case '"' -> '"'; case '\\' -> '\\'; case '/' -> '/';
                        case 'n' -> '\n'; case 'r' -> '\r'; case 't' -> '\t';
                        default -> throw new IllegalArgumentException("Escape invalido: \\" + esc);
                    });
                } else {
                    sb.append(c);
                }
            }
            expect("\"");
            return new JsonString(sb.toString());
        }

        private JsonArray parseArray() {
            expect("[");
            skipWhitespace();
            List<JsonValue> elems = new ArrayList<>();
            if (pos < input.length() && input.charAt(pos) == ']') { pos++; return new JsonArray(elems); }
            elems.add(parseValue());
            skipWhitespace();
            while (pos < input.length() && input.charAt(pos) == ',') {
                pos++;
                elems.add(parseValue());
                skipWhitespace();
            }
            expect("]");
            return new JsonArray(elems);
        }

        private JsonObject parseObject() {
            expect("{");
            skipWhitespace();
            Map<String, JsonValue> fields = new LinkedHashMap<>();
            if (pos < input.length() && input.charAt(pos) == '}') { pos++; return new JsonObject(fields); }
            parsePair(fields);
            skipWhitespace();
            while (pos < input.length() && input.charAt(pos) == ',') {
                pos++;
                parsePair(fields);
                skipWhitespace();
            }
            expect("}");
            return new JsonObject(fields);
        }

        private void parsePair(Map<String, JsonValue> fields) {
            skipWhitespace();
            String key = parseString().value();
            skipWhitespace();
            expect(":");
            fields.put(key, parseValue());
        }

        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
        }

        private void expect(String token) {
            if (!input.startsWith(token, pos))
                throw new IllegalArgumentException(
                    "Esperado '" + token + "' en pos=" + pos + ", encontrado: '" +
                    (pos < input.length() ? input.charAt(pos) : "EOF") + "'"
                );
            pos += token.length();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Mini JSON Parser ===\n");

        // Valores primitivos
        System.out.println("--- Primitivos ---");
        String[] primitivos = { "null", "true", "false", "42", "3.14", "\"texto\"", "-7.5" };
        for (String s : primitivos) {
            JsonValue v = parse(s);
            System.out.printf("  parse(%s) -> %s [%s] -> stringify: %s%n",
                s, v, v.getClass().getSimpleName(), stringify(v));
        }

        // Array
        System.out.println("\n--- Array ---");
        JsonValue arr = parse("[1, \"dos\", true, null, 3.14]");
        System.out.println("  parse: " + stringify(arr));

        // Objeto simple
        System.out.println("\n--- Objeto simple ---");
        JsonValue obj = parse("{\"nombre\": \"Java\", \"version\": 21, \"activo\": true}");
        System.out.println("  parse: " + stringify(obj));
        System.out.println("  get(nombre): " + get(obj, "nombre").map(Ejercicio3::stringify));
        System.out.println("  get(version): " + get(obj, "version").map(Ejercicio3::stringify));
        System.out.println("  get(noexiste): " + get(obj, "noexiste"));

        // Objeto anidado
        System.out.println("\n--- Objeto anidado ---");
        String nestedJson = """
                {"config": {"host": "localhost", "port": 8080}, "debug": true}""";
        JsonValue nested = parse(nestedJson);
        System.out.println("  parse: " + stringify(nested));
        Optional<JsonValue> config = get(nested, "config");
        config.flatMap(c -> get(c, "host"))
              .ifPresent(h -> System.out.println("  config.host = " + stringify(h)));
        config.flatMap(c -> get(c, "port"))
              .ifPresent(p -> System.out.println("  config.port = " + stringify(p)));

        // Array de objetos
        System.out.println("\n--- Array de objetos ---");
        String arrObjs = "[{\"id\": 1, \"nombre\": \"Alice\"}, {\"id\": 2, \"nombre\": \"Bob\"}]";
        JsonValue listaPersonas = parse(arrObjs);
        System.out.println("  parse: " + stringify(listaPersonas));

        // Verificar round-trip: parse -> stringify -> parse -> stringify
        System.out.println("\n--- Round-trip ---");
        String original = "{\"a\": [1, 2, 3], \"b\": {\"c\": \"hola\\nmundo\"}}";
        String roundTrip = stringify(parse(original));
        System.out.println("  Original:   " + original);
        System.out.println("  Round-trip: " + roundTrip);
        System.out.println("  Igual: " + stringify(parse(original)).equals(stringify(parse(roundTrip))));
    }
}
