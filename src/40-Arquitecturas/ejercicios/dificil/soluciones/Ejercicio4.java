import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Ejercicio4 {

    // --- Dominio nuevo ---

    record Nombre(String given, String family) {
        @Override
        public String toString() {
            return given + " " + family;
        }
    }

    record Direccion(String calle, String ciudad) {
        @Override
        public String toString() {
            return calle + ", " + ciudad;
        }
    }

    record Cliente(UUID id, Nombre nombre, Direccion direccion) {
        @Override
        public String toString() {
            return "Cliente{id=" + id + ", nombre=" + nombre + ", direccion=" + direccion + "}";
        }
    }

    interface ClienteRepository {
        Optional<Cliente> findByCodigo(String codigo);
        List<Cliente> findAll();
    }

    // --- Sistema legado ---

    static class LegacyClienteDTO {
        String codCliente;
        String nombreCompleto;
        String domicilio;   // antes "direccionCompleta" — campo renombrado

        LegacyClienteDTO(String codCliente, String nombreCompleto, String domicilio) {
            this.codCliente = codCliente;
            this.nombreCompleto = nombreCompleto;
            this.domicilio = domicilio;
        }
    }

    static class LegacyDatabase {
        private final List<LegacyClienteDTO> data = new ArrayList<>();

        void agregar(LegacyClienteDTO dto) {
            data.add(dto);
        }

        List<LegacyClienteDTO> findAll() {
            return data;
        }

        Optional<LegacyClienteDTO> findByCod(String cod) {
            return data.stream().filter(d -> d.codCliente.equals(cod)).findFirst();
        }
    }

    // --- Anti-Corruption Layer: Translator ---

    static class LegacyTranslator {
        Cliente translate(LegacyClienteDTO dto) {
            Nombre nombre = parseName(dto.nombreCompleto);
            Direccion direccion = parseAddress(dto.domicilio);   // solo aquí cambia si el campo se renombra
            return new Cliente(UUID.nameUUIDFromBytes(dto.codCliente.getBytes()), nombre, direccion);
        }

        private Nombre parseName(String nombreCompleto) {
            String[] parts = nombreCompleto.trim().split("\\s+", 2);
            return new Nombre(parts[0], parts.length > 1 ? parts[1] : "");
        }

        private Direccion parseAddress(String full) {
            int comma = full.indexOf(',');
            if (comma < 0) return new Direccion(full.trim(), "");
            return new Direccion(full.substring(0, comma).trim(), full.substring(comma + 1).trim());
        }
    }

    // --- Adaptador ---

    static class LegacyClienteAdapter implements ClienteRepository {
        private final LegacyDatabase legacyDb;
        private final LegacyTranslator translator;

        LegacyClienteAdapter(LegacyDatabase legacyDb, LegacyTranslator translator) {
            this.legacyDb = legacyDb;
            this.translator = translator;
        }

        @Override
        public Optional<Cliente> findByCodigo(String codigo) {
            return legacyDb.findByCod(codigo).map(translator::translate);
        }

        @Override
        public List<Cliente> findAll() {
            return legacyDb.findAll().stream().map(translator::translate).toList();
        }
    }

    // --- Servicio del dominio nuevo (solo conoce ClienteRepository) ---

    static class ClienteService {
        private final ClienteRepository repository;

        ClienteService(ClienteRepository repository) {
            this.repository = repository;
        }

        void mostrarCliente(String codigo) {
            repository.findByCodigo(codigo)
                .ifPresentOrElse(
                    c -> System.out.println("Encontrado: " + c),
                    () -> System.out.println("No encontrado: " + codigo));
        }

        void listarTodos() {
            repository.findAll().forEach(c -> System.out.println("  " + c));
        }
    }

    public static void main(String[] args) {
        LegacyDatabase legacyDb = new LegacyDatabase();
        legacyDb.agregar(new LegacyClienteDTO("CLI-001", "Ana García", "Gran Vía 1, Madrid"));
        legacyDb.agregar(new LegacyClienteDTO("CLI-002", "Luis Martínez", "Paseo de Gracia 5, Barcelona"));
        legacyDb.agregar(new LegacyClienteDTO("CLI-003", "Marta López", "Calle Mayor 10, Sevilla"));

        LegacyTranslator translator = new LegacyTranslator();
        ClienteRepository repo = new LegacyClienteAdapter(legacyDb, translator);
        ClienteService service = new ClienteService(repo);

        System.out.println("--- ClienteService usa ClienteRepository, sin saber del legado ---");
        service.mostrarCliente("CLI-001");
        service.mostrarCliente("CLI-999");

        System.out.println("\n--- Todos los clientes ---");
        service.listarTodos();

        System.out.println("\n--- Demostración: solo el Translator cambia al renombrar el campo ---");
        System.out.println("LegacyClienteDTO.direccionCompleta → LegacyClienteDTO.domicilio");
        System.out.println("Solo LegacyTranslator.parseAddress() lee 'domicilio'");
        System.out.println("ClienteService, Cliente, Nombre, Direccion — sin cambios");
        service.mostrarCliente("CLI-002");
    }
}
