import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Ejercicio5 {

    static class DataSource {
        private final String url;

        DataSource(String url) {
            this.url = url;
            System.out.println("[DataSource] Conectado a: " + url);
        }

        String getUrl() {
            return url;
        }
    }

    static class ContenedorCondicional {
        private final Map<String, String> config;
        private final Map<Class<?>, Object> beans = new HashMap<>();

        ContenedorCondicional(Map<String, String> config) {
            this.config = config;
        }

        // Simula @ConditionalOnProperty: solo registra si propiedad == valorEsperado
        <T> boolean registerIf(String propiedad, String valorEsperado, Class<T> tipo, Supplier<T> factory) {
            String valorActual = config.get(propiedad);
            if (valorEsperado.equals(valorActual)) {
                T instancia = factory.get();
                beans.put(tipo, instancia);
                System.out.println("[ContenedorCondicional] Bean registrado: " + tipo.getSimpleName()
                    + " (condicion: " + propiedad + "=" + valorActual + ")");
                return true;
            } else {
                System.out.println("[ContenedorCondicional] Bean OMITIDO: " + tipo.getSimpleName()
                    + " (condicion: " + propiedad + "=" + valorEsperado
                    + " pero encontrado: " + valorActual + ")");
                return false;
            }
        }

        boolean contains(Class<?> tipo) {
            return beans.containsKey(tipo);
        }

        @SuppressWarnings("unchecked")
        <T> T get(Class<T> tipo) {
            Object b = beans.get(tipo);
            if (b == null) throw new IllegalStateException("Bean no disponible: " + tipo.getSimpleName());
            return (T) b;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== @ConditionalOnProperty simulado ===\n");

        // Caso 1: db.enabled = true -> DataSource SE crea
        System.out.println("--- Config: db.enabled=true ---");
        Map<String, String> configOn = new HashMap<>();
        configOn.put("db.enabled", "true");
        configOn.put("db.url", "jdbc:postgresql://localhost/mydb");

        ContenedorCondicional ctx1 = new ContenedorCondicional(configOn);
        ctx1.registerIf("db.enabled", "true", DataSource.class,
            () -> new DataSource(configOn.get("db.url")));

        if (ctx1.contains(DataSource.class)) {
            System.out.println("DataSource disponible: " + ctx1.get(DataSource.class).getUrl());
        }

        System.out.println();

        // Caso 2: db.enabled = false -> DataSource NO se crea
        System.out.println("--- Config: db.enabled=false ---");
        Map<String, String> configOff = new HashMap<>();
        configOff.put("db.enabled", "false");

        ContenedorCondicional ctx2 = new ContenedorCondicional(configOff);
        ctx2.registerIf("db.enabled", "true", DataSource.class,
            () -> new DataSource("jdbc:postgresql://localhost/mydb"));

        System.out.println("DataSource disponible: " + ctx2.contains(DataSource.class));
    }
}
