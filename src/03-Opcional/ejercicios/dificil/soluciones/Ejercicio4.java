import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Ejercicio4 {

    static class Candidato {
        private final String nombre;
        private final int    puntuacion;

        Candidato(String nombre, int puntuacion) {
            this.nombre     = nombre;
            this.puntuacion = puntuacion;
        }

        String getNombre()    { return nombre; }
        int    getPuntuacion() { return puntuacion; }

        @Override
        public String toString() {
            return nombre + " (" + puntuacion + "pts)";
        }
    }

    public static void main(String[] args) {
        List<Candidato> candidatos = Arrays.asList(
                new Candidato("Ana",   72),
                new Candidato("Luis",  85),
                new Candidato("Marta", 91),
                new Candidato("Pedro", 60)
        );

        // Primer candidato con puntuación >= 80
        String primerApto = candidatos.stream()
                .filter(c -> c.getPuntuacion() >= 80)
                .findFirst()
                .map(Candidato::getNombre)
                .orElse("Sin candidato apto");

        System.out.println("Primer candidato apto: " + primerApto); // Luis

        // Sin candidatos aptos
        List<Candidato> bajasPuntuaciones = Arrays.asList(
                new Candidato("Diego", 55),
                new Candidato("Eva",   61)
        );

        String sinApto = bajasPuntuaciones.stream()
                .filter(c -> c.getPuntuacion() >= 80)
                .findFirst()
                .map(Candidato::getNombre)
                .orElse("Sin candidato apto");

        System.out.println("Sin candidato apto:    " + sinApto);

        // También podemos obtener el candidato completo antes de extraer el nombre
        Optional<Candidato> candidatoCompleto = candidatos.stream()
                .filter(c -> c.getPuntuacion() >= 80)
                .findFirst();

        candidatoCompleto.ifPresent(c ->
                System.out.println("Candidato completo: " + c));
    }
}
