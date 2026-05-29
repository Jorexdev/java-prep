import java.util.ArrayList;
import java.util.List;

// Event Sourcing reactivo: stream de eventos → fold → estado actual
// Los eventos son la fuente de verdad; el estado se reconstruye aplicándolos en orden
public class Ejercicio4 {

    // ======================= EVENTOS =======================
    sealed interface EventoCuenta {
        record CuentaCreada(String id, String titular) implements EventoCuenta {}
        record DepositoRealizado(String id, double importe) implements EventoCuenta {}
        record RetiroRealizado(String id, double importe) implements EventoCuenta {}
        record RetiroRechazado(String id, double intentado, double saldoActual) implements EventoCuenta {}
    }

    // ======================= ESTADO =======================
    record Cuenta(String id, String titular, double saldo) {
        Cuenta conSaldo(double nuevoSaldo) {
            return new Cuenta(id, titular, nuevoSaldo);
        }

        @Override
        public String toString() {
            return String.format("Cuenta{id=%s, titular=%s, saldo=%.2f}", id, titular, saldo);
        }
    }

    // ======================= FOLD =======================
    // Recorre el stream de eventos y aplica cada uno al estado
    static Cuenta fold(List<EventoCuenta> eventos, Cuenta estadoInicial) {
        Cuenta estado = estadoInicial;

        System.out.println("  Estado inicial: " + estado);

        for (EventoCuenta evento : eventos) {
            Cuenta anterior = estado;
            estado = aplicar(estado, evento);

            String desc = switch (evento) {
                case EventoCuenta.CuentaCreada e ->
                    "CuentaCreada(titular=" + e.titular() + ")";
                case EventoCuenta.DepositoRealizado e ->
                    "Deposito(" + e.importe() + ") → saldo " + anterior.saldo() + " → " + estado.saldo();
                case EventoCuenta.RetiroRealizado e ->
                    "Retiro(" + e.importe() + ") → saldo " + anterior.saldo() + " → " + estado.saldo();
                case EventoCuenta.RetiroRechazado e ->
                    "RetiroRechazado(intentado=" + e.intentado() + ", saldo=" + e.saldoActual() + ")";
            };
            System.out.println("  Evento: " + desc + " | Estado: " + estado);
        }

        return estado;
    }

    // Aplica un evento al estado y devuelve el nuevo estado
    // Si el retiro deja saldo negativo, genera RetiroRechazado (no modifica estado)
    static Cuenta aplicar(Cuenta cuenta, EventoCuenta evento) {
        return switch (evento) {
            case EventoCuenta.CuentaCreada e ->
                new Cuenta(e.id(), e.titular(), 0.0);
            case EventoCuenta.DepositoRealizado e ->
                cuenta.conSaldo(cuenta.saldo() + e.importe());
            case EventoCuenta.RetiroRealizado e -> {
                if (cuenta.saldo() < e.importe()) {
                    System.out.println("  *** SALDO INSUFICIENTE: retiro " + e.importe()
                        + " con saldo " + cuenta.saldo() + " → generando RetiroRechazado");
                    // No modificamos el estado, el evento de rechazo ya está en la lista
                    yield cuenta;
                }
                yield cuenta.conSaldo(cuenta.saldo() - e.importe());
            }
            case EventoCuenta.RetiroRechazado e ->
                cuenta; // ya fue rechazado, no cambia el saldo
        };
    }

    // Construir el stream de eventos con validación de retiros
    static List<EventoCuenta> construirStreamEventos(List<EventoCuenta> eventosEntrada) {
        List<EventoCuenta> resultado = new ArrayList<>();
        Cuenta estadoActual = new Cuenta("", "", 0.0);

        for (EventoCuenta evento : eventosEntrada) {
            if (evento instanceof EventoCuenta.RetiroRealizado retiro) {
                double saldoActual = estadoActual.saldo();
                if (saldoActual < retiro.importe()) {
                    // Reemplazar por RetiroRechazado
                    resultado.add(new EventoCuenta.RetiroRechazado(retiro.id(), retiro.importe(), saldoActual));
                    continue;
                }
            }
            resultado.add(evento);
            estadoActual = aplicar(estadoActual, evento);
        }
        return resultado;
    }

    public static void main(String[] args) {
        System.out.println("=== Event Sourcing Reactivo ===\n");

        // Stream de eventos con un retiro que dejaría saldo negativo
        List<EventoCuenta> eventosEntrada = List.of(
            new EventoCuenta.CuentaCreada("cuenta-001", "Ana Garcia"),
            new EventoCuenta.DepositoRealizado("cuenta-001", 1000.0),
            new EventoCuenta.DepositoRealizado("cuenta-001", 500.0),
            new EventoCuenta.RetiroRealizado("cuenta-001", 200.0),
            new EventoCuenta.RetiroRealizado("cuenta-001", 2000.0),  // saldo insuficiente
            new EventoCuenta.DepositoRealizado("cuenta-001", 300.0),
            new EventoCuenta.RetiroRealizado("cuenta-001", 400.0),
            new EventoCuenta.RetiroRealizado("cuenta-001", 100.0),
            new EventoCuenta.DepositoRealizado("cuenta-001", 50.0),
            new EventoCuenta.RetiroRealizado("cuenta-001", 1000.0)   // saldo insuficiente
        );

        System.out.println("--- Construyendo stream de eventos con validación ---\n");
        List<EventoCuenta> eventos = construirStreamEventos(eventosEntrada);

        System.out.println();
        System.out.println("--- Reconstruyendo estado via fold ---\n");
        Cuenta estadoFinal = fold(eventos, new Cuenta("cuenta-001", "", 0.0));

        System.out.println();
        System.out.println("=== Estado final ===");
        System.out.println(estadoFinal);
        System.out.println("Total eventos aplicados: " + eventos.size());
        System.out.println("Rechazados: " + eventos.stream()
            .filter(e -> e instanceof EventoCuenta.RetiroRechazado).count());

        System.out.println();
        System.out.println("=== Ventajas del Event Sourcing ===");
        System.out.println("1. Auditabilidad completa: cada cambio queda registrado como evento.");
        System.out.println("2. Reconstrucción: puedes reproducir cualquier estado histórico.");
        System.out.println("3. CQRS: separar escritura (eventos) de lectura (proyecciones).");
        System.out.println("4. Temporal queries: ¿cuál era el saldo el día X? → fold hasta ese día.");
    }
}
