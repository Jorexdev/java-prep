import java.util.ArrayList;
import java.util.List;

// Simula la negociación de contenido de Spring MVC via Accept header.
// MessageConverter serializa el mismo objeto Java a JSON o XML según
// lo que el cliente declare en Accept: application/json / application/xml.

// ── Modelo ────────────────────────────────────────────────────────────────────

// @Entity / @JsonSerializable — POJO plano
class User {
    private final Long id;
    private final String nombre;
    private final String email;
    private final boolean activo;

    User(Long id, String nombre, String email, boolean activo) {
        this.id     = id;
        this.nombre = nombre;
        this.email  = email;
        this.activo = activo;
    }

    public Long    getId()     { return id; }
    public String  getNombre() { return nombre; }
    public String  getEmail()  { return email; }
    public boolean isActivo()  { return activo; }
}

// ── Interfaz MessageConverter ─────────────────────────────────────────────────

// Equivale a org.springframework.http.converter.HttpMessageConverter
interface MessageConverter {
    // Retorna true si este converter puede escribir el mediaType solicitado
    boolean canWrite(String mediaType);

    // Serializa el objeto al formato correspondiente
    String write(Object object);

    // Media type que produce (para el header Content-Type de la respuesta)
    String getMediaType();
}

// ── Converter JSON ────────────────────────────────────────────────────────────

// Equivale a MappingJackson2HttpMessageConverter
class JsonMessageConverter implements MessageConverter {

    @Override
    public boolean canWrite(String mediaType) {
        return mediaType.contains("application/json") || mediaType.equals("*/*");
    }

    @Override
    public String write(Object obj) {
        if (obj instanceof User u) {
            return "{\n"
                + "  \"id\": "      + u.getId()     + ",\n"
                + "  \"nombre\": \"" + u.getNombre() + "\",\n"
                + "  \"email\": \""  + u.getEmail()  + "\",\n"
                + "  \"activo\": "   + u.isActivo()  + "\n"
                + "}";
        }
        return "{}";
    }

    @Override
    public String getMediaType() { return "application/json"; }
}

// ── Converter XML ─────────────────────────────────────────────────────────────

// Equivale a Jaxb2RootElementHttpMessageConverter
class XmlMessageConverter implements MessageConverter {

    @Override
    public boolean canWrite(String mediaType) {
        return mediaType.contains("application/xml") || mediaType.contains("text/xml");
    }

    @Override
    public String write(Object obj) {
        if (obj instanceof User u) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<user>\n"
                + "  <id>"      + u.getId()     + "</id>\n"
                + "  <nombre>"  + u.getNombre() + "</nombre>\n"
                + "  <email>"   + u.getEmail()  + "</email>\n"
                + "  <activo>"  + u.isActivo()  + "</activo>\n"
                + "</user>";
        }
        return "<user/>";
    }

    @Override
    public String getMediaType() { return "application/xml"; }
}

// ── ContentNegotiationHandler ─────────────────────────────────────────────────

// Equivale al DispatcherServlet seleccionando el HttpMessageConverter correcto
class ContentNegotiationHandler {

    private final List<MessageConverter> converters = new ArrayList<>();

    ContentNegotiationHandler() {
        // Spring registra converters en orden; primero JSON, luego XML
        converters.add(new JsonMessageConverter());
        converters.add(new XmlMessageConverter());
    }

    // Simula el endpoint: GET /api/users/{id}
    // @GetMapping(value="/api/users/{id}", produces={"application/json","application/xml"})
    void handle(Long id, String acceptHeader) {
        System.out.println("  Petición: GET /api/users/" + id + "  Accept: " + acceptHeader);

        // En Spring MVC este paso lo hace ContentNegotiationManager
        MessageConverter selected = converters.stream()
            .filter(c -> c.canWrite(acceptHeader))
            .findFirst()
            .orElse(null);

        if (selected == null) {
            System.out.println("  → 406 Not Acceptable — ningún converter soporta: " + acceptHeader);
            return;
        }

        // Simula la "lógica de negocio" del controller
        User user = new User(id, "Jorge Martínez", "jorge@example.com", true);

        System.out.println("  Converter seleccionado: " + selected.getClass().getSimpleName());
        System.out.println("  Content-Type respuesta: " + selected.getMediaType());
        System.out.println("  Body:\n");
        System.out.println(selected.write(user));
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpContentNegotiation {
    public static void main(String[] args) {

        ContentNegotiationHandler handler = new ContentNegotiationHandler();

        System.out.println("=== Simulación Content Negotiation (Accept header) ===\n");

        // ─── Mismo endpoint, dos clientes distintos ───────────────────────────

        System.out.println("--- Cliente 1: espera JSON ---");
        handler.handle(1L, "application/json");

        System.out.println("\n" + "─".repeat(55) + "\n");

        System.out.println("--- Cliente 2: espera XML ---");
        handler.handle(1L, "application/xml");

        System.out.println("\n" + "─".repeat(55) + "\n");

        System.out.println("--- Cliente 3: acepta cualquier cosa (*/*) → JSON gana (primer converter) ---");
        handler.handle(1L, "*/*");

        System.out.println("\n" + "─".repeat(55) + "\n");

        System.out.println("--- Cliente 4: Accept no soportado → 406 ---");
        handler.handle(1L, "application/pdf");

        System.out.println("\n" + "─".repeat(55));
        System.out.println("\n[ Notas ]");
        System.out.println("  @RequestMapping(produces=...) restringe qué converters aplican al endpoint.");
        System.out.println("  Si el cliente envía Accept: application/xml pero el método solo produce JSON");
        System.out.println("  → Spring devuelve 406 Not Acceptable antes de invocar el handler.");
        System.out.println("  El orden de registro de converters en la lista importa: */* siempre cae en el primero.");
    }
}
