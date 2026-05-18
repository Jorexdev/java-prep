import java.util.EnumSet;

public class Ejercicio6 {

    enum Dia {
        LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
    }

    public static void main(String[] args) {
        EnumSet<Dia> laborables = EnumSet.of(
                Dia.LUNES, Dia.MARTES, Dia.MIERCOLES, Dia.JUEVES, Dia.VIERNES
        );

        EnumSet<Dia> finDeSemana = EnumSet.complementOf(laborables);

        System.out.println("Días laborables: " + laborables);
        System.out.println("Fin de semana:   " + finDeSemana);
        System.out.println("¿SABADO es laborable? " + laborables.contains(Dia.SABADO));
    }
}
