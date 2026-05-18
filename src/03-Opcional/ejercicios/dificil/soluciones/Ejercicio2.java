import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Ejercicio2 {

    // Extrae un campo de un mapa como Optional, casteando al tipo esperado
    @SuppressWarnings("unchecked")
    static <T> Optional<T> campo(Map<?, ?> mapa, String clave, Class<T> tipo) {
        return Optional.ofNullable(mapa.get(clave))
                .filter(tipo::isInstance)
                .map(tipo::cast);
    }

    public static void main(String[] args) {
        // Construir el "JSON" simulado como Map anidado
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", "Jorex");
        usuario.put("edad", 25);

        Map<String, Object> data = new HashMap<>();
        data.put("usuario", usuario);

        Map<String, Object> json = new HashMap<>();
        json.put("data", data);

        // Extraer data.usuario.nombre
        Optional<String> nombre = campo(json, "data", Map.class)
                .flatMap(d -> campo(d, "usuario", Map.class))
                .flatMap(u -> campo(u, "nombre", String.class));

        // Extraer data.usuario.edad
        Optional<Integer> edad = campo(json, "data", Map.class)
                .flatMap(d -> campo(d, "usuario", Map.class))
                .flatMap(u -> campo(u, "edad", Integer.class));

        System.out.println("Nombre: " + nombre.orElse("desconocido"));
        System.out.println("Edad:   " + edad.orElse(-1));

        // Clave ausente — no hay NullPointerException
        Optional<String> apellido = campo(json, "data", Map.class)
                .flatMap(d -> campo(d, "usuario", Map.class))
                .flatMap(u -> campo(u, "apellido", String.class));

        System.out.println("Apellido: " + apellido.orElse("no proporcionado"));

        // Sección ausente en el JSON
        Optional<String> token = campo(json, "auth", Map.class)
                .flatMap(a -> campo(a, "token", String.class));

        System.out.println("Token: " + token.orElse("no autenticado"));
    }
}
