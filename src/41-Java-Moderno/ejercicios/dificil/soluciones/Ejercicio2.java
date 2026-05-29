import java.time.Instant;

public class Ejercicio2 {

    // =================== ESTADOS ===================
    sealed interface EstadoPedido
        permits Pendiente, Confirmado, Preparando, Enviado, Entregado, Cancelado {}

    record Pendiente(Instant creadoEn) implements EstadoPedido {}
    record Confirmado(Instant confirmadoEn, String metodoPago) implements EstadoPedido {}
    record Preparando(Instant inicioEn) implements EstadoPedido {}
    record Enviado(Instant enviadoEn, String trackingCode) implements EstadoPedido {}
    record Entregado(Instant entregadoEn) implements EstadoPedido {}
    record Cancelado(Instant canceladoEn, String motivo) implements EstadoPedido {}

    // =================== EVENTOS ===================
    sealed interface Evento permits ConfirmarPago, IniciarPreparacion, Enviar, Entregar, Cancelar {}

    record ConfirmarPago(String metodoPago) implements Evento {}
    record IniciarPreparacion() implements Evento {}
    record Enviar(String trackingCode) implements Evento {}
    record Entregar() implements Evento {}
    record Cancelar(String motivo) implements Evento {}

    // =================== RESULT ===================
    sealed interface Result<T> permits Result.Ok, Result.Err {
        record Ok<T>(T value) implements Result<T> {}
        record Err<T>(String message) implements Result<T> {}

        static <T> Result<T> ok(T value) { return new Ok<>(value); }
        static <T> Result<T> err(String msg) { return new Err<>(msg); }
    }

    // =================== MAQUINA DE ESTADOS ===================
    static Result<EstadoPedido> transicionar(EstadoPedido estado, Evento evento) {
        Instant ahora = Instant.now();
        return switch (estado) {
            case Pendiente p -> switch (evento) {
                case ConfirmarPago(var metodo) -> Result.ok(new Confirmado(ahora, metodo));
                case Cancelar(var motivo)      -> Result.ok(new Cancelado(ahora, motivo));
                default -> Result.err(
                    "Transicion invalida: " + estado.getClass().getSimpleName() +
                    " + " + evento.getClass().getSimpleName()
                );
            };
            case Confirmado c -> switch (evento) {
                case IniciarPreparacion() -> Result.ok(new Preparando(ahora));
                case Cancelar(var motivo) -> Result.ok(new Cancelado(ahora, motivo));
                default -> Result.err(
                    "Transicion invalida: Confirmado no acepta " + evento.getClass().getSimpleName()
                );
            };
            case Preparando pr -> switch (evento) {
                case Enviar(var codigo) -> Result.ok(new Enviado(ahora, codigo));
                default -> Result.err(
                    "Transicion invalida: Preparando no acepta " + evento.getClass().getSimpleName()
                );
            };
            case Enviado en -> switch (evento) {
                case Entregar() -> Result.ok(new Entregado(ahora));
                default -> Result.err(
                    "Transicion invalida: Enviado no acepta " + evento.getClass().getSimpleName()
                );
            };
            case Entregado e ->
                Result.err("Transicion invalida: Entregado es estado final");
            case Cancelado c ->
                Result.err("Transicion invalida: Cancelado es estado final");
        };
    }

    static String nombreEstado(EstadoPedido e) {
        return switch (e) {
            case Pendiente p   -> "PENDIENTE";
            case Confirmado c  -> "CONFIRMADO [" + c.metodoPago() + "]";
            case Preparando pr -> "PREPARANDO";
            case Enviado en    -> "ENVIADO [" + en.trackingCode() + "]";
            case Entregado et  -> "ENTREGADO";
            case Cancelado ca  -> "CANCELADO [" + ca.motivo() + "]";
        };
    }

    static EstadoPedido aplicar(EstadoPedido estado, Evento evento) {
        Result<EstadoPedido> resultado = transicionar(estado, evento);
        return switch (resultado) {
            case Result.Ok<EstadoPedido> ok -> {
                System.out.println("  [OK] " + nombreEstado(estado) +
                    " --[" + evento.getClass().getSimpleName() + "]--> " + nombreEstado(ok.value()));
                yield ok.value();
            }
            case Result.Err<EstadoPedido> err -> {
                System.out.println("  [ERR] " + err.message());
                yield estado; // estado no cambia
            }
        };
    }

    public static void main(String[] args) {
        System.out.println("=== State Machine de Pedido ===\n");

        // Happy path completo
        System.out.println("--- Happy Path ---");
        EstadoPedido estado = new Pendiente(Instant.now());
        System.out.println("Estado inicial: " + nombreEstado(estado));

        estado = aplicar(estado, new ConfirmarPago("VISA-****1234"));
        estado = aplicar(estado, new IniciarPreparacion());
        estado = aplicar(estado, new Enviar("TRACK-ESP-000123"));
        estado = aplicar(estado, new Entregar());

        System.out.println("Estado final: " + nombreEstado(estado));

        // Transiciones invalidas
        System.out.println("\n--- Transiciones invalidas ---");

        // Intentar enviar desde Entregado (estado final)
        aplicar(estado, new Enviar("TRACK-456"));

        // Intentar IniciarPreparacion desde Pendiente (saltandose Confirmado)
        EstadoPedido pendiente = new Pendiente(Instant.now());
        aplicar(pendiente, new IniciarPreparacion());

        // Intentar Entregar desde Confirmado (sin pasar por Preparando y Enviado)
        EstadoPedido confirmado = new Confirmado(Instant.now(), "MASTERCARD");
        aplicar(confirmado, new Entregar());

        // Cancelacion desde Confirmado (permitida)
        System.out.println("\n--- Cancelacion desde Confirmado ---");
        EstadoPedido conf2 = new Confirmado(Instant.now(), "PayPal");
        System.out.println("Estado: " + nombreEstado(conf2));
        EstadoPedido cancelado = aplicar(conf2, new Cancelar("El cliente cambio de opinion"));
        System.out.println("Estado final: " + nombreEstado(cancelado));

        // Intentar reactivar un pedido cancelado
        System.out.println("\n--- Intentar reactivar pedido cancelado ---");
        aplicar(cancelado, new ConfirmarPago("AMEX"));
    }
}
